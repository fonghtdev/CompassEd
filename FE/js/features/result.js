import { KEYS, api, checkSession, clearAuth, formatVnd, getSubjectId, goAuthWithRedirect, nav, setText, toast } from "./core.js";

async function initResult() {
  const data = JSON.parse(localStorage.getItem(KEYS.result) || "{}");
  const scorePercent = Number(data.scorePercent || 0);
  const score10 = Math.round((scorePercent / 10) * 10) / 10;
  const level = data.level || "L1";
  const subjectId = getSubjectId();

  setText("result-score", String(score10));
  setText("result-level", level);
  setText("result-focus", level === "L3" ? "Advanced Mixed Skills" : level === "L2" ? "Algebra" : "Core Foundations");

  const progressEl = document.getElementById("result-level-progress");
  if (progressEl) progressEl.style.width = `${level === "L3" ? 100 : level === "L2" ? 65 : 35}%`;

  const nextTarget = document.getElementById("result-next-target");
  if (nextTarget) {
    if (level === "L1") nextTarget.textContent = "Next level at 5 điểm";
    else if (level === "L2") nextTarget.textContent = "Next level at 9 điểm";
    else nextTarget.textContent = "Max level reached";
  }

  const backBtn = document.getElementById("result-back-btn");
  if (backBtn) {
    backBtn.addEventListener("click", async () => {
      if (await checkSession()) {
        nav("/dashboard", "dashboard.html");
        return;
      }
      nav("/landing", "landingPage.html");
    });
  }

  const returnLaterBtn = document.getElementById("result-return-later-btn");
  if (returnLaterBtn) {
    returnLaterBtn.addEventListener("click", async () => {
      if (await checkSession()) {
        nav("/dashboard", "dashboard.html");
        return;
      }
      nav("/landing", "landingPage.html");
    });
  }

  const unlockBtn = document.getElementById("result-unlock-btn");
  const unlockLabel = document.getElementById("result-unlock-label");
  const unlockPrice = document.getElementById("result-unlock-price");
  let hasActiveSub = false;
  if (await checkSession()) {
    try {
      const subData = await api("/api/me/subscriptions", "GET", null, true);
      const active = (subData && subData.activeSubscriptions) || [];
      hasActiveSub = active.some((x) => Number(x.subjectId) === Number(subjectId));
    } catch {}
    try {
      const plans = await api("/api/pricing/plans", "GET", null, false);
      const one = Array.isArray(plans) ? plans.find((p) => Number(p.subjectCount) === 1) : null;
      if (unlockPrice) unlockPrice.textContent = formatVnd(one ? one.amountVnd : 50000);
    } catch {}
  }
  if (unlockBtn) {
    if (unlockLabel) unlockLabel.textContent = hasActiveSub ? "Go to Roadmap" : "Unlock Now";
    unlockBtn.addEventListener("click", async () => {
      if (!(await checkSession())) {
        goAuthWithRedirect("/placement-result", "placementTestResult.html");
        return;
      }
      localStorage.setItem("compassed_subject_id", String(subjectId));
      if (hasActiveSub) {
        nav(`/learning-roadmap?subjectId=${subjectId}`, `coursesDetail.html?subjectId=${subjectId}`);
        return;
      }
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
      localStorage.setItem("compassed_subject_id", String(subjectId));
      if (hasActiveSub) {
        nav(`/learning-roadmap?subjectId=${subjectId}`, `coursesDetail.html?subjectId=${subjectId}`);
        return;
      }
      nav("/checkout", "checkout.html");
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
