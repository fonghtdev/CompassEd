import { KEYS, setText } from "./core.js";

const I18N = {
  vi: {
    switchLabel: "Ng\u00f4n ng\u1eef",
    login: "\u0110\u0103ng nh\u1eadp",
    register: "\u0110\u0103ng k\u00fd",
    getStarted: "B\u1eaft \u0111\u1ea7u",
    startLearningNow: "B\u1eaft \u0111\u1ea7u h\u1ecdc ngay",
    getStartedFree: "B\u1eaft \u0111\u1ea7u mi\u1ec5n ph\u00ed",
    joinProgram: "Tham gia ch\u01b0\u01a1ng tr\u00ecnh",
    choosePlan: "Ch\u1ecdn m\u00f4n h\u1ecdc",
    choosePlanSub: "Ch\u1ecdn m\u00f4n h\u1ecdc ph\u00f9 h\u1ee3p \u0111\u1ec3 b\u1eaft \u0111\u1ea7u l\u1ed9 tr\u00ecnh c\u00e1 nh\u00e2n h\u00f3a.",
    needAuth: "B\u1ea1n c\u1ea7n \u0111\u0103ng nh\u1eadp ho\u1eb7c \u0111\u0103ng k\u00fd tr\u01b0\u1edbc khi b\u1eaft \u0111\u1ea7u",
    checkoutTitle: "Thanh to\u00e1n",
    checkoutSubtitle: "Ch\u1ecdn m\u00f4n mu\u1ed1n m\u1edf roadmap.",
    checkoutTotal: "T\u1ed5ng thanh to\u00e1n",
    checkoutBack: "Quay l\u1ea1i",
    checkoutPay: "Thanh to\u00e1n v\u00e0 m\u1edf roadmap",
    dashboard: "Dashboard",
    history: "L\u1ecbch s\u1eed",
    roadmap: "Roadmap",
    profile: "H\u1ed3 s\u01a1",
    logout: "\u0110\u0103ng xu\u1ea5t",
    setting: "C\u00e0i \u0111\u1eb7t",
    studentPortal: "C\u1ed5ng h\u1ecdc vi\u00ean",
    notificationTitle: "Th\u00f4ng b\u00e1o",
    notificationLoading: "\u0110ang t\u1ea3i th\u00f4ng b\u00e1o...",
    authIntro: "\u0110\u0103ng nh\u1eadp \u0111\u1ec3 \u0111\u1ed3ng b\u1ed9 placement, roadmap v\u00e0 l\u1ecbch s\u1eed h\u1ecdc t\u1eadp.",
    authBullet1: "Placement test v\u1edbi k\u1ebft qu\u1ea3 t\u1eeb BE local",
    authBullet2: "M\u1edf kh\u00f3a roadmap t\u1eeb checkout local",
    authBullet3: "L\u1ecbch s\u1eed c\u00e1c l\u1ea7n l\u00e0m b\u00e0i",
    continueWith: "HO\u1eb6C TI\u1ebeP T\u1ee4C V\u1edaI",
    googleNote: "Google login c\u1ea7n thi\u1ebft l\u1eadp",
    dashboardTitle: "B\u1ea3ng \u0111i\u1ec1u khi\u1ec3n h\u1ecdc t\u1eadp",
    historyTitle: "L\u1ecbch s\u1eed Placement",
    roadmapTitle: "L\u1ed9 tr\u00ecnh h\u1ecdc t\u1eadp",
    roadmapDashboardTitle: "B\u1ea3ng ch\u1ecdn roadmap",
    roadmapDashboardSectionTitle: "Ch\u1ecdn m\u00f4n \u0111\u1ec3 v\u00e0o roadmap",
    roadmapDashboardSectionSub: "M\u1ed7i m\u00f4n s\u1ebd m\u1edf roadmap ri\u00eang. Ch\u1ecdn m\u00f4n b\u1ea1n mu\u1ed1n ti\u1ebfp t\u1ee5c h\u1ecdc.",
    noData: "Kh\u00f4ng c\u00f3 d\u1eef li\u1ec7u",
    noGoal: "Ch\u01b0a c\u00f3 m\u1ee5c ti\u00eau",
    targetScore: "\u0110i\u1ec3m m\u1ee5c ti\u00eau",
    rank: "X\u1ebfp h\u1ea1ng",
    avg: "Trung b\u00ecnh",
    strong: "M\u1ea1nh",
    weak: "Y\u1ebfu",
    read: "\u0110\u00e3 \u0111\u1ecdc",
    unread: "Ch\u01b0a \u0111\u1ecdc",
    email: "Email",
    inApp: "Trong \u1ee9ng d\u1ee5ng",
    on: "B\u1eadt",
    off: "T\u1eaft"
  },
  en: {
    switchLabel: "Language",
    login: "Login",
    register: "Register",
    getStarted: "Get Started",
    startLearningNow: "Start Learning Now",
    getStartedFree: "Get Started Free",
    joinProgram: "Join Program",
    choosePlan: "Choose Your Plan",
    choosePlanSub: "Choose the subjects that match your goals to start your personalized roadmap.",
    needAuth: "You need to login/register before starting",
    checkoutTitle: "Checkout",
    checkoutSubtitle: "Choose subjects to unlock roadmap.",
    checkoutTotal: "Total",
    checkoutBack: "Back",
    checkoutPay: "Pay and unlock roadmap",
    dashboard: "Dashboard",
    history: "History",
    roadmap: "Roadmap",
    profile: "Profile",
    logout: "Logout",
    setting: "Setting",
    studentPortal: "Student Portal",
    notificationTitle: "Notifications",
    notificationLoading: "Loading notifications...",
    authIntro: "Sign in to sync placement, roadmap, and learning history.",
    authBullet1: "Placement test with local backend results",
    authBullet2: "Unlock roadmap from local checkout",
    authBullet3: "History of attempts",
    continueWith: "OR CONTINUE WITH",
    googleNote: "Google login requires",
    dashboardTitle: "My Learning Dashboard",
    historyTitle: "Placement History",
    roadmapTitle: "Learning Roadmap",
    roadmapDashboardTitle: "Roadmap Dashboard",
    roadmapDashboardSectionTitle: "Choose a subject to open roadmap",
    roadmapDashboardSectionSub: "Each subject has its own roadmap. Select the subject you want to continue.",
    noData: "No data",
    noGoal: "No goal",
    targetScore: "Target score",
    rank: "Rank",
    avg: "Avg",
    strong: "Strong",
    weak: "Weak",
    read: "Read",
    unread: "Unread",
    email: "Email",
    inApp: "In-App",
    on: "ON",
    off: "OFF"
  }
};

const STATIC_TEXT_MAP = {
  vi: {
    "Completed Lessons": "B\u00e0i h\u1ecdc \u0111\u00e3 ho\u00e0n th\u00e0nh",
    "Mini Tests Done": "Mini test \u0111\u00e3 l\u00e0m",
    "Average Score": "\u0110i\u1ec3m trung b\u00ecnh",
    "Goal Progress": "Ti\u1ebfn \u0111\u1ed9 m\u1ee5c ti\u00eau",
    "Roadmap Active": "Roadmap \u0111ang h\u1ecdc",
    "Roadmap Done": "Roadmap ho\u00e0n th\u00e0nh",
    "Time": "Th\u1eddi gian",
    "Subject": "M\u00f4n h\u1ecdc",
    "Score": "\u0110i\u1ec3m",
    "Attempt": "L\u1ea7n l\u00e0m",
    "Loading...": "\u0110ang t\u1ea3i..."
  },
  en: {
    "B\u00e0i h\u1ecdc \u0111\u00e3 ho\u00e0n th\u00e0nh": "Completed Lessons",
    "Mini test \u0111\u00e3 l\u00e0m": "Mini Tests Done",
    "\u0110i\u1ec3m trung b\u00ecnh": "Average Score",
    "Ti\u1ebfn \u0111\u1ed9 m\u1ee5c ti\u00eau": "Goal Progress",
    "Roadmap \u0111ang h\u1ecdc": "Roadmap Active",
    "Roadmap ho\u00e0n th\u00e0nh": "Roadmap Done",
    "Th\u1eddi gian": "Time",
    "M\u00f4n h\u1ecdc": "Subject",
    "\u0110i\u1ec3m": "Score",
    "L\u1ea7n l\u00e0m": "Attempt",
    "\u0110ang t\u1ea3i...": "Loading..."
  }
};

function getLang() {
  const lang = (localStorage.getItem(KEYS.language) || "vi").toLowerCase();
  return lang === "en" ? "en" : "vi";
}

function t(key) {
  const lang = getLang();
  return (I18N[lang] && I18N[lang][key]) || (I18N.vi && I18N.vi[key]) || key;
}

function applyStaticTextMap(lang) {
  const map = STATIC_TEXT_MAP[lang];
  if (!map || !document.body) return;
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
  const updates = [];
  while (walker.nextNode()) {
    const node = walker.currentNode;
    const parent = node.parentElement;
    if (!parent) continue;
    const tag = parent.tagName;
    if (tag === "SCRIPT" || tag === "STYLE" || tag === "CODE") continue;
    const raw = node.textContent;
    const trimmed = String(raw || "").trim();
    if (!trimmed || !map[trimmed]) continue;
    updates.push({ node, raw, from: trimmed, to: map[trimmed] });
  }
  updates.forEach(({ node, raw, from, to }) => {
    node.textContent = raw.replace(from, to);
  });
}

function applyLanguage() {
  const lang = getLang();
  document.documentElement.lang = lang;
  const setBySelector = (selector, value) => {
    const el = document.querySelector(selector);
    if (el) el.textContent = value;
  };

  setText("landing-get-started-top", t("getStarted"));
  setText("landing-start-learning", t("startLearningNow"));
  setText("landing-get-started-bottom", t("getStartedFree"));
  setText("landing-login-link", t("login"));
  setText("landing-subject-title", t("choosePlan"));
  setText("landing-subject-subtitle", t("choosePlanSub"));
  document.querySelectorAll(".js-join-program").forEach((btn) => {
    btn.textContent = t("joinProgram");
  });

  setText("tab-login", t("login"));
  setText("tab-register", t("register"));
  setText("checkout-title", t("checkoutTitle"));
  setText("checkout-subtitle", t("checkoutSubtitle"));
  setText("checkout-total-label", t("checkoutTotal"));
  setText("checkout-back", t("checkoutBack"));
  setText("checkout-pay-text", t("checkoutPay"));
  setText("auth-intro", t("authIntro"));
  setText("auth-bullet-1", t("authBullet1"));
  setText("auth-bullet-2", t("authBullet2"));
  setText("auth-bullet-3", t("authBullet3"));
  setText("auth-or-continue", t("continueWith"));
  setBySelector("#auth-google-note-label", t("googleNote"));

  setText("dash-nav-overview-label", t("dashboard"));
  setText("dash-nav-roadmap-label", t("roadmap"));
  setText("dash-nav-history-label", t("history"));
  setText("dash-nav-profile-label", t("profile"));
  setText("dash-nav-logout-label", t("logout"));
  setText("dash-setting-label", t("setting"));
  setText("dash-portal-label", t("studentPortal"));
  setText("dash-page-title", t("dashboardTitle"));
  setText("dash-notif-title", t("notificationTitle"));
  setText("dash-notif-loading", t("notificationLoading"));

  setText("history-nav-dashboard-label", t("dashboard"));
  setText("history-nav-roadmap-label", t("roadmap"));
  setText("history-nav-overview-label", t("history"));
  setText("history-nav-profile-label", t("profile"));
  setText("history-nav-logout-label", t("logout"));
  setText("history-setting-label", t("setting"));
  setText("history-portal-label", t("studentPortal"));
  setText("history-page-title", t("historyTitle"));
  setText("history-notif-title", t("notificationTitle"));
  setText("history-notif-loading", t("notificationLoading"));

  setText("road-nav-dashboard-label", t("dashboard"));
  setText("road-nav-overview-label", t("roadmap"));
  setText("road-nav-history-label", t("history"));
  setText("road-nav-profile-label", t("profile"));
  setText("road-nav-logout-label", t("logout"));
  setText("road-setting-label", t("setting"));
  setText("road-portal-label", t("studentPortal"));
  setText("road-page-title", t("roadmapTitle"));
  setText("road-notif-title", t("notificationTitle"));
  setText("road-notif-loading", t("notificationLoading"));

  setText("rm-nav-dashboard-label", t("dashboard"));
  setText("rm-nav-overview-label", t("roadmap"));
  setText("rm-nav-history-label", t("history"));
  setText("rm-nav-profile-label", t("profile"));
  setText("rm-nav-logout-label", t("logout"));
  setText("rm-setting-label", t("setting"));
  setText("rm-portal-label", t("studentPortal"));
  setText("rm-page-title", t("roadmapDashboardTitle"));
  setText("rm-section-title", t("roadmapDashboardSectionTitle"));
  setText("rm-section-subtitle", t("roadmapDashboardSectionSub"));
  setText("rm-notif-title", t("notificationTitle"));
  setText("rm-notif-loading", t("notificationLoading"));

  setText("landing-notif-title", t("notificationTitle"));
  setText("landing-notif-loading", t("notificationLoading"));

  applyStaticTextMap(lang);

  const switchBtn = document.getElementById("language-switch-btn");
  if (switchBtn) {
    switchBtn.textContent = `${t("switchLabel")}: ${lang.toUpperCase()}`;
  }
}

function setLang(lang) {
  localStorage.setItem(KEYS.language, lang === "en" ? "en" : "vi");
  applyLanguage();
}

function ensureLanguageSwitcher() {
  let btn = document.getElementById("language-switch-btn");
  if (!btn) {
    btn = document.createElement("button");
    btn.id = "language-switch-btn";
    btn.type = "button";
    btn.style.position = "fixed";
    btn.style.right = "16px";
    btn.style.bottom = "16px";
    btn.style.zIndex = "9999";
    btn.style.padding = "8px 12px";
    btn.style.borderRadius = "9999px";
    btn.style.background = "#0d7ff2";
    btn.style.color = "white";
    btn.style.fontWeight = "700";
    btn.style.fontSize = "12px";
    btn.style.border = "none";
    btn.style.cursor = "pointer";
    btn.addEventListener("click", () => {
      setLang(getLang() === "vi" ? "en" : "vi");
    });
    document.body.appendChild(btn);
  }
  btn.style.left = "auto";
  btn.style.right = "16px";
  btn.style.bottom = "16px";
  applyLanguage();
}

export { getLang, t, applyLanguage, setLang, ensureLanguageSwitcher };
