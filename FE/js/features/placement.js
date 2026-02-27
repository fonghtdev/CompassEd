import {
  KEYS,
  api,
  checkSession,
  clearAuth,
  getSubjectId,
  goAuthWithRedirect,
  hideLoading,
  nav,
  showLoading,
  toast
} from "./core.js";

function isUnauthorizedError(message) {
  const msg = String(message || "").toLowerCase();
  return msg.includes("http 401") || msg.includes("\"status\":401") || msg.includes("unauthorized") || msg.includes("invalid token");
}

function renderPlacementOptions(options, selected, onSelect) {
  const wrap = document.getElementById("placement-options");
  if (!wrap) return;
  wrap.innerHTML = "";
  options.forEach((text, idx) => {
    const letter = String.fromCharCode(65 + idx);
    const label = document.createElement("label");
    label.className = "relative group cursor-pointer";
    label.innerHTML = `
      <input class="peer sr-only" type="radio" name="placement-answer" value="${letter}" ${selected === letter ? "checked" : ""} />
      <div class="p-5 rounded-xl border-2 border-border-light bg-surface-light hover:border-primary/50 peer-checked:border-primary peer-checked:bg-primary/5 transition-all duration-200 flex items-center gap-4">
        <div class="size-8 rounded-full border-2 border-border-light flex items-center justify-center text-sm font-bold text-text-secondary-light peer-checked:bg-primary peer-checked:border-primary peer-checked:text-white">${letter}</div>
        <span class="text-lg font-medium">${text}</span>
      </div>`;
    label.querySelector("input").addEventListener("change", (e) => onSelect(e.target.value));
    wrap.appendChild(label);
  });
}

async function initPlacement() {
  if (!(await checkSession())) {
    goAuthWithRedirect(window.location.pathname + window.location.search, "placementTest.html" + window.location.search);
    return;
  }

  const subjectId = getSubjectId();
  const attemptKey = `${KEYS.attemptId}_${subjectId}`;
  const paperKey = `${KEYS.paper}_${subjectId}`;
  const answersKey = `${KEYS.answers}_${subjectId}`;

  showLoading("Loading placement...");
  try {
    let attemptId = Number(localStorage.getItem(attemptKey) || 0);
    let paper = JSON.parse(localStorage.getItem(paperKey) || "[]");
    if (!attemptId || !paper.length) {
      const started = await api(`/api/subjects/${subjectId}/placement-tests`, "POST", null, true);
      attemptId = started.attemptId;
      paper = JSON.parse(started.paperJson || "[]");
      localStorage.setItem(attemptKey, String(attemptId));
      localStorage.setItem(paperKey, JSON.stringify(paper));
      localStorage.setItem(answersKey, JSON.stringify({}));
    }
    let index = 0;
    const answers = JSON.parse(localStorage.getItem(answersKey) || "{}");

    const questionEl = document.getElementById("placement-question-text");
    const descEl = document.getElementById("placement-question-desc");
    const counterEl = document.getElementById("placement-counter-mobile");
    const progressText = document.getElementById("placement-progress-text");
    const progressBar = document.getElementById("placement-progress-bar");
    const prevBtn = document.getElementById("placement-prev-btn");
    const nextBtn = document.getElementById("placement-next-btn");
    const clearScratchpadBtn = document.getElementById("placement-clear-scratchpad-btn");
    const flagBtn = document.getElementById("placement-flag-btn");
    const saveExitBtn = document.getElementById("placement-save-exit-btn");
    const settingsBtn = document.getElementById("placement-settings-btn");
    const reviewAllBtn = document.getElementById("placement-review-all-btn");
    const navButtons = [1, 2, 3, 4, 5].map((i) => document.getElementById(`placement-nav-q${i}`));
    const lockedNavBtn = document.getElementById("placement-nav-locked");

    function rerender() {
      const q = paper[index];
      if (!q) return;
      questionEl.textContent = q.q || `Question ${index + 1}`;
      if (descEl) descEl.textContent = "Choose one answer.";
      const options = (q.options || []).map((x) => String(x).replace(/^[A-D]\.\s*/, ""));
      renderPlacementOptions(options, answers[String(q.id)], (value) => {
        answers[String(q.id)] = value;
        localStorage.setItem(answersKey, JSON.stringify(answers));
      });
      const answeredCount = Object.keys(answers).length;
      progressText.textContent = `${answeredCount} / ${paper.length}`;
      progressBar.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
      counterEl.textContent = `Question ${index + 1} of ${paper.length}`;
      prevBtn.disabled = index === 0;
      nextBtn.textContent = index === paper.length - 1 ? "Submit Test" : "Next Question";
      navButtons.forEach((btn, i) => {
        if (!btn) return;
        btn.classList.toggle("bg-primary/10", i === index);
        btn.classList.toggle("border", i === index);
        btn.classList.toggle("border-primary/20", i === index);
      });
    }

    prevBtn.addEventListener("click", () => {
      if (index > 0) {
        index -= 1;
        rerender();
      }
    });

    nextBtn.addEventListener("click", async () => {
      const q = paper[index];
      if (!answers[String(q.id)]) {
        toast("Please select an answer", "warn");
        return;
      }
      if (index < paper.length - 1) {
        index += 1;
        rerender();
        return;
      }
      showLoading("Submitting...");
      try {
        const result = await api(`/api/placement-attempts/${attemptId}/submit`, "POST", { answersJson: JSON.stringify(answers) }, true);
        localStorage.setItem(KEYS.result, JSON.stringify(result));
        localStorage.removeItem(attemptKey);
        localStorage.removeItem(paperKey);
        localStorage.removeItem(answersKey);
        nav("/placement-result", "placementTestResult.html");
      } catch (e) {
        const msg = String(e && e.message ? e.message : "");
        if (isUnauthorizedError(msg)) {
          clearAuth();
          toast("Session expired. Please login again.", "warn");
          goAuthWithRedirect(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
          return;
        }
        if (msg.toLowerCase().includes("attempt not found") || msg.includes("404")) {
          // Stale local attempt (changed DB/profile). Reset local state and start a fresh attempt.
          localStorage.removeItem(attemptKey);
          localStorage.removeItem(paperKey);
          localStorage.removeItem(answersKey);
          toast("Attempt expired. Starting a new placement test...", "warn");
          setTimeout(() => {
            window.location.reload();
          }, 300);
          return;
        }
        toast(`Submit failed: ${e.message}`, "error");
      } finally {
        hideLoading();
      }
    });

    navButtons.forEach((btn, i) => {
      if (!btn) return;
      btn.addEventListener("click", () => {
        index = Math.min(i, paper.length - 1);
        rerender();
      });
    });
    if (lockedNavBtn) {
      lockedNavBtn.addEventListener("click", () => toast("Locked section. Complete current section first.", "warn"));
    }
    if (clearScratchpadBtn) {
      clearScratchpadBtn.addEventListener("click", () => {
        const q = paper[index];
        if (!q) return;
        delete answers[String(q.id)];
        localStorage.setItem(answersKey, JSON.stringify(answers));
        rerender();
      });
    }
    if (flagBtn) {
      flagBtn.addEventListener("click", () => toast("Question flagged for review"));
    }
    if (saveExitBtn) {
      saveExitBtn.addEventListener("click", () => {
        localStorage.setItem(answersKey, JSON.stringify(answers));
        toast("Progress saved");
        nav("/landing", "landingPage.html");
      });
    }
    if (settingsBtn) {
      settingsBtn.addEventListener("click", () => nav("/profile", "profile.html"));
    }
    if (reviewAllBtn) {
      reviewAllBtn.addEventListener("click", () => {
        const answeredCount = Object.keys(answers).length;
        toast(`Answered ${answeredCount}/${paper.length} questions`);
      });
    }

    rerender();
  } catch (err) {
    const msg = String(err && err.message ? err.message : "");
    if (isUnauthorizedError(msg)) {
      clearAuth();
      toast("Session expired. Please login again.", "warn");
      goAuthWithRedirect(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
      return;
    }
    toast(`Cannot load placement: ${err.message}`, "error");
  } finally {
    hideLoading();
  }
}

export { initPlacement };
