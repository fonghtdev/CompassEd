import { api, showLoading, hideLoading, toast } from "./core.js";

export async function initQuestions() {
  showLoading("Đang tải câu hỏi...");
  try {
    // Example: get subjectId, level, skillType from UI or URL
    const subjectId = Number(localStorage.getItem("compassed_subject_id") || 1);
    const level = localStorage.getItem("compassed_level") || null;
    const skillType = localStorage.getItem("compassed_skill_type") || null;
    const params = [];
    if (subjectId) params.push(`subjectId=${subjectId}`);
    if (level) params.push(`level=${level}`);
    if (skillType) params.push(`skillType=${skillType}`);
    const query = params.length ? "?" + params.join("&") : "";
    const data = await api(`/api/questions${query}`, "GET", null, false);
    const questions = data.questions || [];
    renderQuestions(questions);
  } catch (err) {
    toast("Không tải được câu hỏi: " + err.message, "error");
  } finally {
    hideLoading();
  }
}

function renderQuestions(questions) {
  const wrap = document.getElementById("user-question-list");
  if (!wrap) return;
  wrap.innerHTML = "";
  if (!questions.length) {
    wrap.innerHTML = "<div class='text-sm text-slate-400'>Không có câu hỏi nào.</div>";
    return;
  }
  questions.forEach((q) => {
    const el = document.createElement("div");
    el.className = "question-item border rounded p-3 mb-2";
    el.innerHTML = `
      <div class='font-semibold'>${q.questionText}</div>
      <div class='text-xs text-slate-500'>Môn: ${q.subjectName} | Level: ${q.level} | Kỹ năng: ${q.skillType}</div>
      <div class='mt-2'>
        <b>Đáp án:</b> ${q.correctAnswer}
      </div>
      <div class='mt-1 text-xs text-slate-400'>Giải thích: ${q.explanation || ""}</div>
    `;
    wrap.appendChild(el);
  });
}
