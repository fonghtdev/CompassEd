import { KEYS, api, checkSession, clearAuth, getSubjectId, goAuthWithRedirect, nav, setText, toast } from "./core.js";

function parseJsonLoose(raw) {
  if (!raw) return null;
  if (typeof raw === "object") return raw;
  const text = String(raw).trim();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    const first = text.indexOf("{");
    const last = text.lastIndexOf("}");
    if (first >= 0 && last > first) {
      try {
        return JSON.parse(text.slice(first, last + 1));
      } catch {
        return null;
      }
    }
    return null;
  }
}

function normalizeSkillRows(analysis, level) {
  const rawSkills = Array.isArray(analysis && analysis.skills) ? analysis.skills : [];
  const rows = rawSkills
    .map((s) => ({
      name: String((s && s.name) || "").trim(),
      score: Number((s && s.score) || 0),
      note: String((s && s.note) || "").trim()
    }))
    .filter((s) => s.name)
    .map((s) => ({ ...s, score: Math.max(0, Math.min(100, Number.isFinite(s.score) ? s.score : 0)) }))
    .sort((a, b) => b.score - a.score);

  if (rows.length) return rows.slice(0, 5);

  const fallbackScore = level === "L3" ? 85 : level === "L2" ? 65 : 40;
  return [{ name: "Nang luc tong quan", score: fallbackScore, note: "Du lieu mac dinh" }];
}

function polarPoint(score, index, total = 5) {
  const angle = (-90 + (360 / total) * index) * (Math.PI / 180);
  const radius = 40 * (Math.max(0, Math.min(100, score)) / 100);
  const x = 50 + radius * Math.cos(angle);
  const y = 50 + radius * Math.sin(angle);
  return `${x.toFixed(2)},${y.toFixed(2)}`;
}

function renderSkillAnalysis(skillAnalysisJson, level) {
  const parsed = parseJsonLoose(skillAnalysisJson) || {};
  const skills = normalizeSkillRows(parsed, level);

  const listEl = document.getElementById("result-skill-list");
  if (listEl) {
    listEl.innerHTML = skills.map((s) => `
      <div class="p-3 bg-slate-50 dark:bg-slate-800/50 rounded-lg">
        <div class="flex justify-between items-center mb-1">
          <span class="text-sm font-medium text-slate-700 dark:text-slate-300">${s.name}</span>
          <span class="text-sm font-bold text-primary">${Math.round(s.score)}%</span>
        </div>
        <div class="w-full bg-slate-200 dark:bg-slate-700 h-1.5 rounded-full overflow-hidden">
          <div class="bg-primary h-full rounded-full" style="width: ${Math.round(s.score)}%"></div>
        </div>
      </div>
    `).join("");
  }

  const radarScores = [0, 0, 0, 0, 0];
  const radarNames = ["Skill 1", "Skill 2", "Skill 3", "Skill 4", "Skill 5"];
  for (let i = 0; i < Math.min(5, skills.length); i += 1) {
    radarScores[i] = Number(skills[i].score) || 0;
    radarNames[i] = skills[i].name;
  }

  const polygon = document.getElementById("result-skill-radar-polygon");
  if (polygon) {
    polygon.setAttribute("points", radarScores.map((score, i) => polarPoint(score, i, 5)).join(" "));
  }

  const points = document.querySelectorAll(".result-skill-radar-point");
  points.forEach((el, i) => {
    const [x, y] = polarPoint(radarScores[i] || 0, i, 5).split(",");
    el.setAttribute("cx", x);
    el.setAttribute("cy", y);
  });

  for (let i = 0; i < 5; i += 1) {
    const labelEl = document.getElementById(`result-skill-label-${i}`);
    if (labelEl) labelEl.textContent = radarNames[i];
  }
}

async function loadLatestResultData(subjectId, seedData) {
  let data = seedData || {};

  if (!data || typeof data.scorePercent === "undefined") {
    try {
      if (await checkSession()) {
        const rows = await api("/api/history/placements", "GET", null, true);
        const latest = Array.isArray(rows)
          ? rows.find((x) => Number(x.subjectId) === Number(subjectId)) || rows[0]
          : null;
        if (latest) {
          data = {
            scorePercent: Number(latest.scorePercent || 0),
            level: latest.level || "L1"
          };
        }
      }
    } catch {
      // ignore
    }
  }

  try {
    if (await checkSession()) {
      const status = await api(`/api/subjects/${subjectId}/placement-result-status`, "GET", null, true);
      if (status && status.hasPlacementResult) {
        data = {
          ...data,
          scorePercent: typeof data.scorePercent === "number" ? data.scorePercent : Number(status.scorePercent || 0),
          level: data.level || status.level || "L1",
          skillAnalysisJson: data.skillAnalysisJson || status.skillAnalysisJson || null
        };
      }
    }
  } catch {
    // ignore
  }

  localStorage.setItem(KEYS.result, JSON.stringify(data || {}));
  return data || {};
}

async function initResult() {
  let data = JSON.parse(localStorage.getItem(KEYS.result) || "{}");
  const subjectId = getSubjectId();

  data = await loadLatestResultData(subjectId, data);

  const scorePercent = Number(data && data.scorePercent ? data.scorePercent : 0);
  const score10 = Math.round((scorePercent / 10) * 10) / 10;
  const level = (data && data.level) || "L1";

  if (data && data.preview) {
    toast("Day la che do xem truoc, chua co diem cham chinh thuc.", "warn");
  }

  setText("result-score", String(score10));
  setText("result-level", level);
  setText("result-focus", level === "L3" ? "Advanced Mixed Skills" : level === "L2" ? "Algebra" : "Core Foundations");

  const progressEl = document.getElementById("result-level-progress");
  if (progressEl) progressEl.style.width = `${level === "L3" ? 100 : level === "L2" ? 65 : 35}%`;

  const nextTarget = document.getElementById("result-next-target");
  if (nextTarget) {
    if (level === "L1") nextTarget.textContent = "Next level at 5 diem";
    else if (level === "L2") nextTarget.textContent = "Next level at 9 diem";
    else nextTarget.textContent = "Max level reached";
  }

  renderSkillAnalysis(data && data.skillAnalysisJson, level);

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
  let hasActiveSub = false;
  if (await checkSession()) {
    try {
      const subData = await api("/api/me/subscriptions", "GET", null, true);
      const active = (subData && subData.activeSubscriptions) || [];
      hasActiveSub = active.some((x) => Number(x.subjectId) === Number(subjectId));
    } catch {
      // ignore
    }
  }
  if (unlockBtn) {
    if (unlockLabel) unlockLabel.textContent = "Go to your roadmap";
    unlockBtn.addEventListener("click", async () => {
      if (!(await checkSession())) {
        goAuthWithRedirect("/placement-result", "placementTestResult.html");
        return;
      }
      localStorage.setItem("compassed_subject_id", String(subjectId));
      if (!hasActiveSub) {
        toast("Ban duoc hoc mien phi Module 1. Dang ky de mo khoa cac module tiep theo.", "warn");
      }
      nav(`/learning-roadmap?subjectId=${subjectId}`, `roadmap.html?subjectId=${subjectId}`);
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
      if (!hasActiveSub) {
        toast("Ban duoc hoc mien phi Module 1. Dang ky de mo khoa cac module tiep theo.", "warn");
      }
      nav(`/learning-roadmap?subjectId=${subjectId}`, `roadmap.html?subjectId=${subjectId}`);
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