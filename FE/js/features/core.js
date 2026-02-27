const API_BASE = (window.APP_CONFIG && window.APP_CONFIG.API_BASE) || "http://localhost:8080";
const GOOGLE_CLIENT_ID = (window.APP_CONFIG && window.APP_CONFIG.GOOGLE_CLIENT_ID) || "";

const KEYS = {
  token: "compassed_auth_token",
  user: "compassed_auth_user",
  subjectId: "compassed_subject_id",
  attemptId: "compassed_attempt_id",
  paper: "compassed_paper_json",
  answers: "compassed_answers_json",
  result: "compassed_result_json",
  subscription: "compassed_subscription_json",
  language: "compassed_language"
};

function pageName() {
  const p = window.location.pathname.toLowerCase();
  if (p.endsWith("/landing") || p.endsWith("/")) return "landing";
  if (p.includes("landingpage.html")) return "landing";
  if (p.endsWith("/auth") || p.includes("auth.html")) return "auth";
  if (p.endsWith("/placement-test") || p.includes("placementtest.html")) return "placement";
  if (p.endsWith("/placement-result") || p.includes("placementtestresult.html")) return "result";
  if (p.endsWith("/checkout") || p.includes("checkout.html")) return "checkout";
  if (p.endsWith("/learning-roadmap") || p.includes("coursesdetail.html")) return "roadmap";
  if (p.endsWith("/history") || p.includes("history.html")) return "history";
  if (p.endsWith("/dashboard") || p.includes("dashboard.html")) return "dashboard";
  if (p.endsWith("/profile") || p.includes("profile.html")) return "profile";
  if (p.endsWith("/admin-dashboard") || p.includes("admindashboard.html")) return "admin";
  return "";
}

function nav(routePath, fileName) {
  if (window.location.protocol === "file:") {
    window.location.href = fileName;
    return;
  }
  window.location.href = routePath;
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el && value != null) el.textContent = value;
}

function formatVnd(amount) {
  return Number(amount || 0).toLocaleString("vi-VN") + " VND";
}

function toast(message, kind) {
  let wrap = document.getElementById("app-toast-wrap");
  if (!wrap) {
    wrap = document.createElement("div");
    wrap.id = "app-toast-wrap";
    wrap.style.position = "fixed";
    wrap.style.top = "16px";
    wrap.style.right = "16px";
    wrap.style.zIndex = "9999";
    wrap.style.display = "grid";
    wrap.style.gap = "8px";
    document.body.appendChild(wrap);
  }
  const item = document.createElement("div");
  item.textContent = message;
  item.style.padding = "10px 14px";
  item.style.borderRadius = "10px";
  item.style.color = "white";
  item.style.fontSize = "14px";
  item.style.fontWeight = "600";
  item.style.background = kind === "error" ? "#dc2626" : kind === "warn" ? "#d97706" : "#2563eb";
  wrap.appendChild(item);
  setTimeout(() => item.remove(), 3000);
}

function showLoading(text) {
  let el = document.getElementById("app-loading");
  if (!el) {
    el = document.createElement("div");
    el.id = "app-loading";
    el.style.position = "fixed";
    el.style.inset = "0";
    el.style.background = "rgba(15,23,42,0.35)";
    el.style.zIndex = "9998";
    el.style.display = "flex";
    el.style.alignItems = "center";
    el.style.justifyContent = "center";
    el.innerHTML = '<div style="background:white;padding:12px 16px;border-radius:10px;font-weight:700;color:#0f172a">Loading...</div>';
    document.body.appendChild(el);
  }
  el.querySelector("div").textContent = text || "Loading...";
  el.style.display = "flex";
}

function hideLoading() {
  const el = document.getElementById("app-loading");
  if (el) el.style.display = "none";
}

function getAuth() {
  const token = localStorage.getItem(KEYS.token);
  const user = JSON.parse(localStorage.getItem(KEYS.user) || "null");
  return { token, user };
}

function currentRole() {
  const { user } = getAuth();
  return String((user && user.role) || "USER").toUpperCase();
}

function saveAuth(resp) {
  localStorage.setItem(KEYS.token, resp.token);
  localStorage.setItem(KEYS.user, JSON.stringify(resp.user));
}

function clearAuth() {
  localStorage.removeItem(KEYS.token);
  localStorage.removeItem(KEYS.user);
}

async function api(path, method, body, needAuth) {
  const { token, user } = getAuth();
  const headers = { "Content-Type": "application/json" };
  if (needAuth) {
    if (!token || !user) throw new Error("Not authenticated");
    headers.Authorization = `Bearer ${token}`;
  }
  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    });
  } catch (e) {
    throw new Error("Cannot connect backend: http://localhost:8080");
  }
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      msg = await res.text();
    } catch (e) {}
    throw new Error(msg);
  }
  const raw = await res.text();
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (e) {
    return raw;
  }
}

async function checkSession() {
  const { token } = getAuth();
  if (!token) return false;
  try {
    const me = await api("/api/auth/me", "GET", null, true);
    localStorage.setItem(KEYS.user, JSON.stringify(me));
    return true;
  } catch (e) {
    clearAuth();
    return false;
  }
}

function getSubjectId() {
  const q = new URLSearchParams(window.location.search);
  const fromUrl = Number(q.get("subjectId"));
  if (fromUrl) {
    localStorage.setItem(KEYS.subjectId, String(fromUrl));
    return fromUrl;
  }
  return Number(localStorage.getItem(KEYS.subjectId) || 1);
}

function goAuthWithRedirect(routeAfter, fileAfter) {
  localStorage.setItem("compassed_after_auth_route", routeAfter);
  localStorage.setItem("compassed_after_auth_file", fileAfter);
  nav("/auth", "auth.html");
}

function redirectAfterAuthDefault() {
  const { user } = getAuth();
  const role = String((user && user.role) || "USER").toUpperCase();
  if (role === "ADMIN") {
    localStorage.removeItem("compassed_after_auth_route");
    localStorage.removeItem("compassed_after_auth_file");
    nav("/admin-dashboard", "admin/adminDashboard.html");
    return;
  }
  const route = localStorage.getItem("compassed_after_auth_route") || "/landing";
  const file = localStorage.getItem("compassed_after_auth_file") || "landingPage.html";
  localStorage.removeItem("compassed_after_auth_route");
  localStorage.removeItem("compassed_after_auth_file");
  nav(route, file);
}

export {
  API_BASE,
  GOOGLE_CLIENT_ID,
  KEYS,
  pageName,
  nav,
  setText,
  formatVnd,
  toast,
  showLoading,
  hideLoading,
  getAuth,
  saveAuth,
  clearAuth,
  api,
  checkSession,
  currentRole,
  getSubjectId,
  goAuthWithRedirect,
  redirectAfterAuthDefault
};
