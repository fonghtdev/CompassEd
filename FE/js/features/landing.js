import { api, checkSession, clearAuth, getAuth, getSubjectId, goAuthWithRedirect, nav, toast, formatVnd } from "./core.js";
import { t } from "./i18n.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";

async function hydrateLandingPricing() {
  try {
    const plans = await api("/api/pricing/plans", "GET", null, false);
    const one = Array.isArray(plans) ? plans.find((p) => Number(p.subjectCount) === 1) : null;
    const singleText = formatVnd(one ? one.amountVnd : 50000);
    ["landing-price-math", "landing-price-literature", "landing-price-english"].forEach((id) => {
      const el = document.getElementById(id);
      if (el) el.textContent = singleText;
    });
  } catch (e) {
    // keep static fallback text in html
  }
}

function initLanding() {
  hydrateLandingPricing();
  let subscribedSubjectIds = new Set();

  const loginLink = document.getElementById("landing-login-link");
  const getStartedTop = document.getElementById("landing-get-started-top");
  const notifWrap = document.getElementById("landing-notif-wrap");
  const notifBtn = document.getElementById("landing-notif-btn");
  const notifBadge = document.getElementById("landing-notif-badge");
  const notifPanel = document.getElementById("landing-notif-panel");
  const notifList = document.getElementById("landing-notif-list");
  let profileMenu = null;
  let popupOverlay = null;
  let notifOpen = false;
  let notifLoading = false;

  const escapeHtml = (text) =>
    String(text ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");

  const formatNotificationTime = (value) => {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "";
    const diffMs = Date.now() - date.getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) return "Just now";
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days}d ago`;
    return date.toLocaleDateString();
  };

  const setNotifBadge = (unreadCount) => {
    if (!notifBadge) return;
    const count = Number(unreadCount || 0);
    if (count > 0) {
      notifBadge.textContent = count > 99 ? "99+" : String(count);
      notifBadge.classList.remove("hidden");
      return;
    }
    notifBadge.textContent = "0";
    notifBadge.classList.add("hidden");
  };

  const renderNotificationGroups = (items) => {
    if (!notifList) return [];
    if (!Array.isArray(items) || !items.length) {
      notifList.innerHTML = '<div class="px-1 py-2 text-sm text-slate-300">Bạn chưa có thông báo nào.</div>';
      return [];
    }
    const unread = items.filter((x) => !x.read);
    const read = items.filter((x) => x.read);
    const html = [];
    const pushGroup = (label, rows) => {
      if (!rows.length) return;
      html.push(`<div class="px-1 pt-2 pb-1 text-sm font-semibold text-slate-300">${label}</div>`);
      rows.forEach((item) => {
        html.push(`
          <article class="group rounded-xl border border-slate-700 bg-slate-800/60 p-3 transition-colors hover:bg-slate-700/80">
            <div class="flex items-start gap-3">
              <div class="min-w-0 flex-1">
                <h4 class="break-words text-sm font-semibold leading-5 text-slate-100">${escapeHtml(item.title || "Thông báo")}</h4>
                <p class="mt-1 break-words text-xs leading-5 text-slate-300">${escapeHtml(item.message || "")}</p>
                <p class="mt-2 text-[11px] text-slate-400">${formatNotificationTime(item.createdAt)}</p>
              </div>
              ${item.read ? "" : '<span class="mt-1 h-2.5 w-2.5 flex-shrink-0 rounded-full bg-sky-400"></span>'}
            </div>
          </article>`);
      });
    };
    pushGroup("Mới", unread);
    pushGroup("Trước đó", read);
    notifList.innerHTML = html.join("");
    return unread.map((x) => x.id).filter((id) => typeof id === "number");
  };

  const closeNotifPanel = () => {
    notifOpen = false;
    if (notifPanel) notifPanel.classList.add("hidden");
  };

  const loadUnreadCount = async () => {
    try {
      const data = await api("/api/notifications/unread-count", "GET", null, true);
      setNotifBadge(data.unreadCount || 0);
    } catch {
      setNotifBadge(0);
    }
  };

  const loadNotifications = async () => {
    if (!notifList || notifLoading) return;
    notifLoading = true;
    notifList.innerHTML = '<div class="px-1 py-2 text-sm text-slate-300">Đang tải thông báo...</div>';
    try {
      const items = await api("/api/notifications", "GET", null, true);
      const unreadIds = renderNotificationGroups(items);
      if (unreadIds.length) {
        await Promise.allSettled(unreadIds.map((id) => api(`/api/notifications/${id}/read`, "POST", null, true)));
      }
      setNotifBadge(0);
    } catch (e) {
      notifList.innerHTML = `<div class="px-1 py-2 text-sm text-rose-300">Không tải được thông báo: ${escapeHtml(e.message || "Unknown error")}</div>`;
    } finally {
      notifLoading = false;
    }
  };

  const closeProfileMenu = () => {
    if (profileMenu) profileMenu.style.display = "none";
  };

  const ensurePopupOverlay = () => {
    if (popupOverlay) return popupOverlay;
    popupOverlay = document.createElement("div");
    popupOverlay.style.position = "fixed";
    popupOverlay.style.inset = "0";
    popupOverlay.style.background = "rgba(15,23,42,0.4)";
    popupOverlay.style.display = "none";
    popupOverlay.style.alignItems = "center";
    popupOverlay.style.justifyContent = "center";
    popupOverlay.style.zIndex = "3000";
    popupOverlay.innerHTML = `
      <div id="landing-popup-card" style="width:min(560px,92vw);background:#fff;border-radius:14px;border:1px solid #e2e8f0;box-shadow:0 18px 40px rgba(2,6,23,0.2);">
        <div id="landing-popup-content"></div>
      </div>`;
    popupOverlay.addEventListener("click", (e) => {
      if (e.target === popupOverlay) popupOverlay.style.display = "none";
    });
    document.body.appendChild(popupOverlay);
    return popupOverlay;
  };

  const closePopup = () => {
    if (popupOverlay) popupOverlay.style.display = "none";
  };

  const setJoinProgramButtons = (activeSubjectIds = new Set(), loggedIn = false) => {
    document.querySelectorAll(".js-join-program").forEach((btn) => {
      const subjectId = Number(btn.getAttribute("data-subject-id"));
      const subscribed = loggedIn && activeSubjectIds.has(subjectId);
      if (subscribed) {
        btn.innerHTML = '<span class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-emerald-100 text-emerald-600"><span class="material-symbols-outlined" style="font-size:20px;">check_circle</span></span>';
        btn.className =
          "js-join-program mt-auto w-full rounded-lg border border-emerald-200 bg-white py-2 text-sm font-bold text-emerald-700 transition-colors hover:bg-emerald-50";
        btn.title = "Đã đăng ký";
      } else {
        btn.textContent = t("joinProgram") || "Join Program";
        btn.className =
          "js-join-program mt-auto w-full rounded-lg bg-slate-100 py-3 text-sm font-bold text-slate-900 transition-colors hover:bg-primary hover:text-white group-hover:bg-primary group-hover:text-white";
        btn.title = "";
      }
    });
  };

  const openProfilePopup = async () => {
    closePopup();
    await openInlineProfilePanel();
  };

  const openSettingPopup = async () => {
    const overlay = ensurePopupOverlay();
    const content = overlay.querySelector("#landing-popup-content");
    content.innerHTML = `<div style="padding:20px 18px;">Loading...</div>`;
    overlay.style.display = "flex";
    try {
      const profile = await api("/api/me/profile", "GET", null, true);
      content.innerHTML = `
        <div style="padding:18px 18px 12px 18px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;">
          <div style="font-size:18px;font-weight:800;color:#0f172a;">Setting</div>
          <button id="setting-popup-close" style="border:none;background:#f8fafc;border-radius:8px;padding:6px 10px;cursor:pointer;">X</button>
        </div>
        <div style="padding:16px 18px;display:grid;gap:12px;">
          <label style="display:flex;align-items:center;gap:8px;">
            <input id="setting-notify-email" type="checkbox" ${profile.notifyEmail ? "checked" : ""}/>
            <span style="font-size:13px;color:#334155;">Email notification</span>
          </label>
          <label style="display:flex;align-items:center;gap:8px;">
            <input id="setting-notify-inapp" type="checkbox" ${profile.notifyInApp ? "checked" : ""}/>
            <span style="font-size:13px;color:#334155;">In-app notification</span>
          </label>
          <button id="setting-popup-save" style="margin-top:6px;padding:10px 12px;border:none;border-radius:8px;background:#0d7ff2;color:#fff;font-weight:700;cursor:pointer;">Save Settings</button>
        </div>`;
      content.querySelector("#setting-popup-close").addEventListener("click", closePopup);
      content.querySelector("#setting-popup-save").addEventListener("click", async () => {
        try {
          const notifyEmail = !!content.querySelector("#setting-notify-email").checked;
          const notifyInApp = !!content.querySelector("#setting-notify-inapp").checked;
          await api("/api/me/profile", "PUT", { notifyEmail, notifyInApp }, true);
          toast("Settings updated");
          closePopup();
        } catch (e) {
          toast(`Save failed: ${e.message}`, "error");
        }
      });
    } catch (e) {
      content.innerHTML = `<div style="padding:20px 18px;color:#b91c1c;">Cannot load settings</div>`;
    }
  };

  const showProfileMenu = () => {
    if (!profileMenu || !loginLink) return;
    const rect = loginLink.getBoundingClientRect();
    const menuWidth = profileMenu.offsetWidth || 300;
    const centerLeft = rect.left + window.scrollX + rect.width / 2 - menuWidth / 2;
    const minLeft = window.scrollX + 8;
    const maxLeft = window.scrollX + window.innerWidth - menuWidth - 8;
    profileMenu.style.top = `${rect.bottom + window.scrollY + 8}px`;
    profileMenu.style.left = `${Math.max(minLeft, Math.min(centerLeft, maxLeft))}px`;
    profileMenu.style.display = "block";
  };

  const ensureProfileMenu = async (user) => {
    if (!profileMenu) {
      profileMenu = document.createElement("div");
      profileMenu.id = "landing-profile-menu";
      profileMenu.style.position = "absolute";
      profileMenu.style.minWidth = "300px";
      profileMenu.style.background = "#fff";
      profileMenu.style.border = "1px solid #e2e8f0";
      profileMenu.style.borderRadius = "12px";
      profileMenu.style.boxShadow = "0 10px 30px rgba(2,6,23,0.12)";
      profileMenu.style.padding = "12px";
      profileMenu.style.zIndex = "2000";
      profileMenu.style.display = "none";
      profileMenu.innerHTML = `
        <div style="display:flex;align-items:center;gap:10px;padding:6px 4px 10px 4px;border-bottom:1px solid #f1f5f9;">
          <div style="width:34px;height:34px;border-radius:999px;background:#f1f5f9;display:flex;align-items:center;justify-content:center;font-weight:700;">${String((user && user.fullName) || (user && user.email) || "U").charAt(0).toUpperCase()}</div>
          <div style="min-width:0;">
            <div style="font-size:13px;font-weight:700;color:#0f172a;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${(user && (user.fullName || user.email)) || "User"}</div>
            <div style="font-size:11px;color:#64748b;">${(user && user.email) || ""}</div>
          </div>
        </div>
        <div style="display:grid;gap:6px;padding-top:10px;">
          <button id="profile-menu-profile" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Profile</button>
          <button id="profile-menu-setting" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Setting</button>
          <div style="padding:8px 10px;border-radius:8px;border:1px dashed #cbd5e1;background:#f8fafc;">
            <div style="font-size:12px;font-weight:700;color:#0f172a;">Subscription</div>
            <div id="profile-menu-subs-list" style="margin-top:6px;font-size:12px;color:#334155;">Loading...</div>
            <button id="profile-menu-subscribe-more" style="margin-top:8px;padding:6px 8px;border-radius:6px;background:#0d7ff2;color:white;font-size:12px;font-weight:700;">Đăng ký thêm</button>
          </div>
          <button id="profile-menu-logout" style="display:flex;align-items:center;gap:8px;text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;color:#b91c1c;background:#fef2f2;">
            <span class="material-symbols-outlined" style="font-size:18px;">logout</span>
            <span>Logout</span>
          </button>
        </div>`;
      document.body.appendChild(profileMenu);

      const profileBtn = profileMenu.querySelector("#profile-menu-profile");
      const settingBtn = profileMenu.querySelector("#profile-menu-setting");
      const subscribeMoreBtn = profileMenu.querySelector("#profile-menu-subscribe-more");
      const logoutBtn = profileMenu.querySelector("#profile-menu-logout");
      const subsCard = profileMenu.querySelector("#profile-menu-subs-list")?.parentElement;

      const applyHover = (el, base, hover) => {
        if (!el) return;
        el.style.transition = "background-color 140ms ease, color 140ms ease";
        el.addEventListener("mouseenter", () => {
          el.style.background = hover;
        });
        el.addEventListener("mouseleave", () => {
          el.style.background = base;
        });
      };
      applyHover(profileBtn, "#f8fafc", "#e2e8f0");
      applyHover(settingBtn, "#f8fafc", "#e2e8f0");
      applyHover(subscribeMoreBtn, "#0d7ff2", "#0b6bc9");
      applyHover(logoutBtn, "#fef2f2", "#fee2e2");
      applyHover(subsCard, "#f8fafc", "#eef2f7");

      profileBtn.addEventListener("click", async () => {
        closeProfileMenu();
        await openProfilePopup();
      });
      settingBtn.addEventListener("click", async () => {
        closeProfileMenu();
        await openSettingPopup();
      });
      subscribeMoreBtn.addEventListener("click", () => nav("/checkout", "checkout.html"));
      logoutBtn.addEventListener("click", () => {
        clearAuth();
        closeProfileMenu();
        nav("/landing", "landingPage.html");
      });
    }

    try {
      const subData = await api("/api/me/subscriptions", "GET", null, true);
      const active = (subData && subData.activeSubscriptions) || [];
      const available = (subData && subData.availableSubjects) || [];
      const list = profileMenu.querySelector("#profile-menu-subs-list");
      if (!active.length) {
        list.innerHTML = "Chưa đăng ký gói nào.";
      } else {
        list.innerHTML = active
          .map((s) => `- ${s.subjectName} (${s.level || "N/A"} | ${s.phase || "NOT_STARTED"})`)
          .join("<br/>");
      }
      if (available.length) {
        list.innerHTML += `<div style="margin-top:6px;color:#64748b;">Co the dang ky: ${available.map((x) => x.subjectName).join(", ")}</div>`;
      }
    } catch (e) {
      const list = profileMenu.querySelector("#profile-menu-subs-list");
      if (list) list.textContent = "Không tải được danh sách gói đăng ký.";
    }
  };

  checkSession().then((ok) => {
    if (!ok) {
      subscribedSubjectIds = new Set();
      setJoinProgramButtons(new Set(), false);
      if (notifWrap) notifWrap.classList.add("hidden");
      if (loginLink) {
        loginLink.textContent = "Login";
        loginLink.className = "hidden text-sm font-medium text-slate-600 hover:text-primary md:block";
        loginLink.onclick = null;
      }
      if (getStartedTop) {
        getStartedTop.textContent = "Get Started";
        getStartedTop.onclick = scrollToSubjects;
      }
      closeProfileMenu();
      return;
    }
    const { user } = getAuth();
    const activeSubjectIds = new Set();
    if (notifWrap) notifWrap.classList.remove("hidden");
    loadUnreadCount();
    api("/api/me/subscriptions", "GET", null, true)
      .then((subData) => {
        const active = (subData && subData.activeSubscriptions) || [];
        active.forEach((s) => activeSubjectIds.add(Number(s.subjectId)));
        subscribedSubjectIds = new Set(activeSubjectIds);
      })
      .catch(() => {})
      .finally(() => setJoinProgramButtons(activeSubjectIds, true));
    if (loginLink) {
      const base = String((user && user.fullName) || (user && user.email) || "U").trim();
      loginLink.textContent = base ? base.charAt(0).toUpperCase() : "U";
      loginLink.className =
        "flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-700 text-sm font-bold ring-1 ring-slate-200 hover:bg-slate-200";
      loginLink.title = "Profile";
      loginLink.onclick = async (e) => {
        e.preventDefault();
        if (profileMenu && profileMenu.style.display === "block") {
          closeProfileMenu();
          return;
        }
        await ensureProfileMenu(user);
        showProfileMenu();
      };
    }
    if (getStartedTop) {
      getStartedTop.textContent =
        String((user && user.role) || "USER").toUpperCase() === "ADMIN" ? "Admin Dashboard" : "Dashboard";
      getStartedTop.onclick = () => {
        if (String((user && user.role) || "USER").toUpperCase() === "ADMIN") {
          nav("/admin-dashboard", "admin/adminDashboard.html");
          return;
        }
        nav("/dashboard", "dashboard.html");
      };
    }
  });

  const scrollToSubjects = () => {
    const section = document.getElementById("master-core-subjects");
    if (section) {
      section.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  const navByPlacementStatus = async (subjectId) => {
    localStorage.setItem("compassed_subject_id", String(subjectId));
    const gradeKey = `compassed_grade_level_${subjectId}`;
    const gradeLevel = Number(localStorage.getItem(gradeKey) || 10);
    try {
      const rows = await api("/api/history/placements", "GET", null, true);
      const hasPlacement = Array.isArray(rows) && rows.length > 0;
      if (hasPlacement) {
        toast("Bạn đã làm placement. Chuyển đến roadmap.");
        nav("/roadmap-dashboard", "roadmapDashboard.html");
        return;
      }
    } catch (e) {
      // Fallback to placement flow if history endpoint cannot be loaded.
    }
    nav(`/placement-test?subjectId=${subjectId}&grade=${gradeLevel}`, `placementTest.html?subjectId=${subjectId}&grade=${gradeLevel}`);
  };

  ["landing-start-learning", "landing-get-started-bottom"].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.addEventListener("click", scrollToSubjects);
  });

  document.querySelectorAll(".js-join-program").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const subjectId = Number(btn.getAttribute("data-subject-id")) || getSubjectId();
      const ok = await checkSession();
      if (!ok) {
        toast(t("needAuth") || "Please login first", "warn");
        goAuthWithRedirect(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
        return;
      }
      if (subscribedSubjectIds.has(subjectId)) {
        localStorage.setItem("compassed_subject_id", String(subjectId));
        nav("/roadmap-dashboard", "roadmapDashboard.html");
        return;
      }
      await navByPlacementStatus(subjectId);
    });
  });

  if (loginLink) {
    loginLink.addEventListener("click", (e) => {
      if (!loginLink.className.includes("rounded-full")) {
        e.preventDefault();
        nav("/auth", "auth.html");
      }
    });
    document.addEventListener("click", (e) => {
      if (!profileMenu) return;
      if (e.target === loginLink || profileMenu.contains(e.target)) return;
      closeProfileMenu();
    });
    window.addEventListener("scroll", closeProfileMenu);
    window.addEventListener("resize", closeProfileMenu);
  }

  if (notifBtn && notifPanel) {
    notifBtn.addEventListener("click", async (e) => {
      e.preventDefault();
      e.stopPropagation();
      closeProfileMenu();
      notifOpen = !notifOpen;
      notifPanel.classList.toggle("hidden", !notifOpen);
      if (notifOpen) {
        await loadNotifications();
      }
    });
  }

  document.addEventListener("click", (e) => {
    if (!notifOpen || !notifPanel || !notifBtn) return;
    if (notifPanel.contains(e.target) || notifBtn.contains(e.target)) return;
    closeNotifPanel();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeNotifPanel();
  });
  window.addEventListener("scroll", closeNotifPanel);
  window.addEventListener("resize", closeNotifPanel);

  const viewSyllabus = document.getElementById("landing-view-syllabus");
  if (viewSyllabus) {
    viewSyllabus.addEventListener("click", () => {
      nav("/roadmap-dashboard", "roadmapDashboard.html");
    });
  }

  const contactSales = document.getElementById("landing-contact-sales");
  if (contactSales) {
    contactSales.addEventListener("click", () => {
      window.location.href = "mailto:sales@compassed.local?subject=CompassED%20Consultation";
    });
  }
}

export { initLanding };
