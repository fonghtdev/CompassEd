import { api, checkSession, clearAuth, goAuthWithRedirect, hideLoading, nav, showLoading, toast } from "./core.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";
import { getLang } from "./i18n.js";

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
  const badge = document.getElementById("history-notif-badge");
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
  const listEl = document.getElementById("history-notif-list");
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
  const notifBtn = document.getElementById("history-notif-btn");
  const notifPanel = document.getElementById("history-notif-panel");
  const notifList = document.getElementById("history-notif-list");
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

async function initHistory() {
  if (!(await checkSession())) {
    goAuthWithRedirect("/history", "history.html");
    return;
  }
  const body = document.getElementById("history-table-body");
  if (!body) return;

  showLoading(getLang() === "vi" ? "Đang tải lịch sử..." : "Loading history...");
  try {
    const rows = await api("/api/history/tests", "GET", null, true);
    body.innerHTML = "";
    if (!rows || !rows.length) {
      body.innerHTML = `<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">${getLang() === "vi" ? "Chưa có lịch sử bài test" : "No test history"}</td></tr>`;
    } else {
      rows.forEach((r) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td class="px-4 py-3">${escapeHtml(r.testName || "")}</td>
          <td class="px-4 py-3">${escapeHtml(`${r.subjectCode || ""} - ${r.subjectName || ""}`)}</td>
          <td class="px-4 py-3">${escapeHtml(r.level || "")}</td>
          <td class="px-4 py-3">${r.submittedAt ? new Date(r.submittedAt).toLocaleString() : ""}</td>
          <td class="px-4 py-3">${Math.round(Number(r.scorePercent || 0))}%</td>`;
        body.appendChild(tr);
      });
    }
  } catch (err) {
    toast(`${getLang() === "vi" ? "Tải lịch sử thất bại" : "History load failed"}: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  const navDash = document.getElementById("history-nav-dashboard");
  if (navDash) navDash.addEventListener("click", () => nav("/dashboard", "dashboard.html"));
  const navRoadmap = document.getElementById("history-nav-roadmap");
  if (navRoadmap) navRoadmap.addEventListener("click", () => nav("/roadmap-dashboard", "roadmapDashboard.html"));
  const navOverview = document.getElementById("history-nav-overview");
  if (navOverview) navOverview.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));
  const navProfile = document.getElementById("history-nav-profile");
  if (navProfile) navProfile.addEventListener("click", () => openInlineProfilePanel());
  const navLogout = document.getElementById("history-nav-logout");
  if (navLogout) {
    navLogout.addEventListener("click", () => {
      clearAuth();
      nav("/landing", "landingPage.html");
    });
  }
  const menuBtn = document.getElementById("history-menu-btn");
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

export { initHistory };
