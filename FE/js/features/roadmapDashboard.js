import { KEYS, api, checkSession, clearAuth, currentRole, goAuthWithRedirect, nav, toast } from "./core.js";
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
  const badge = document.getElementById("rm-notif-badge");
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
  const listEl = document.getElementById("rm-notif-list");
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
  const notifBtn = document.getElementById("rm-notif-btn");
  const notifPanel = document.getElementById("rm-notif-panel");
  const notifList = document.getElementById("rm-notif-list");
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

function subjectCardColor(index) {
  if (index % 3 === 1) return { border: "border-b-emerald-500", ring: "stroke-emerald-500", btn: "bg-emerald-500/10 text-emerald-600 hover:bg-emerald-500" };
  if (index % 3 === 2) return { border: "border-b-amber-500", ring: "stroke-amber-500", btn: "bg-amber-500/10 text-amber-600 hover:bg-amber-500" };
  return { border: "border-b-primary", ring: "stroke-primary", btn: "bg-primary/10 text-primary hover:bg-primary" };
}

function renderSubjects(subjects, activeBySubjectId, keyword) {
  const grid = document.getElementById("rm-subject-grid");
  if (!grid) return { total: 0, enrolled: 0 };

  const normalizedKey = String(keyword || "").trim().toLowerCase();
  const filtered = (subjects || []).filter((subject) => {
    if (!normalizedKey) return true;
    const name = String(subject.name || "").toLowerCase();
    const code = String(subject.code || "").toLowerCase();
    return name.includes(normalizedKey) || code.includes(normalizedKey);
  });

  if (!filtered.length) {
    grid.innerHTML = '<div class="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">Không tìm thấy môn học phù hợp.</div>';
    return { total: 0, enrolled: 0 };
  }

  grid.innerHTML = "";
  let enrolledCount = 0;

  filtered.forEach((subject, idx) => {
    const active = activeBySubjectId.get(Number(subject.id));
    const enrolled = !!active;
    if (enrolled) enrolledCount += 1;

    const level = active && active.level ? active.level : "Not started";
    const progress = Math.max(0, Math.min(100, Number(active && active.progressPercent ? active.progressPercent : 0)));
    const phase = active && active.phase ? active.phase : "LOCKED";
    const palette = subjectCardColor(idx);

    const card = document.createElement("article");
    card.className = `group flex flex-col rounded-2xl border border-slate-200 bg-white p-6 hover:shadow-xl transition-all border-b-4 ${palette.border}`;
    card.innerHTML = `
      <div class="flex justify-between items-start mb-6">
        <div class="flex flex-col min-w-0">
          <span class="text-xs font-bold text-slate-400 uppercase tracking-widest mb-1">${escapeHtml(subject.code || "SUBJECT")}</span>
          <h3 class="text-2xl font-black text-slate-900 break-words">${escapeHtml(subject.name || "Subject")}</h3>
          <span class="text-xs text-slate-500 mt-1">${escapeHtml(level)}</span>
        </div>
        <div class="relative flex items-center justify-center size-16">
          <svg class="size-full -rotate-90" viewBox="0 0 36 36">
            <circle class="stroke-slate-100" cx="18" cy="18" fill="none" r="16" stroke-width="3"></circle>
            <circle class="${palette.ring}" cx="18" cy="18" fill="none" r="16" stroke-dasharray="100" stroke-dashoffset="${100 - progress}" stroke-linecap="round" stroke-width="3"></circle>
          </svg>
          <span class="absolute text-sm font-bold text-slate-900">${progress}%</span>
        </div>
      </div>
      <div class="space-y-4">
        <div class="rounded-xl bg-slate-50 p-4">
          <p class="text-xs font-semibold text-slate-500 mb-1">Current Phase</p>
          <p class="text-sm font-bold text-slate-800 flex items-center gap-2">
            <span class="material-symbols-outlined text-primary text-sm">radio_button_checked</span>
            ${escapeHtml(phase)}
          </p>
        </div>
        <div class="flex items-center justify-between text-xs">
          <span class="font-semibold ${enrolled ? "text-emerald-600" : "text-slate-500"}">${enrolled ? "Subscribed" : "Not subscribed"}</span>
          <span class="text-slate-400">ID: ${Number(subject.id)}</span>
        </div>
      </div>
      <button type="button" class="rm-open-subject mt-8 flex w-full items-center justify-center rounded-xl h-10 ${palette.btn} font-bold hover:text-white transition-all">${enrolled ? "Continue Journey" : "Đăng ký"}</button>`;

    const btn = card.querySelector(".rm-open-subject");
    btn.addEventListener("click", () => {
      localStorage.setItem(KEYS.subjectId, String(subject.id));
      if (!enrolled) {
        nav("/checkout", "checkout.html");
        return;
      }
      nav(`/learning-roadmap?subjectId=${subject.id}`, `coursesDetail.html?subjectId=${subject.id}`);
    });

    grid.appendChild(card);
  });

  return { total: filtered.length, enrolled: enrolledCount };
}

function updateTopStats(stats) {
  const subscribed = document.getElementById("rm-stat-subscribed");
  const ready = document.getElementById("rm-stat-ready");
  const total = document.getElementById("rm-stat-total");
  if (subscribed) subscribed.textContent = `${stats.enrolled} subjects`;
  if (ready) ready.textContent = `${Math.max(0, stats.enrolled)} active`;
  if (total) total.textContent = `${stats.total} subjects`;
}

function ensureRoadmapChoiceModal() {
  let overlay = document.getElementById("rm-choice-overlay");
  if (overlay) return overlay;
  overlay = document.createElement("div");
  overlay.id = "rm-choice-overlay";
  overlay.style.position = "fixed";
  overlay.style.inset = "0";
  overlay.style.background = "rgba(15,23,42,0.45)";
  overlay.style.display = "none";
  overlay.style.alignItems = "center";
  overlay.style.justifyContent = "center";
  overlay.style.zIndex = "9999";
  overlay.innerHTML = `
    <div style="width:min(520px,92vw);background:#fff;border-radius:14px;border:1px solid #e2e8f0;box-shadow:0 18px 40px rgba(2,6,23,0.25);overflow:hidden;">
      <div id="rm-choice-content" style="padding:18px;"></div>
    </div>`;
  document.body.appendChild(overlay);
  return overlay;
}

function openRoadmapChoiceModal(render) {
  const overlay = ensureRoadmapChoiceModal();
  const content = overlay.querySelector("#rm-choice-content");
  overlay.style.display = "flex";
  render(content, () => {
    overlay.style.display = "none";
  });
}

function askSubscribeWhenNoRoadmap() {
  return new Promise((resolve) => {
    openRoadmapChoiceModal((content, close) => {
      content.innerHTML = `
        <h3 style="font-size:20px;font-weight:800;color:#0f172a;margin:0;">Hiện tại chưa có roadmap nào</h3>
        <p style="margin:8px 0 14px 0;color:#475569;font-size:14px;">Bạn có muốn đăng ký để mở roadmap của riêng bạn không?</p>
        <div style="display:flex;gap:10px;justify-content:flex-end;">
          <button id="rm-choice-no" type="button" style="padding:9px 14px;border:1px solid #e2e8f0;background:#fff;border-radius:8px;font-weight:700;color:#334155;cursor:pointer;">Không</button>
          <button id="rm-choice-yes" type="button" style="padding:9px 14px;border:none;background:#2563eb;color:#fff;border-radius:8px;font-weight:700;cursor:pointer;">Có, đăng ký</button>
        </div>`;
      content.querySelector("#rm-choice-no").addEventListener("click", () => {
        close();
        resolve(false);
      });
      content.querySelector("#rm-choice-yes").addEventListener("click", () => {
        close();
        resolve(true);
      });
    });
  });
}

function chooseEnrolledSubjectModal(subjects) {
  return new Promise((resolve) => {
    const rows = (subjects || [])
      .map(
        (s) => `
          <button type="button" data-subject-id="${s.id}" style="text-align:left;padding:10px 12px;border:1px solid #e2e8f0;border-radius:8px;background:#fff;cursor:pointer;">
            <div style="font-weight:700;color:#0f172a;">${escapeHtml(s.name || "Subject")}</div>
            <div style="font-size:12px;color:#64748b;">${escapeHtml(s.code || "")}</div>
          </button>`
      )
      .join("");
    openRoadmapChoiceModal((content, close) => {
      content.innerHTML = `
        <h3 style="font-size:20px;font-weight:800;color:#0f172a;margin:0;">Chọn môn để tiếp tục học</h3>
        <p style="margin:8px 0 14px 0;color:#475569;font-size:14px;">Bạn đang đăng ký nhiều roadmap, hãy chọn môn muốn tiếp tục.</p>
        <div id="rm-subject-choice-list" style="display:grid;gap:8px;max-height:280px;overflow:auto;">${rows}</div>
        <div style="display:flex;justify-content:flex-end;margin-top:12px;">
          <button id="rm-subject-choice-cancel" type="button" style="padding:9px 14px;border:1px solid #e2e8f0;background:#fff;border-radius:8px;font-weight:700;color:#334155;cursor:pointer;">Hủy</button>
        </div>`;
      content.querySelector("#rm-subject-choice-cancel").addEventListener("click", () => {
        close();
        resolve(null);
      });
      content.querySelectorAll("[data-subject-id]").forEach((btn) => {
        btn.addEventListener("click", () => {
          close();
          resolve(Number(btn.getAttribute("data-subject-id")));
        });
      });
    });
  });
}

async function initRoadmapDashboard() {
  const ok = await checkSession();
  if (!ok) {
    goAuthWithRedirect("/roadmap-dashboard", "roadmapDashboard.html");
    return;
  }
  if (currentRole() === "ADMIN") {
    nav("/admin-dashboard", "admin/adminDashboard.html");
    return;
  }

  const navDashboard = document.getElementById("rm-nav-dashboard");
  if (navDashboard) navDashboard.addEventListener("click", (e) => {
    e.preventDefault();
    nav("/dashboard", "dashboard.html");
  });
  const navHistory = document.getElementById("rm-nav-history");
  if (navHistory) navHistory.addEventListener("click", (e) => {
    e.preventDefault();
    nav("/history", "history.html");
  });
  const navOverview = document.getElementById("rm-nav-overview");
  if (navOverview) navOverview.addEventListener("click", (e) => {
    e.preventDefault();
    window.scrollTo({ top: 0, behavior: "smooth" });
  });
  const navProfile = document.getElementById("rm-nav-profile");
  if (navProfile) navProfile.addEventListener("click", (e) => {
    e.preventDefault();
    openInlineProfilePanel();
  });
  const navLogout = document.getElementById("rm-nav-logout");
  if (navLogout) {
    navLogout.addEventListener("click", (e) => {
      e.preventDefault();
      clearAuth();
      nav("/landing", "landingPage.html");
    });
  }

  const startRecommended = document.getElementById("rm-start-recommended");
  const dailyPlan = document.getElementById("rm-view-daily-plan");
  const analytics = document.getElementById("rm-detailed-analytics");
  const switchFocus = document.getElementById("rm-switch-focus-btn");

  let latestSubjects = [];
  let latestActiveMap = new Map();

  const openFirstAvailable = async () => {
    const enrolledSubjects = latestSubjects.filter((s) => latestActiveMap.has(Number(s.id)));
    if (enrolledSubjects.length === 1) {
      const only = enrolledSubjects[0];
      localStorage.setItem(KEYS.subjectId, String(only.id));
      nav(`/learning-roadmap?subjectId=${only.id}`, `coursesDetail.html?subjectId=${only.id}`);
      return;
    }
    if (enrolledSubjects.length >= 2) {
      const selectedId = await chooseEnrolledSubjectModal(enrolledSubjects);
      if (!selectedId) return;
      localStorage.setItem(KEYS.subjectId, String(selectedId));
      nav(`/learning-roadmap?subjectId=${selectedId}`, `coursesDetail.html?subjectId=${selectedId}`);
      return;
    }
    if (!latestSubjects.length) {
      toast("No subjects available", "warn");
      return;
    }
    const okGoCheckout = await askSubscribeWhenNoRoadmap();
    if (okGoCheckout) nav("/checkout", "checkout.html");
  };

  if (startRecommended) startRecommended.addEventListener("click", openFirstAvailable);
  if (dailyPlan) {
    dailyPlan.addEventListener("click", async () => {
      if (!latestActiveMap.size) {
        const okGoCheckout = await askSubscribeWhenNoRoadmap();
        if (okGoCheckout) nav("/checkout", "checkout.html");
        return;
      }
      nav("/dashboard", "dashboard.html");
    });
  }
  if (analytics) analytics.addEventListener("click", () => nav("/dashboard", "dashboard.html"));
  if (switchFocus) switchFocus.addEventListener("click", () => {
    const input = document.getElementById("rm-search-input");
    if (input) input.focus();
  });

  try {
    const [subjects, subData] = await Promise.all([
      api("/api/subjects", "GET", null, false),
      api("/api/me/subscriptions", "GET", null, true)
    ]);
    latestSubjects = Array.isArray(subjects) ? subjects : [];
    const activeRows = (subData && subData.activeSubscriptions) || [];
    latestActiveMap = new Map(activeRows.map((x) => [Number(x.subjectId), x]));

    if (startRecommended) {
      if (activeRows.length > 0) {
        startRecommended.innerHTML = '<span class="material-symbols-outlined mr-2">play_circle</span>Continue';
      } else {
        startRecommended.innerHTML = '<span class="material-symbols-outlined mr-2">play_circle</span>Start Recommended';
      }
    }

    const advisorText = document.getElementById("rm-advisor-text");
    if (advisorText) {
      if (activeRows.length) {
        advisorText.textContent = `You have ${activeRows.length} active roadmap(s). Continue your strongest subject today.`;
      } else {
        advisorText.textContent = "You have no active roadmap yet. Choose a subject below to subscribe and start learning.";
      }
    }

    const searchInput = document.getElementById("rm-search-input");
    const render = () => {
      const stats = renderSubjects(latestSubjects, latestActiveMap, searchInput ? searchInput.value : "");
      updateTopStats(stats);
    };

    render();
    if (searchInput) searchInput.addEventListener("input", render);
  } catch (err) {
    toast(`Không tải được danh sách môn: ${err.message}`, "error");
  }

  await setupNotificationCenter();
}

export { initRoadmapDashboard };
