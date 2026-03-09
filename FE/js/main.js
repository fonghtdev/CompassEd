import { api, pageName } from "./features/core.js";
import { initSharedLayout } from "./features/layout.js";
import { ensureLanguageSwitcher } from "./features/i18n.js";
import { initLanding } from "./features/landing.js";
import { initAuth } from "./features/auth.js";
import { initPlacement } from "./features/placement.js";
import { initResult } from "./features/result.js";
import { initCheckout } from "./features/checkout.js";
import { initRoadmap } from "./features/roadmap.js";
import { initRoadmapDashboard } from "./features/roadmapDashboard.js";
import { initRoadmapTutor } from "./features/roadmapTutor.js";
import { initLearningAi } from "./features/learningAi.js";
import { initHistory } from "./features/history.js";
import { initDashboard } from "./features/dashboard.js";
import { initProfile } from "./features/profile.js";
import { initAdmin } from "./features/admin.js";

async function trackAnonymousVisit() {
  try {
    const key = "compassed_visitor_id";
    let visitorId = localStorage.getItem(key);
    if (!visitorId) {
      visitorId = `v_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
      localStorage.setItem(key, visitorId);
    }
    const today = new Date().toISOString().slice(0, 10);
    const lastTrackedDate = localStorage.getItem("compassed_last_visit_track_date");
    if (lastTrackedDate === today) return;

    await api("/api/analytics/visit", "POST", {
      visitorId,
      pagePath: window.location.pathname
    }, false);
    localStorage.setItem("compassed_last_visit_track_date", today);
  } catch (e) {
    // Silent fail: tracking must not block UX
  }
}

document.addEventListener("DOMContentLoaded", () => {
  initSharedLayout();
  ensureLanguageSwitcher();
  trackAnonymousVisit();
  const page = pageName();
  if (page === "landing") initLanding();
  if (page === "auth") initAuth();
  if (page === "placement") initPlacement();
  if (page === "result") initResult();
  if (page === "checkout") initCheckout();
  if (page === "roadmap") initRoadmap();
  if (page === "roadmapDashboard") initRoadmapDashboard();
  if (page === "roadmapTutor") initRoadmapTutor();
  if (page === "learningAi") initLearningAi();
  if (page === "history") initHistory();
  if (page === "dashboard") initDashboard();
  if (page === "profile") initProfile();
  if (page === "admin") initAdmin();
});
