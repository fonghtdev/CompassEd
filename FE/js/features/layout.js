import { api, checkSession, clearAuth, getAuth, nav, pageName } from "./core.js";

function initSharedLayout() {
  const page = pageName();
  const enabledPages = new Set(["placement", "result", "checkout", "profile"]);
  if (!enabledPages.has(page)) return;

  if (!document.querySelector('link[href*="Material+Symbols+Outlined"]')) {
    const iconFont = document.createElement("link");
    iconFont.rel = "stylesheet";
    iconFont.href =
      "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap";
    document.head.appendChild(iconFont);
  }

  const nativeHeader = document.querySelector("header");
  if (nativeHeader) nativeHeader.style.display = "none";
  const nativeFooter = document.querySelector("footer");
  if (nativeFooter) nativeFooter.style.display = "none";

  if (!document.getElementById("shared-global-header")) {
    const wrap = document.createElement("div");
    wrap.id = "shared-global-header";
    wrap.innerHTML = `
      <header class="flex w-full items-center justify-between border-b border-slate-200 bg-white px-6 py-4 lg:px-20 sticky top-0 z-40">
        <a id="shared-brand-link" class="flex items-center gap-4 cursor-pointer" href="/landing" style="text-decoration:none;color:inherit;">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <span class="material-symbols-outlined text-3xl">explore</span>
          </div>
          <h2 class="text-xl font-bold tracking-tight text-slate-900">CompassED</h2>
        </a>
        <nav class="hidden items-center gap-8 md:flex">
          <a id="shared-home-link" class="text-sm font-medium text-slate-600 hover:text-blue-600 transition-colors" href="/landing">Home</a>
          <a id="shared-dashboard-link" class="text-sm font-medium text-slate-600 hover:text-blue-600 transition-colors" href="/dashboard">Dashboard</a>
          <a id="shared-roadmap-link" class="text-sm font-medium text-slate-600 hover:text-blue-600 transition-colors" href="/learning-roadmap">Roadmap</a>
          <a id="shared-history-link" class="text-sm font-medium text-slate-600 hover:text-blue-600 transition-colors" href="/history">History</a>
        </nav>
        <div class="flex items-center gap-4">
          <a id="shared-login-link" class="hidden text-sm font-medium text-slate-600 hover:text-blue-600 md:block" href="/auth">Login</a>
          <button id="shared-primary-btn" class="flex h-10 items-center justify-center rounded-lg bg-blue-600 px-5 text-sm font-bold text-white transition-colors hover:bg-blue-700">Get Started</button>
        </div>
      </header>`;
    document.body.prepend(wrap);
  }

  if (!document.getElementById("shared-global-footer")) {
    const footer = document.createElement("footer");
    footer.id = "shared-global-footer";
    footer.className = "border-t border-slate-200 bg-white py-10 mt-12";
    footer.innerHTML = `
      <div class="mx-auto w-full max-w-[1280px] px-6 lg:px-20 text-center text-sm text-slate-500">
        Â© 2026 CompassED Learning. All rights reserved.
      </div>`;
    document.body.appendChild(footer);
  }

  const loginLink = document.getElementById("shared-login-link");
  const primaryBtn = document.getElementById("shared-primary-btn");
  const brandLink = document.getElementById("shared-brand-link");
  const navHome = document.getElementById("shared-home-link");
  const navDashboard = document.getElementById("shared-dashboard-link");
  const navRoadmap = document.getElementById("shared-roadmap-link");
  const navHistory = document.getElementById("shared-history-link");
  let profileMenu = null;
  let popupOverlay = null;

  if (brandLink) {
    brandLink.onclick = (e) => {
      e.preventDefault();
      nav("/landing", "landingPage.html");
    };
  }

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
      <div style="width:min(560px,92vw);background:#fff;border-radius:14px;border:1px solid #e2e8f0;box-shadow:0 18px 40px rgba(2,6,23,0.2);">
        <div id="shared-popup-content"></div>
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

  const openProfilePopup = async () => {
    const overlay = ensurePopupOverlay();
    const content = overlay.querySelector("#shared-popup-content");
    content.innerHTML = `<div style="padding:20px 18px;">Loading...</div>`;
    overlay.style.display = "flex";
    const profile = await api("/api/me/profile", "GET", null, true);
    content.innerHTML = `
      <div style="padding:18px 18px 12px 18px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;">
        <div style="font-size:18px;font-weight:800;color:#0f172a;">Profile</div>
        <button id="shared-profile-popup-close" style="border:none;background:#f8fafc;border-radius:8px;padding:6px 10px;cursor:pointer;">X</button>
      </div>
      <div style="padding:16px 18px;display:grid;gap:10px;">
        <label style="font-size:12px;font-weight:700;color:#334155;">Email</label>
        <input disabled value="${profile.email || ""}" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;background:#f8fafc;" />
        <label style="font-size:12px;font-weight:700;color:#334155;">Full Name</label>
        <input id="shared-profile-name" value="${profile.fullName || ""}" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
        <label style="font-size:12px;font-weight:700;color:#334155;">Learning Goal</label>
        <textarea id="shared-profile-goal" rows="3" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;">${profile.learningGoal || ""}</textarea>
        <label style="font-size:12px;font-weight:700;color:#334155;">Target Score</label>
        <input id="shared-profile-target" type="number" min="0" max="100" value="${profile.targetScore || 75}" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
        <button id="shared-profile-save" style="margin-top:6px;padding:10px 12px;border:none;border-radius:8px;background:#0d7ff2;color:#fff;font-weight:700;cursor:pointer;">Save Profile</button>
      </div>`;
    content.querySelector("#shared-profile-popup-close").addEventListener("click", closePopup);
    content.querySelector("#shared-profile-save").addEventListener("click", async () => {
      const fullName = content.querySelector("#shared-profile-name").value.trim();
      const learningGoal = content.querySelector("#shared-profile-goal").value.trim();
      const targetScore = Number(content.querySelector("#shared-profile-target").value || 75);
      const updated = await api("/api/me/profile", "PUT", { fullName, learningGoal, targetScore }, true);
      const userRaw = localStorage.getItem("compassed_auth_user");
      const user = userRaw ? JSON.parse(userRaw) : null;
      if (user) {
        user.fullName = updated.fullName || user.fullName;
        localStorage.setItem("compassed_auth_user", JSON.stringify(user));
      }
      if (loginLink) loginLink.textContent = String(updated.fullName || updated.email || "U").charAt(0).toUpperCase();
      closePopup();
    });
  };

  const openSettingPopup = async () => {
    const overlay = ensurePopupOverlay();
    const content = overlay.querySelector("#shared-popup-content");
    content.innerHTML = `<div style="padding:20px 18px;">Loading...</div>`;
    overlay.style.display = "flex";
    const profile = await api("/api/me/profile", "GET", null, true);
    content.innerHTML = `
      <div style="padding:18px 18px 12px 18px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;">
        <div style="font-size:18px;font-weight:800;color:#0f172a;">Setting</div>
        <button id="shared-setting-popup-close" style="border:none;background:#f8fafc;border-radius:8px;padding:6px 10px;cursor:pointer;">X</button>
      </div>
      <div style="padding:16px 18px;display:grid;gap:12px;">
        <label style="display:flex;align-items:center;gap:8px;">
          <input id="shared-setting-email" type="checkbox" ${profile.notifyEmail ? "checked" : ""}/>
          <span style="font-size:13px;color:#334155;">Email notification</span>
        </label>
        <label style="display:flex;align-items:center;gap:8px;">
          <input id="shared-setting-inapp" type="checkbox" ${profile.notifyInApp ? "checked" : ""}/>
          <span style="font-size:13px;color:#334155;">In-app notification</span>
        </label>
        <button id="shared-setting-save" style="margin-top:6px;padding:10px 12px;border:none;border-radius:8px;background:#0d7ff2;color:#fff;font-weight:700;cursor:pointer;">Save Settings</button>
      </div>`;
    content.querySelector("#shared-setting-popup-close").addEventListener("click", closePopup);
    content.querySelector("#shared-setting-save").addEventListener("click", async () => {
      const notifyEmail = !!content.querySelector("#shared-setting-email").checked;
      const notifyInApp = !!content.querySelector("#shared-setting-inapp").checked;
      await api("/api/me/profile", "PUT", { notifyEmail, notifyInApp }, true);
      closePopup();
    });
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
          <button id="shared-menu-profile" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Profile</button>
          <button id="shared-menu-setting" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Setting</button>
          <div style="padding:8px 10px;border-radius:8px;border:1px dashed #cbd5e1;background:#f8fafc;">
            <div style="font-size:12px;font-weight:700;color:#0f172a;">Subscription</div>
            <div id="shared-menu-subs" style="margin-top:6px;font-size:12px;color:#334155;">Loading...</div>
            <button id="shared-menu-subscribe-more" style="margin-top:8px;padding:6px 8px;border-radius:6px;background:#0d7ff2;color:white;font-size:12px;font-weight:700;">Đăng ký thêm</button>
          </div>
          <button id="shared-menu-logout" style="display:flex;align-items:center;gap:8px;text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;color:#b91c1c;background:#fef2f2;">
            <span class="material-symbols-outlined" style="font-size:18px;">logout</span>
            <span>Logout</span>
          </button>
        </div>`;
      document.body.appendChild(profileMenu);

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
      const profileBtn = profileMenu.querySelector("#shared-menu-profile");
      const settingBtn = profileMenu.querySelector("#shared-menu-setting");
      const subscribeMoreBtn = profileMenu.querySelector("#shared-menu-subscribe-more");
      const logoutBtn = profileMenu.querySelector("#shared-menu-logout");
      const subsCard = profileMenu.querySelector("#shared-menu-subs")?.parentElement;
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

    const subData = await api("/api/me/subscriptions", "GET", null, true);
    const active = (subData && subData.activeSubscriptions) || [];
    const available = (subData && subData.availableSubjects) || [];
    const list = profileMenu.querySelector("#shared-menu-subs");
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
  };

  checkSession().then(async (ok) => {
    if (!ok) {
      loginLink.textContent = "Login";
      loginLink.className = "hidden text-sm font-medium text-slate-600 hover:text-blue-600 md:block";
      loginLink.onclick = null;
      primaryBtn.textContent = "Get Started";
      primaryBtn.onclick = () => nav("/landing", "landingPage.html");
      return;
    }

    const { user } = getAuth();
    loginLink.textContent = String((user && user.fullName) || (user && user.email) || "U").charAt(0).toUpperCase();
    loginLink.className =
      "flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-700 text-sm font-bold ring-1 ring-slate-200 hover:bg-slate-200";
    loginLink.onclick = async (e) => {
      e.preventDefault();
      if (profileMenu && profileMenu.style.display === "block") {
        closeProfileMenu();
        return;
      }
      await ensureProfileMenu(user);
      showProfileMenu();
    };

    const role = String((user && user.role) || "USER").toUpperCase();
    if (navDashboard) {
      navDashboard.onclick = (e) => {
        e.preventDefault();
        if (role === "ADMIN") {
          nav("/admin-dashboard", "admin/adminDashboard.html");
          return;
        }
        nav("/dashboard", "dashboard.html");
      };
    }
    if (navHome) {
      navHome.onclick = (e) => {
        e.preventDefault();
        nav("/landing", "landingPage.html");
      };
    }
    if (navRoadmap) {
      navRoadmap.onclick = (e) => {
        e.preventDefault();
        if (role === "ADMIN") {
          nav("/admin-dashboard", "admin/adminDashboard.html");
          return;
        }
        nav("/learning-roadmap", "coursesDetail.html");
      };
    }
    if (navHistory) {
      navHistory.onclick = (e) => {
        e.preventDefault();
        if (role === "ADMIN") {
          nav("/admin-dashboard", "admin/adminDashboard.html");
          return;
        }
        nav("/history", "history.html");
      };
    }
    primaryBtn.textContent = role === "ADMIN" ? "Admin Dashboard" : "Dashboard";
    primaryBtn.onclick = () => {
      if (role === "ADMIN") {
        nav("/admin-dashboard", "admin/adminDashboard.html");
        return;
      }
      nav("/dashboard", "dashboard.html");
    };
  });

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

export { initSharedLayout };
