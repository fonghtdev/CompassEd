import { nav } from "./core.js";

function initLearningAi() {
  const backBtn = document.getElementById("learning-ai-back");
  if (backBtn) {
    backBtn.addEventListener("click", () => nav("/landing", "landingPage.html"));
  }
}

export { initLearningAi };
