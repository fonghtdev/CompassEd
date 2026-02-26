import { KEYS, checkSession, clearAuth, getSubjectId, goAuthWithRedirect, nav, setText, toast } from "./core.js";

function initResult() {
  const data = JSON.parse(localStorage.getItem(KEYS.result) || "{}");
  const score = Math.round(Number(data.scorePercent || 0));
  const level = data.level || "L1";

  setText("result-score", String(score));
  setText("result-level", level);
  setText("result-focus", level === "L3" ? "Advanced Mixed Skills" : level === "L2" ? "Algebra" : "Core Foundations");

  const progressEl = document.getElementById("result-level-progress");
  if (progressEl) progressEl.style.width = `${level === "L3" ? 100 : level === "L2" ? 65 : 35}%`;

  const backBtn = document.getElementById("result-back-btn");
  if (backBtn) backBtn.addEventListener("click", () => nav("/landing", "landingPage.html"));

  const returnLaterBtn = document.getElementById("result-return-later-btn");
  if (returnLaterBtn) returnLaterBtn.addEventListener("click", () => nav("/landing", "landingPage.html"));

  const unlockBtn = document.getElementById("result-unlock-btn");
  if (unlockBtn) {
    unlockBtn.addEventListener("click", async () => {
      if (!(await checkSession())) {
        goAuthWithRedirect("/placement-result", "placementTestResult.html");
        return;
      }
      const subjectId = getSubjectId();
      localStorage.setItem("compassed_subject_id", String(subjectId));
      nav("/checkout", "checkout.html");
    });
  }

  const logoutBtn = document.getElementById("result-logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      clearAuth();
      nav("/landing", "landingPage.html");
    });
  }

  const focusBtn = document.getElementById("result-view-focus-btn");
  if (focusBtn) {
    focusBtn.addEventListener("click", async () => {
      if (!(await checkSession())) {
        goAuthWithRedirect("/placement-result", "placementTestResult.html");
        return;
      }
      nav("/learning-roadmap", "coursesDetail.html");
    });
  }

  const infoBtn = document.getElementById("result-analysis-info-btn");
  if (infoBtn) {
    infoBtn.addEventListener("click", () => {
      toast("AI analysis is generated from your placement answers and updated after each attempt.");
    });
  }
}

export { initResult };
