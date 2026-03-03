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

function isPaymentRequiredError(message) {
  const msg = String(message || "").toLowerCase();
  return msg.includes("payment_required") || msg.includes("need subscription") || msg.includes("payment required");
}

function getGradeLevel(subjectId) {
  const key = `compassed_grade_level_${subjectId}`;
  const q = new URLSearchParams(window.location.search);
  const fromUrl = Number(q.get("grade") || q.get("gradeLevel"));
  if (fromUrl) {
    localStorage.setItem(key, String(fromUrl));
    return fromUrl;
  }
  return Number(localStorage.getItem(key) || 10);
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

// Add detailed logging for debugging
console.log("Initializing placement test...");

async function initPlacement() {
  console.log("Checking session...");
  if (!(await checkSession())) {
    console.warn("Session invalid. Redirecting to login...");
    goAuthWithRedirect(window.location.pathname + window.location.search, "placementTest.html" + window.location.search);
    return;
  }

  const subjectId = getSubjectId();
  const gradeLevel = getGradeLevel(subjectId);
  console.log("Subject ID:", subjectId);

  const attemptKey = `${KEYS.attemptId}_${subjectId}`;
  const paperKey = `${KEYS.paper}_${subjectId}`;
  const answersKey = `${KEYS.answers}_${subjectId}`;
  const gradeKey = `compassed_grade_level_${subjectId}`;

  showLoading("Loading placement...");
  try {
    let attemptId = Number(localStorage.getItem(attemptKey) || 0);
    let paper = JSON.parse(localStorage.getItem(paperKey) || "[]");
    const cachedGrade = Number(localStorage.getItem(gradeKey) || 0);
    console.log("Attempt ID:", attemptId);
    console.log("Paper from localStorage:", paper.length, "items");

    // Khởi tạo timerKey sau khi có attemptId
    const timerKey = `compassed_timer_${subjectId}_${attemptId || 'preview'}`;

    // Force-clear nếu URL có ?reset=1 hoặc nếu localStorage chứa câu hỏi demo cũ
    const urlParams = new URLSearchParams(window.location.search);
    const forceReset = urlParams.get("reset") === "1";
    const hasDemoQuestions = Array.isArray(paper) && paper.length > 0 &&
      paper.some((it) => it.q && String(it.q).toLowerCase().includes("demo"));
    if (forceReset || hasDemoQuestions || (cachedGrade && cachedGrade !== gradeLevel)) {
      console.log("Clearing cached demo/old data to force fresh fetch");
      localStorage.removeItem(attemptKey);
      localStorage.removeItem(paperKey);
      localStorage.removeItem(answersKey);
      attemptId = 0;
      paper = [];
    }

    let previewMode = false;
    if (!attemptId || !paper.length) {
      try {
        console.log("Starting placement attempt...");
        const started = await api(`/api/subjects/${subjectId}/placement-tests?gradeLevel=${gradeLevel}`, "POST", null, true);
        console.log("Placement started successfully:", started);
        attemptId = started.attemptId;
        paper = JSON.parse(started.paperJson || "[]");
        localStorage.setItem(attemptKey, String(attemptId));
        localStorage.setItem(paperKey, JSON.stringify(paper));
        localStorage.setItem(answersKey, JSON.stringify({}));
        localStorage.setItem(gradeKey, String(gradeLevel));
      } catch (e) {
        if (isPaymentRequiredError(e && e.message)) {
          toast("Bạn cần đăng ký gói để làm placement.", "warn");
          nav("/checkout", "checkout.html");
          return;
        }
        console.warn("Placement start failed. Falling back to question bank:", e);
        try {
          const resp = await api(`/api/questions?subjectId=${subjectId}&gradeLevel=${gradeLevel}&size=50`, "GET", null, false);
          console.log("Question bank response:", resp);
          // API trả về { questions: [...], totalItems: N, ... }
          const qrows = (resp && Array.isArray(resp.questions)) ? resp.questions
                       : Array.isArray(resp) ? resp : [];
          console.log("Extracted questions array:", qrows.length, "items");
          if (qrows.length > 0) {
            paper = qrows.map((q) => {
              let options = [];
              try {
                options = typeof q.options === "string" ? JSON.parse(q.options) : (Array.isArray(q.options) ? q.options : []);
              } catch (ex) {
                if (typeof q.options === "string") options = q.options.split("|").map((s) => s.trim());
              }
              return {
                id: q.id || Math.floor(Math.random() * 1000000),
                q: q.questionText || q.question || "",
                options: options,
                correct: q.correctAnswer || null,
                skill: q.skillType || null
              };
            });
            attemptId = 0;
            localStorage.setItem(attemptKey, String(attemptId));
            localStorage.setItem(paperKey, JSON.stringify(paper));
            localStorage.setItem(answersKey, JSON.stringify({}));
            localStorage.setItem(gradeKey, String(gradeLevel));
            previewMode = true;
            toast("Loaded questions from Question Bank (preview mode)", "warn");
          } else {
            console.warn("No questions found in question bank.");
            throw e;
          }
        } catch (e2) {
          console.error("Fallback to question bank failed:", e2);
          throw e;
        }
      }
    }

    console.log("Final paper:", paper);
    if (!paper.length) {
      console.error("No questions available to display.");
      toast("No questions available.", "error");
      return;
    }

    let index = 0;
    const answers = JSON.parse(localStorage.getItem(answersKey) || "{}");

    // If paper is empty or contains demo markers, try to fetch question bank directly
    const looksLikeDemo = (p) => {
      if (!Array.isArray(p) || !p.length) return true;
      return p.some((it) => (it.q && String(it.q).toLowerCase().includes("demo")) || (it.q && String(it.q).toLowerCase().includes("question") && String(it.q).toLowerCase().includes("demo")));
    };
    if (looksLikeDemo(paper)) {
      try {
        const resp = await api(`/api/questions?subjectId=${subjectId}&gradeLevel=${gradeLevel}&size=50`, "GET", null, false);
        // API trả về { questions: [...], totalItems: N, ... }
        const qrows = (resp && Array.isArray(resp.questions)) ? resp.questions
                     : Array.isArray(resp) ? resp : [];
        if (qrows.length > 0) {
          paper = qrows.map((q) => {
            let options = [];
            try {
              options = typeof q.options === "string" ? JSON.parse(q.options) : (Array.isArray(q.options) ? q.options : []);
            } catch (ex) {
              if (typeof q.options === "string") options = q.options.split("|").map((s) => s.trim());
            }
            return {
              id: q.id || Math.floor(Math.random() * 1000000),
              q: q.questionText || q.question || "",
              options: options,
              correct: q.correctAnswer || null,
              skill: q.skillType || null
            };
          });
          localStorage.setItem(paperKey, JSON.stringify(paper));
          localStorage.setItem(gradeKey, String(gradeLevel));
          previewMode = true;
          toast("Loaded real questions from Question Bank", "warn");
        }
      } catch (e) {
        // ignore fallback error; keep existing paper
      }
    }

    const questionEl = document.getElementById("placement-question-text");
    const descEl = document.getElementById("placement-question-desc");
    const counterEl = document.getElementById("placement-counter-mobile");
    const progressText = document.getElementById("placement-progress-text");
    const progressBar = document.getElementById("placement-progress-bar");
    const progressTextMain = document.getElementById("placement-progress-text-main");
    const progressBarMain = document.getElementById("placement-progress-bar-main");
    const questionGrid = document.getElementById("placement-question-grid");
    const prevBtn = document.getElementById("placement-prev-btn");
    const nextBtn = document.getElementById("placement-next-btn");
    const submitBtn = document.getElementById("placement-submit-btn");
    const flagBtn = document.getElementById("placement-flag-btn");
    const saveExitBtn = document.getElementById("placement-save-exit-btn");
    const settingsBtn = document.getElementById("placement-settings-btn");
    const loadPreviewBtn = document.getElementById("placement-load-preview-btn");
    const previewSubjectSel = document.getElementById("placement-preview-subject");
    const gradeSelect = document.getElementById("placement-grade-select");
    if (gradeSelect) {
      gradeSelect.value = String(gradeLevel);
      gradeSelect.addEventListener("change", () => {
        const newGrade = Number(gradeSelect.value) || 10;
        localStorage.setItem(gradeKey, String(newGrade));
        const params = new URLSearchParams(window.location.search);
        params.set("grade", String(newGrade));
        params.set("subjectId", String(subjectId));
        params.set("reset", "1");
        window.location.search = params.toString();
      });
    }

    // --- Đồng hồ đếm ngược 45 phút ---
    // Lấy thời gian còn lại từ localStorage hoặc mặc định 45 phút
    const savedTime = localStorage.getItem(timerKey);
    let timerSeconds = savedTime ? parseInt(savedTime) : 45 * 60;
    
    const hrsEl = document.getElementById("placement-timer-hrs");
    const minEl = document.getElementById("placement-timer-min");
    const secEl = document.getElementById("placement-timer-sec");
    
    function updateTimerDisplay() {
      const h = Math.floor(timerSeconds / 3600);
      const m = Math.floor((timerSeconds % 3600) / 60);
      const s = timerSeconds % 60;
      if (hrsEl) hrsEl.textContent = String(h).padStart(2, "0");
      if (minEl) {
        minEl.textContent = String(m).padStart(2, "0");
        minEl.classList.toggle("text-red-500", timerSeconds < 300);
        minEl.classList.toggle("text-primary", timerSeconds >= 300);
      }
      if (secEl) secEl.textContent = String(s).padStart(2, "0");
      
      // Lưu thời gian còn lại vào localStorage
      localStorage.setItem(timerKey, String(timerSeconds));
    }
    
    updateTimerDisplay();
    
    const timerInterval = setInterval(() => {
      if (timerSeconds <= 0) {
        clearInterval(timerInterval);
        localStorage.removeItem(timerKey);
        toast("Hết giờ! Đang tự động nộp bài...", "warn");
        // Tự động submit sau 1 giây
        setTimeout(() => {
          handleSubmit();
        }, 1000);
        return;
      }
      timerSeconds--;
      updateTimerDisplay();
    }, 1000);

    // --- Render lưới số câu hỏi ---
    function renderQuestionGrid() {
      if (!questionGrid) return;
      questionGrid.innerHTML = "";
      const grid = document.createElement("div");
      grid.className = "grid grid-cols-5 gap-2";
      paper.forEach((q, i) => {
        const isAnswered = !!answers[String(q.id)];
        const isCurrent = i === index;
        const btn = document.createElement("button");
        btn.className = [
          "w-full aspect-square rounded-lg text-sm transition-all duration-150 flex items-center justify-center",
          isCurrent
            ? "bg-primary text-white font-bold shadow-md shadow-primary/30"
            : isAnswered
            ? "bg-emerald-500 text-white font-bold"
            : "bg-slate-100 dark:bg-slate-800 text-text-secondary-light dark:text-text-secondary-dark hover:bg-slate-200 dark:hover:bg-slate-700 font-medium"
        ].join(" ");
        btn.textContent = String(i + 1);
        btn.addEventListener("click", () => { index = i; rerender(); });
        grid.appendChild(btn);
      });
      questionGrid.appendChild(grid);
    }

    function rerender() {
      const q = paper[index];
      if (!q) return;
      questionEl.textContent = q.q || `Question ${index + 1}`;
      if (descEl) descEl.textContent = "Choose one answer.";
      const options = (q.options || []).map((x) => String(x).replace(/^[A-D]\.\s*/, ""));
      renderPlacementOptions(options, answers[String(q.id)], (value) => {
        answers[String(q.id)] = value;
        localStorage.setItem(answersKey, JSON.stringify(answers));
        renderQuestionGrid();
        // cập nhật progress
        const answeredCount = Object.keys(answers).length;
        if (progressText) progressText.textContent = `${answeredCount} / ${paper.length}`;
        if (progressBar) progressBar.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
        if (progressTextMain) progressTextMain.textContent = `${answeredCount} / ${paper.length}`;
        if (progressBarMain) progressBarMain.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
      });
      const answeredCount = Object.keys(answers).length;
      if (progressText) progressText.textContent = `${answeredCount} / ${paper.length}`;
      if (progressBar) progressBar.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
      if (progressTextMain) progressTextMain.textContent = `${answeredCount} / ${paper.length}`;
      if (progressBarMain) progressBarMain.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
      if (counterEl) counterEl.textContent = `Câu ${index + 1} / ${paper.length}`;
      prevBtn.disabled = index === 0;
      
      // Cập nhật text nút Next
      const isLastQuestion = index === paper.length - 1;
      nextBtn.innerHTML = isLastQuestion 
        ? 'Nộp bài<span class="material-symbols-outlined">check_circle</span>'
        : 'Câu tiếp theo<span class="material-symbols-outlined">arrow_forward</span>';
      
      renderQuestionGrid();
    }

    prevBtn.addEventListener("click", () => {
      if (index > 0) {
        index -= 1;
        rerender();
      }
    });

    // Hàm xử lý submit
    async function handleSubmit() {
      // Kiểm tra xem có câu hỏi chưa trả lời không
      const unansweredCount = paper.length - Object.keys(answers).length;
      if (unansweredCount > 0) {
        const confirmSubmit = confirm(`Bạn còn ${unansweredCount} câu chưa trả lời. Bạn có chắc muốn nộp bài không?`);
        if (!confirmSubmit) return;
      }

      // Nếu đang ở preview mode
      if (previewMode || Number(attemptId) === 0) {
        clearInterval(timerInterval);
        localStorage.removeItem(timerKey);
        toast("Preview mode: submission is disabled. Your answers are saved locally.", "warn");
        localStorage.setItem(KEYS.result, JSON.stringify({ preview: true, answers }));
        localStorage.removeItem(attemptKey);
        localStorage.removeItem(paperKey);
        localStorage.removeItem(answersKey);
        nav("/placement-result", "placementTestResult.html");
        return;
      }

      // Submit lên server
      showLoading("Đang nộp bài...");
      try {
        const result = await api(`/api/placement-attempts/${attemptId}/submit`, "POST", { answersJson: JSON.stringify(answers) }, true);
        clearInterval(timerInterval);
        localStorage.removeItem(timerKey);
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
          localStorage.removeItem(attemptKey);
          localStorage.removeItem(paperKey);
          localStorage.removeItem(answersKey);
          localStorage.removeItem(timerKey);
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
    }

    nextBtn.addEventListener("click", async () => {
      const q = paper[index];
      
      // Nếu đang ở câu cuối cùng, submit bài
      if (index === paper.length - 1) {
        await handleSubmit();
        return;
      }
      
      // Nếu chưa chọn đáp án
      if (!answers[String(q.id)]) {
        toast("Vui lòng chọn đáp án", "warn");
        return;
      }
      
      // Chuyển sang câu tiếp theo
      index += 1;
      rerender();
    });

    // Event listener cho nút Submit
    if (submitBtn) {
      submitBtn.addEventListener("click", async () => {
        await handleSubmit();
      });
    }

    // Preview loader (hidden element, kept for programmatic use)
    if (loadPreviewBtn && previewSubjectSel) {
      loadPreviewBtn.addEventListener("click", async () => {
        const sid = Number(previewSubjectSel.value) || subjectId;
        try {
          showLoading("Loading preview questions...");
          const resp = await api(`/api/questions?subjectId=${sid}&gradeLevel=${gradeLevel}&size=50`, "GET", null, false);
          console.log("Load preview response:", resp);
          // API trả về { questions: [...], totalItems: N, ... }
          const qrows = (resp && Array.isArray(resp.questions)) ? resp.questions
                       : Array.isArray(resp) ? resp : [];
          console.log("Preview questions count:", qrows.length);
          if (!qrows.length) {
            toast("No questions available for selected subject", "warn");
            return;
          }
          paper = qrows.map((q) => {
            let options = [];
            try {
              options = typeof q.options === "string" ? JSON.parse(q.options) : (Array.isArray(q.options) ? q.options : []);
            } catch (ex) {
              if (typeof q.options === "string") options = q.options.split("|").map((s) => s.trim());
            }
            return {
              id: q.id || Math.floor(Math.random() * 1000000),
              q: q.questionText || q.question || "",
              options: options,
              correct: q.correctAnswer || null,
              skill: q.skillType || null
            };
          });
          // set preview mode
          previewMode = true;
          attemptId = 0;
          localStorage.setItem(attemptKey, String(attemptId));
          localStorage.setItem(paperKey, JSON.stringify(paper));
          localStorage.setItem(answersKey, JSON.stringify({}));
          localStorage.setItem(gradeKey, String(gradeLevel));
          toast(`Loaded ${paper.length} questions from Question Bank`, "info");
          index = 0;
          rerender();
        } catch (e) {
          console.error("Error loading preview questions:", e);
          toast(`Preview load failed: ${e.message || e}`, "error");
        } finally {
          hideLoading();
        }
      });
    }
    if (flagBtn) {
      flagBtn.addEventListener("click", () => toast("Question flagged for review"));
    }
    if (saveExitBtn) {
      saveExitBtn.addEventListener("click", () => {
        clearInterval(timerInterval);
        localStorage.setItem(answersKey, JSON.stringify(answers));
        toast("Progress saved");
        nav("/landing", "landingPage.html");
      });
    }
    if (settingsBtn) {
      settingsBtn.addEventListener("click", () => nav("/profile", "profile.html"));
    }

    rerender();
  } catch (err) {
    console.error("Error initializing placement:", err);
    const msg = String(err && err.message ? err.message : "");
    if (isPaymentRequiredError(msg)) {
      toast("Bạn cần đăng ký gói để làm placement.", "warn");
      nav("/checkout", "checkout.html");
      return;
    }
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
