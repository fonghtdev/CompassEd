import {
  api,
  checkSession,
  clearAuth,
  getSubjectId,
  goAuthWithRedirect,
  hideLoading,
  nav,
  showLoading,
  toast
} from "./core.js";
import { getLang } from "./i18n.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";

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

function setNotifBadge(unreadCount) {
  const badge = document.getElementById("road-notif-badge");
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
  const listEl = document.getElementById("road-notif-list");
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
  const notifBtn = document.getElementById("road-notif-btn");
  const notifPanel = document.getElementById("road-notif-panel");
  const notifList = document.getElementById("road-notif-list");
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
    if (e.key === "Escape" && panelOpen) closePanel();
  });

  await loadUnreadCount();
}

function roadmapActionLabel(phase) {
  if (getLang() === "vi") {
    if (phase === "LESSONS") return "Hoàn thành bài học tiếp theo";
    if (phase === "MINI_TESTS") return "Nộp mini test tiếp theo";
    if (phase === "FINAL_TEST") return "Nộp bài kiểm tra cuối";
    if (phase === "COURSE_COMPLETED") return "Đã hoàn thành roadmap";
    return "Cập nhật roadmap";
  }
  if (phase === "LESSONS") return "Complete next lesson";
  if (phase === "MINI_TESTS") return "Submit next mini test";
  if (phase === "FINAL_TEST") return "Submit final test";
  if (phase === "COURSE_COMPLETED") return "Roadmap completed";
  return "Refresh roadmap";
}

function renderRoadmapStatus(roadmap) {
  const status = document.getElementById("roadmap-api-status");
  if (!status) return;
  const level = roadmap && roadmap.level ? roadmap.level : "-";
  const phase = roadmap && roadmap.phase ? roadmap.phase : "LOCKED";
  const progress = Number(roadmap && roadmap.progressPercent ? roadmap.progressPercent : 0);
  const replans = Number(roadmap && roadmap.replanCount ? roadmap.replanCount : 0);
  status.textContent = `Level ${level} | Phase: ${phase} | Progress: ${progress}% | Replan: ${replans}`;

  const title = document.getElementById("roadmap-title");
  if (title && roadmap && roadmap.subjectName) title.textContent = `${roadmap.subjectName} - ${level}`;

  const startBtn = document.getElementById("roadmap-start-lesson-btn");
  if (startBtn) startBtn.childNodes[0].textContent = roadmapActionLabel(phase) + " ";
}

async function initRoadmap() {
  if (!(await checkSession())) {
    goAuthWithRedirect("/learning-roadmap", "coursesDetail.html");
    return;
  }
  const subjectId = getSubjectId();
  let roadmap = null;

  async function reloadRoadmap() {
    roadmap = await api(`/api/subjects/${subjectId}/roadmap`, "GET", null, true);
    renderRoadmapStatus(roadmap);
    return roadmap;
  }

  try {
    showLoading("Loading roadmap...");
    await reloadRoadmap();
  } catch (err) {
    toast(`Roadmap load failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  const resume = document.getElementById("roadmap-resume-btn");
  if (resume) {
    resume.addEventListener("click", () => nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`));
  }
  const syllabusBtn = document.getElementById("roadmap-syllabus-btn");
  if (syllabusBtn) {
    syllabusBtn.addEventListener("click", async () => {
      try {
        const subjects = await api("/api/subjects", "GET", null, false);
        const current = (subjects || []).find((s) => Number(s.id) === Number(subjectId));
        toast(current ? `Syllabus: ${current.name} (${current.code})` : "Syllabus loaded");
      } catch (e) {
        toast("Cannot load syllabus", "error");
      }
    });
  }

  const historyBtn = document.getElementById("roadmap-history-btn");
  if (historyBtn) historyBtn.addEventListener("click", () => nav("/history", "history.html"));
  const backAllCourses = document.getElementById("road-back-all-courses");
  if (backAllCourses) {
    backAllCourses.addEventListener("click", (e) => {
      e.preventDefault();
      nav("/roadmap-dashboard", "roadmapDashboard.html");
    });
  }
  const navDash = document.getElementById("road-nav-dashboard");
  if (navDash) navDash.addEventListener("click", () => nav("/dashboard", "dashboard.html"));
  const navHistory = document.getElementById("road-nav-history");
  if (navHistory) navHistory.addEventListener("click", () => nav("/history", "history.html"));
  const navProfile = document.getElementById("road-nav-profile");
  if (navProfile) navProfile.addEventListener("click", () => openInlineProfilePanel());
  const navOverview = document.getElementById("road-nav-overview");
  if (navOverview) navOverview.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));
  const navLogout = document.getElementById("road-nav-logout");
  if (navLogout) {
    navLogout.addEventListener("click", () => {
      clearAuth();
      nav("/landing", "landingPage.html");
    });
  }
  const menuBtn = document.getElementById("road-menu-btn");
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

  const action = async () => {
    if (!roadmap) return;
    try {
      showLoading("Updating roadmap...");
      if (roadmap.phase === "LOCKED") {
        nav("/checkout", "checkout.html");
        return;
      }
      if (roadmap.phase === "WAITING_PLACEMENT") {
        nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
        return;
      }
      if (roadmap.phase === "LESSONS") {
        const nextLesson = (roadmap.lessons || []).find((x) => !x.completed);
        if (nextLesson) {
          await api(`/api/lessons/${nextLesson.id}/complete`, "POST", {
            subjectId,
            timeSpentSeconds: Math.max(300, Number(nextLesson.estimatedMinutes || 10) * 60),
            score: 100
          }, true);
        }
      } else if (roadmap.phase === "MINI_TESTS") {
        const nextMini = (roadmap.miniTests || []).find((x) => !x.completed);
        if (nextMini) {
          const raw = prompt("Mini test score (0-100):", "75");
          const score = Math.max(0, Math.min(100, Number(raw)));
          await api(`/api/subjects/${subjectId}/mini-tests/${nextMini.id}/submit`, "POST", { score }, true);
        }
      } else if (roadmap.phase === "FINAL_TEST") {
        const raw = prompt("Final test score (0-100):", "75");
        const score = Math.max(0, Math.min(100, Number(raw)));
        await api(`/api/subjects/${subjectId}/final-test/submit`, "POST", { score }, true);
      } else {
        toast("Roadmap completed");
      }
      await reloadRoadmap();
    } catch (err) {
      toast(`Roadmap action failed: ${err.message}`, "error");
    } finally {
      hideLoading();
    }
  };

  const start = document.getElementById("roadmap-start-lesson-btn");
  if (start) start.addEventListener("click", action);

  const review = document.getElementById("roadmap-review-btn");
  if (review) review.addEventListener("click", action);

  const quick1 = document.getElementById("roadmap-quick-practice-1");
  if (quick1) quick1.addEventListener("click", action);
  const quick2 = document.getElementById("roadmap-quick-practice-2");
  if (quick2) quick2.addEventListener("click", action);
}

export { initRoadmap };


