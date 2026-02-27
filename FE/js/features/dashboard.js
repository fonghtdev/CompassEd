import { api, checkSession, clearAuth, currentRole, hideLoading, nav, showLoading, toast } from "./core.js";
import { getLang, t } from "./i18n.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";

function renderList(targetId, rows, renderer) {
  const wrap = document.getElementById(targetId);
  if (!wrap) return;
  wrap.innerHTML = "";
  if (!rows || !rows.length) {
    wrap.innerHTML = `<div class="list-item">${t("noData")}</div>`;
    return;
  }
  rows.forEach((row) => {
    const el = document.createElement("div");
    el.className = "list-item";
    el.innerHTML = renderer(row);
    wrap.appendChild(el);
  });
}

function renderChart(chart) {
  const wrap = document.getElementById("dashboard-chart");
  if (!wrap || !chart || !Array.isArray(chart.labels) || !Array.isArray(chart.values)) return;
  wrap.innerHTML = "";
  const max = Math.max(...chart.values, 1);
  chart.labels.forEach((label, idx) => {
    const value = Number(chart.values[idx] || 0);
    const col = document.createElement("div");
    col.className = "chart-col";
    col.innerHTML = `
      <div class="chart-bar-wrap"><div class="chart-bar" style="height:${Math.round((value / max) * 120)}px"></div></div>
      <div class="text-xs font-semibold mt-1">${value}</div>
      <div class="chart-label">${label}</div>`;
    wrap.appendChild(col);
  });
}

function escapeHtml(text) {
  return String(text ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function formatNotificationTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const diffMs = Date.now() - date.getTime();
  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) return getLang() === "vi" ? "Vừa xong" : "Just now";
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}d ago`;
  return date.toLocaleDateString();
}

function trRecommendation(text) {
  if (getLang() !== "vi") return text;
  const map = {
    "Take your first placement test to unlock personalized recommendations.": "Hãy làm bài placement đầu tiên để mở đề xuất cá nhân hóa.",
    "Complete final test in active roadmap to move to higher level.": "Hoàn thành bài kiểm tra cuối trong roadmap đang học để lên cấp độ cao hơn.",
    "Finish pending mini-tests before final test.": "Hoàn thành các mini-test còn lại trước bài kiểm tra cuối.",
    "Continue daily lessons to maintain learning streak.": "Tiếp tục học hằng ngày để duy trì chuỗi học tập."
  };
  return map[text] || text;
}

function trTopic(text) {
  if (getLang() !== "vi") return text;
  const map = {
    "Core skills": "Kỹ năng nền tảng",
    "Need more placement data": "Cần thêm dữ liệu placement",
    "Developing": "Đang phát triển",
    "No weak topic detected": "Chưa phát hiện chủ đề yếu",
    "Cannot parse analysis data": "Không thể phân tích dữ liệu"
  };
  return map[text] || text;
}

function trPractice(text) {
  if (getLang() !== "vi") return text;
  const map = {
    "Take placement test to get personalized practice.": "Làm bài placement để nhận bài luyện tập cá nhân hóa."
  };
  return map[text] || text;
}

function setNotifBadge(unreadCount) {
  const badge = document.getElementById("dash-notif-badge");
  if (!badge) return;
  const count = Number(unreadCount || 0);
  if (count > 0) {
    badge.textContent = count > 99 ? "99+" : String(count);
    badge.classList.remove("hidden");
    return;
  }
  badge.textContent = "0";
  badge.classList.add("hidden");
}

function renderNotificationGroups(items) {
  const listEl = document.getElementById("dash-notif-list");
  if (!listEl) return [];
  if (!Array.isArray(items) || !items.length) {
    listEl.innerHTML = '<div class="text-sm text-slate-300 px-1 py-2">Bạn chưa có thông báo nào.</div>';
    return [];
  }

  const unread = items.filter((x) => !x.read);
  const read = items.filter((x) => x.read);
  const blocks = [];
  const pushGroup = (label, rows) => {
    if (!rows.length) return;
    blocks.push(`<div class="px-1 pt-2 pb-1 text-sm font-semibold text-slate-300">${label}</div>`);
    rows.forEach((item) => {
      blocks.push(`
        <article class="group rounded-xl border border-slate-700 bg-slate-800/60 p-3 transition-colors hover:bg-slate-700/80">
          <div class="flex items-start gap-3">
            <div class="flex-1 min-w-0">
              <h4 class="text-sm font-semibold text-slate-100 leading-5 break-words">${escapeHtml(item.title || "Thông báo")}</h4>
              <p class="mt-1 text-xs text-slate-300 leading-5 break-words">${escapeHtml(item.message || "")}</p>
              <p class="mt-2 text-[11px] text-slate-400">${formatNotificationTime(item.createdAt)}</p>
            </div>
            ${item.read ? "" : '<span class="mt-1 h-2.5 w-2.5 rounded-full bg-sky-400 flex-shrink-0"></span>'}
          </div>
        </article>`);
    });
  };

  pushGroup(getLang() === "vi" ? "Mới" : "New", unread);
  pushGroup(getLang() === "vi" ? "Trước đó" : "Earlier", read);
  listEl.innerHTML = blocks.join("");
  return unread.map((x) => x.id).filter((id) => typeof id === "number");
}

async function setupNotificationCenter() {
  const notifBtn = document.getElementById("dash-notif-btn");
  const notifPanel = document.getElementById("dash-notif-panel");
  const notifList = document.getElementById("dash-notif-list");
  if (!notifBtn || !notifPanel || !notifList) return;

  let panelOpen = false;
  let loading = false;

  const closePanel = () => {
    panelOpen = false;
    notifPanel.classList.add("hidden");
  };

  const loadUnreadCount = async () => {
    try {
      const data = await api("/api/notifications/unread-count", "GET", null, true);
      setNotifBadge(data.unreadCount || 0);
    } catch {
      setNotifBadge(0);
    }
  };

  const markAsRead = async (ids) => {
    if (!ids.length) return;
    await Promise.allSettled(ids.map((id) => api(`/api/notifications/${id}/read`, "POST", null, true)));
    setNotifBadge(0);
  };

  const loadNotifications = async () => {
    if (loading) return;
    loading = true;
    notifList.innerHTML = '<div class="text-sm text-slate-300 px-1 py-2">Đang tải thông báo...</div>';
    try {
      const items = await api("/api/notifications", "GET", null, true);
      const unreadIds = renderNotificationGroups(items);
      await markAsRead(unreadIds);
    } catch (err) {
      notifList.innerHTML = `<div class="text-sm text-rose-300 px-1 py-2">Không tải được thông báo: ${escapeHtml(err.message || "Unknown error")}</div>`;
    } finally {
      loading = false;
    }
  };

  notifBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    e.stopPropagation();
    panelOpen = !panelOpen;
    notifPanel.classList.toggle("hidden", !panelOpen);
    if (panelOpen) {
      await loadNotifications();
    }
  });

  document.addEventListener("click", (e) => {
    if (!panelOpen) return;
    if (notifPanel.contains(e.target) || notifBtn.contains(e.target)) return;
    closePanel();
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && panelOpen) {
      closePanel();
    }
  });

  await loadUnreadCount();
}

async function initDashboard() {
  const ok = await checkSession();
  if (!ok) {
    nav("/auth", "auth.html");
    return;
  }
  if (currentRole() === "ADMIN") {
    nav("/admin-dashboard", "admin/adminDashboard.html");
    return;
  }
  showLoading("Loading dashboard...");
  try {
    const data = await api("/api/me/dashboard", "GET", null, true);

    document.getElementById("kpi-lessons").textContent = String(data.overview.completedLessons || 0);
    document.getElementById("kpi-mini-tests").textContent = String(data.overview.miniTestsDone || 0);
    document.getElementById("kpi-avg").textContent = `${Number(data.overview.averageScore || 0).toFixed(1)}%`;
    document.getElementById("kpi-goal").textContent = `${data.overview.goalProgressPercent || 0}%`;
    document.getElementById("goal-text").textContent = data.profile.learningGoal || t("noGoal");
    document.getElementById("goal-target").textContent = `${t("targetScore")}: ${data.overview.targetScore || 75}`;

    renderChart(data.progressChart);
    renderList("roadmap-list", data.roadmaps, (r) => `${r.subjectName} | ${r.level} | ${r.phase} | replan ${r.replanCount}`);
    renderList("test-results", data.testResults, (r) =>
      `${r.subjectCode} ${r.level} | ${Number(r.scorePercent || 0).toFixed(1)}% | ${new Date(r.submittedAt).toLocaleString(getLang() === "vi" ? "vi-VN" : "en-US")}`
    );
    renderList("upcoming-tests", data.upcomingTests, (t) =>
      `${t.subject} | ${t.type} | ${new Date(t.dueAt).toLocaleString(getLang() === "vi" ? "vi-VN" : "en-US")}`
    );
    document.getElementById("ranking-text").textContent = `${t("rank")} #${data.ranking.rank}/${data.ranking.totalLearners} | ${t("avg")} ${data.ranking.myAverageScore}%`;

    const strong = document.getElementById("strong-topics");
    const weak = document.getElementById("weak-topics");
    strong.innerHTML = (data.strengthWeakness.strongTopics || []).map((x) => `<li>${trTopic(x)}</li>`).join("");
    weak.innerHTML = (data.strengthWeakness.weakTopics || []).map((x) => `<li>${trTopic(x)}</li>`).join("");
    document.getElementById("recommendations").innerHTML = (data.recommendations || []).map((x) => `<li>${trRecommendation(x)}</li>`).join("");
    document.getElementById("practice-questions").innerHTML = (data.practiceQuestions || [])
      .map((x) => `<li>${trPractice(x.question)}</li>`).join("");
    renderList("notification-list", data.notifications, (n) =>
      `${n.title} | ${n.message} | ${n.read ? t("read") : t("unread")}`
    );
    document.getElementById("notification-settings").textContent =
      `${t("email")}: ${data.notificationSettings.notifyEmail ? t("on") : t("off")} | ${t("inApp")}: ${data.notificationSettings.notifyInApp ? t("on") : t("off")}`;

    const exportBtn = document.getElementById("download-results");
    if (exportBtn) {
      exportBtn.addEventListener("click", () => {
        window.location.href = `${window.APP_CONFIG.API_BASE || "http://localhost:8080"}${data.resultArchive.downloadCsvUrl}`;
      });
    }
  } catch (err) {
    toast(`Dashboard load failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  const brandLink = document.getElementById("dash-brand-link");
  if (brandLink) brandLink.addEventListener("click", () => nav("/landing", "landingPage.html"));
  const navRoadmap = document.getElementById("dash-nav-roadmap");
  if (navRoadmap) navRoadmap.addEventListener("click", () => nav("/roadmap-dashboard", "roadmapDashboard.html"));
  const navHistory = document.getElementById("dash-nav-history");
  if (navHistory) navHistory.addEventListener("click", () => nav("/history", "history.html"));
  const navProfile = document.getElementById("dash-nav-profile");
  if (navProfile) navProfile.addEventListener("click", () => openInlineProfilePanel());
  const navOverview = document.getElementById("dash-nav-overview");
  if (navOverview) navOverview.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));
  const navLogout = document.getElementById("dash-nav-logout");
  if (navLogout) {
    navLogout.addEventListener("click", () => {
      clearAuth();
      nav("/landing", "landingPage.html");
    });
  }
  const menuBtn = document.getElementById("dash-menu-btn");
  if (menuBtn) {
    menuBtn.addEventListener("click", () => {
      const sidebar = document.querySelector("body > div > aside");
      if (!sidebar) return;
      if (sidebar.classList.contains("hidden")) {
        sidebar.classList.remove("hidden");
        sidebar.classList.add("flex");
      } else {
        sidebar.classList.add("hidden");
        sidebar.classList.remove("flex");
      }
    });
  }

  await setupNotificationCenter();

}

export { initDashboard };



