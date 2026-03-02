import {
  api,
  checkSession,
  GOOGLE_CLIENT_ID,
  hideLoading,
  nav,
  currentRole,
  redirectAfterAuthDefault,
  saveAuth,
  showLoading,
  toast
} from "./core.js";

function onboardingKey(user) {
  if (!user) return "compassed_onboarding_done_unknown";
  const base = user.id != null ? `id_${user.id}` : `email_${String(user.email || "unknown").toLowerCase()}`;
  return `compassed_placement_onboarding_done_${base}`;
}

function clearAfterAuthRedirect() {
  localStorage.removeItem("compassed_after_auth_route");
  localStorage.removeItem("compassed_after_auth_file");
}

function ensureChoiceModal() {
  let overlay = document.getElementById("auth-choice-overlay");
  if (overlay) return overlay;
  overlay = document.createElement("div");
  overlay.id = "auth-choice-overlay";
  overlay.style.position = "fixed";
  overlay.style.inset = "0";
  overlay.style.background = "rgba(15,23,42,0.45)";
  overlay.style.display = "none";
  overlay.style.alignItems = "center";
  overlay.style.justifyContent = "center";
  overlay.style.zIndex = "9999";
  overlay.innerHTML = `
    <div style="width:min(520px,92vw);background:#fff;border-radius:14px;border:1px solid #e2e8f0;box-shadow:0 18px 40px rgba(2,6,23,0.25);overflow:hidden;">
      <div id="auth-choice-content" style="padding:18px;"></div>
    </div>`;
  document.body.appendChild(overlay);
  return overlay;
}

function openChoiceModal(render) {
  const overlay = ensureChoiceModal();
  const content = overlay.querySelector("#auth-choice-content");
  overlay.style.display = "flex";
  render(content, () => {
    overlay.style.display = "none";
  });
}

function askPlacementChoice() {
  return new Promise((resolve) => {
    openChoiceModal((content, close) => {
      content.innerHTML = `
        <h3 style="font-size:20px;font-weight:800;color:#0f172a;margin:0;">Làm placement test ngay?</h3>
        <p style="margin:8px 0 14px 0;color:#475569;font-size:14px;">Bạn có muốn tham gia làm bài mini-test đầu vào để hệ thống tạo roadmap phù hợp cho bạn không?</p>
        <div style="display:flex;gap:10px;justify-content:flex-end;">
          <button id="auth-choice-no" type="button" style="padding:9px 14px;border:1px solid #e2e8f0;background:#fff;border-radius:8px;font-weight:700;color:#334155;cursor:pointer;">Chưa, về trang chủ</button>
          <button id="auth-choice-yes" type="button" style="padding:9px 14px;border:none;background:#2563eb;color:#fff;border-radius:8px;font-weight:700;cursor:pointer;">Có</button>
        </div>`;
      content.querySelector("#auth-choice-no").addEventListener("click", () => {
        close();
        resolve(false);
      });
      content.querySelector("#auth-choice-yes").addEventListener("click", () => {
        close();
        resolve(true);
      });
    });
  });
}

function chooseSubjectForPlacement(subjects) {
  return new Promise((resolve) => {
    openChoiceModal((content, close) => {
      const rows = (subjects || [])
        .map(
          (s) => `
            <button type="button" data-subject-id="${s.id}" style="text-align:left;padding:10px 12px;border:1px solid #e2e8f0;border-radius:8px;background:#fff;cursor:pointer;">
              <div style="font-weight:700;color:#0f172a;">${s.name}</div>
              <div style="font-size:12px;color:#64748b;">${s.code || ""}</div>
            </button>`
        )
        .join("");
      content.innerHTML = `
        <h3 style="font-size:20px;font-weight:800;color:#0f172a;margin:0;">Bạn muốn bắt đầu học môn nào?</h3>
        <p style="margin:8px 0 14px 0;color:#475569;font-size:14px;">Chọn 1 môn bạn muốn bắt đầu trước.</p>
        <div id="auth-subject-list" style="display:grid;gap:8px;max-height:280px;overflow:auto;">${rows}</div>
        <div style="display:flex;justify-content:flex-end;margin-top:12px;">
          <button id="auth-subject-cancel" type="button" style="padding:9px 14px;border:1px solid #e2e8f0;background:#fff;border-radius:8px;font-weight:700;color:#334155;cursor:pointer;">Để sau</button>
        </div>`;
      content.querySelector("#auth-subject-cancel").addEventListener("click", () => {
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

function resolvePlacementSubjectId() {
  const stored = Number(localStorage.getItem("compassed_subject_id"));
  if (stored) return stored;
  return 1;
}

function resolvePlacementGrade(subjectId) {
  const key = `compassed_grade_level_${subjectId}`;
  return Number(localStorage.getItem(key) || 10);
}

function askVerificationCode(email) {
  return new Promise((resolve) => {
    openChoiceModal((content, close) => {
      content.innerHTML = `
        <h3 style="font-size:20px;font-weight:800;color:#0f172a;margin:0;">Xac nhan email</h3>
        <p style="margin:8px 0 14px 0;color:#475569;font-size:14px;">Nhap ma 6 so da gui toi <b>${email}</b>.</p>
        <input id="auth-verify-code" type="text" maxlength="8" placeholder="Nhap ma xac nhan" style="width:100%;padding:10px 12px;border:1px solid #cbd5e1;border-radius:8px;outline:none;" />
        <div style="display:flex;gap:10px;justify-content:flex-end;margin-top:12px;">
          <button id="auth-verify-cancel" type="button" style="padding:9px 14px;border:1px solid #e2e8f0;background:#fff;border-radius:8px;font-weight:700;color:#334155;cursor:pointer;">Huy</button>
          <button id="auth-verify-confirm" type="button" style="padding:9px 14px;border:none;background:#2563eb;color:#fff;border-radius:8px;font-weight:700;cursor:pointer;">Xac nhan</button>
        </div>`;
      content.querySelector("#auth-verify-cancel").addEventListener("click", () => {
        close();
        resolve(null);
      });
      content.querySelector("#auth-verify-confirm").addEventListener("click", () => {
        const code = (content.querySelector("#auth-verify-code").value || "").trim();
        if (!code) return;
        close();
        resolve(code);
      });
    });
  });
}

async function runPlacementOnboarding(user) {
  const wantsPlacement = await askPlacementChoice();
  if (!wantsPlacement) {
    localStorage.setItem(onboardingKey(user), "1");
    clearAfterAuthRedirect();
    nav("/landing", "landingPage.html");
    return;
  }

  let subjectId = resolvePlacementSubjectId();
  try {
    const subjects = await api("/api/subjects", "GET", null, false);
    if (Array.isArray(subjects) && subjects.length > 0) {
      const selected = await chooseSubjectForPlacement(subjects);
      if (!selected) {
        localStorage.setItem(onboardingKey(user), "1");
        clearAfterAuthRedirect();
        nav("/landing", "landingPage.html");
        return;
      }
      subjectId = Number(selected);
    }
  } catch (e) {
    // keep fallback subject id
  }

  localStorage.setItem("compassed_subject_id", String(subjectId));
  localStorage.setItem(onboardingKey(user), "1");
  const gradeLevel = resolvePlacementGrade(subjectId);
  clearAfterAuthRedirect();
  nav(`/placement-test?subjectId=${subjectId}&grade=${gradeLevel}`, `placementTest.html?subjectId=${subjectId}&grade=${gradeLevel}`);
}

async function handlePostAuthNavigation(user) {
  if (currentRole() === "ADMIN") {
    redirectAfterAuthDefault();
    return;
  }
  const key = onboardingKey(user);
  const done = localStorage.getItem(key) === "1";
  if (!done) {
    await runPlacementOnboarding(user);
    return;
  }
  redirectAfterAuthDefault();
}

function setupAuthTabs() {
  const tabLogin = document.getElementById("tab-login");
  const tabRegister = document.getElementById("tab-register");
  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");
  if (!tabLogin || !tabRegister || !loginForm || !registerForm) return;

  const showLogin = () => {
    tabLogin.className = "px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold";
    tabRegister.className = "px-4 py-2 rounded-lg bg-slate-100 text-slate-700 font-semibold";
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");
  };
  const showRegister = () => {
    tabRegister.className = "px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold";
    tabLogin.className = "px-4 py-2 rounded-lg bg-slate-100 text-slate-700 font-semibold";
    registerForm.classList.remove("hidden");
    loginForm.classList.add("hidden");
  };
  tabLogin.addEventListener("click", showLogin);
  tabRegister.addEventListener("click", showRegister);
}

function initAuth() {
  checkSession().then((ok) => {
    if (ok) redirectAfterAuthDefault();
  });
  setupAuthTabs();

  const loginForm = document.getElementById("login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      showLoading("Signing in...");
      try {
        const email = document.getElementById("login-email").value.trim();
        const password = document.getElementById("login-password").value;
        const resp = await api("/api/auth/login", "POST", { email, password }, false);
        saveAuth(resp);
        toast("Login successful");
        await handlePostAuthNavigation(resp.user);
      } catch (err) {
        toast(`Login failed: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });
  }

  const registerForm = document.getElementById("register-form");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      showLoading("Sending verification code...");
      try {
        const fullName = document.getElementById("register-name").value.trim();
        const email = document.getElementById("register-email").value.trim();
        const password = document.getElementById("register-password").value;
        const requestCodeResp = await api("/api/auth/register/request-code", "POST", { fullName, email, password }, false);
        toast((requestCodeResp && requestCodeResp.message) || "Da gui ma xac nhan qua email");
        hideLoading();
        const code = await askVerificationCode(email);
        if (!code) {
          toast("Registration cancelled", "warn");
          return;
        }
        showLoading("Verifying code...");
        const resp = await api("/api/auth/register/verify", "POST", { email, code }, false);
        saveAuth(resp);
        toast("Register successful");
        localStorage.removeItem(onboardingKey(resp.user));
        await handlePostAuthNavigation(resp.user);
      } catch (err) {
        toast(`Register failed: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });
  }

  const githubBtn = document.getElementById("github-mock-btn");
  if (githubBtn) {
    githubBtn.addEventListener("click", async () => {
      const email = prompt("Enter GitHub email (dev mode)");
      if (!email) return;
      showLoading("Signing in via GitHub...");
      try {
        const fullName = email.split("@")[0];
        const resp = await api("/api/auth/oauth/mock", "POST", { provider: "github", email, fullName }, false);
        saveAuth(resp);
        toast("GitHub login successful");
        await handlePostAuthNavigation(resp.user);
      } catch (err) {
        toast(`GitHub login failed: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });
  }

  if (window.google && GOOGLE_CLIENT_ID) {
    window.google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: async (response) => {
        showLoading("Verifying Google...");
        try {
          const resp = await api("/api/auth/oauth/google", "POST", { idToken: response.credential }, false);
          saveAuth(resp);
          toast("Google login successful");
          await handlePostAuthNavigation(resp.user);
        } catch (err) {
          toast(`Google login failed: ${err.message}`, "error");
        } finally {
          hideLoading();
        }
      }
    });
    const container = document.getElementById("google-signin-container");
    if (container) {
      window.google.accounts.id.renderButton(container, { theme: "outline", size: "large", width: 320 });
    }
  }
}

export { initAuth };
