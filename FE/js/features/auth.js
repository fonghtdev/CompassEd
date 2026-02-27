import {
  api,
  checkSession,
  GOOGLE_CLIENT_ID,
  hideLoading,
  redirectAfterAuthDefault,
  saveAuth,
  showLoading,
  toast
} from "./core.js";

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
        redirectAfterAuthDefault();
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
      showLoading("Creating account...");
      try {
        const fullName = document.getElementById("register-name").value.trim();
        const email = document.getElementById("register-email").value.trim();
        const password = document.getElementById("register-password").value;
        const resp = await api("/api/auth/register", "POST", { fullName, email, password }, false);
        saveAuth(resp);
        toast("Register successful");
        redirectAfterAuthDefault();
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
        redirectAfterAuthDefault();
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
          redirectAfterAuthDefault();
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
