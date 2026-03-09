import {
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
import { getLang } from "./i18n.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";

function escapeHtml(text) {
  return String(text ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function repairVietnameseText(text) {
  let value = String(text ?? "");
  const fixes = [
    ["B�i", "Bài"],
    ["ph�t", "phút"],
    ["N?i", "Nội"],
    ["h?c", "học"],
    ["l? tr\u00ecnh", "lộ trình"],
    ["Mu?n", "Muốn"],
    ["ti?p", "tiếp"],
    ["ph?i", "phải"],
    ["d?", "để"],
    ["m?", "mở"],
    ["kh\u00f3a", "khóa"],
    ["c?a", "của"]
  ];
  fixes.forEach(([from, to]) => {
    value = value.replaceAll(from, to);
  });
  return value;
}

function formatNotificationTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  const diffMs = Date.now() - date.getTime();
  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) return getLang() === "vi" ? "Vua xong" : "Just now";
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 30) return `${days}d ago`;
  return date.toLocaleDateString();
}

function setNotifBadge(unreadCount) {
  const badge = document.getElementById("road-notif-badge");
  if (!badge) return;
  const count = Number(unreadCount || 0);
  if (count > 0) {
    badge.textContent = count > 99 ? "99+" : String(count);
    badge.classList.remove("hidden");
    return;
  }
  badge.textContent = "0";
  badge.classList.add("hidden");
}

function renderNotificationGroups(items) {
  const listEl = document.getElementById("road-notif-list");
  if (!listEl) return [];
  if (!Array.isArray(items) || !items.length) {
    listEl.innerHTML = '<div class="text-sm text-slate-300 px-1 py-2">Ban chua co thong bao nao.</div>';
    return [];
  }
  const unread = items.filter((x) => !x.read);
  const read = items.filter((x) => x.read);
  const blocks = [];
  const pushGroup = (label, rows) => {
    if (!rows.length) return;
    blocks.push(`<div class="px-1 pt-2 pb-1 text-sm font-semibold text-slate-300">${label}</div>`);
    rows.forEach((item) => {
      blocks.push(`
        <article class="group rounded-xl border border-slate-700 bg-slate-800/60 p-3 transition-colors hover:bg-slate-700/80">
          <div class="flex items-start gap-3">
            <div class="flex-1 min-w-0">
              <h4 class="text-sm font-semibold text-slate-100 leading-5 break-words">${escapeHtml(item.title || "Thong bao")}</h4>
              <p class="mt-1 text-xs text-slate-300 leading-5 break-words">${escapeHtml(item.message || "")}</p>
              <p class="mt-2 text-[11px] text-slate-400">${formatNotificationTime(item.createdAt)}</p>
            </div>
            ${item.read ? "" : '<span class="mt-1 h-2.5 w-2.5 rounded-full bg-sky-400 flex-shrink-0"></span>'}
          </div>
        </article>`);
    });
  };
  pushGroup(getLang() === "vi" ? "Moi" : "New", unread);
  pushGroup(getLang() === "vi" ? "Truoc do" : "Earlier", read);
  listEl.innerHTML = blocks.join("");
  return unread.map((x) => x.id).filter((id) => typeof id === "number");
}

async function setupNotificationCenter() {
  const notifBtn = document.getElementById("road-notif-btn");
  const notifPanel = document.getElementById("road-notif-panel");
  const notifList = document.getElementById("road-notif-list");
  if (!notifBtn || !notifPanel || !notifList) return;

  let panelOpen = false;
  let loading = false;

  const closePanel = () => {
    panelOpen = false;
    notifPanel.classList.add("hidden");
  };

  const loadUnreadCount = async () => {
    try {
      const data = await api("/api/notifications/unread-count", "GET", null, true);
      setNotifBadge(data.unreadCount || 0);
    } catch {
      setNotifBadge(0);
    }
  };

  const markAsRead = async (ids) => {
    if (!ids.length) return;
    await Promise.allSettled(ids.map((id) => api(`/api/notifications/${id}/read`, "POST", null, true)));
    setNotifBadge(0);
  };

  const loadNotifications = async () => {
    if (loading) return;
    loading = true;
    notifList.innerHTML = '<div class="text-sm text-slate-300 px-1 py-2">Dang tai thong bao...</div>';
    try {
      const items = await api("/api/notifications", "GET", null, true);
      const unreadIds = renderNotificationGroups(items);
      await markAsRead(unreadIds);
    } catch (err) {
      notifList.innerHTML = `<div class="text-sm text-rose-300 px-1 py-2">Khong tai duoc thong bao: ${escapeHtml(err.message || "Unknown error")}</div>`;
    } finally {
      loading = false;
    }
  };

  notifBtn.addEventListener("click", async (e) => {
    e.preventDefault();
    e.stopPropagation();
    panelOpen = !panelOpen;
    notifPanel.classList.toggle("hidden", !panelOpen);
    if (panelOpen) await loadNotifications();
  });

  document.addEventListener("click", (e) => {
    if (!panelOpen) return;
    if (notifPanel.contains(e.target) || notifBtn.contains(e.target)) return;
    closePanel();
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && panelOpen) closePanel();
  });

  await loadUnreadCount();
}

function roadmapActionLabel(phase) {
  if (phase === "LESSONS") return "Refresh roadmap";
  if (phase === "MINI_TESTS") return "Submit mini test";
  if (phase === "FINAL_TEST") return "Submit final test";
  if (phase === "COURSE_COMPLETED") return "Roadmap completed";
  if (phase === "WAITING_PLACEMENT") return "Take placement test";
  if (phase === "LOCKED") return "Subscribe to unlock roadmap";
  return "Refresh roadmap";
}

function classLabelByTrack(track) {
  if (track === "GRADE_12") return "12";
  if (track === "UNI_PREP") return "On thi dai hoc";
  return "11";
}

function renderRoadmapStatus(roadmap, academicTrack) {
  const status = document.getElementById("roadmap-api-status");
  if (status) {
    const level = roadmap && roadmap.level ? roadmap.level : "-";
    const lessons = Array.isArray(roadmap && roadmap.lessons) ? roadmap.lessons : [];
    const completedModules = Math.min(5, lessons.filter((x) => x && x.completed).length);
    const progress = completedModules * 20;
    const replans = Number(roadmap && roadmap.replanCount ? roadmap.replanCount : 0);
    status.textContent = `Level ${level} | Progress: ${progress}% | Replan: ${replans}`;
  }
  const title = document.getElementById("roadmap-title");
  if (title && roadmap && roadmap.subjectName) {
    const cls = classLabelByTrack(academicTrack);
    title.textContent = `${roadmap.subjectName} ${cls} - ${roadmap.level || "-"}`;
  }
}

function formatHoursMinutes(totalMinutes) {
  const mins = Math.max(0, Math.round(Number(totalMinutes || 0)));
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return `${h}h ${String(m).padStart(2, "0")}m`;
}

function renderWeeklyProgress(roadmap) {
  const lessons = Array.isArray(roadmap && roadmap.lessons) ? roadmap.lessons : [];
  const completedLessons = lessons.filter((x) => x && x.completed);
  const totalMinutes = completedLessons.reduce((sum, x) => sum + Number(x.estimatedMinutes || 0), 0);
  const spentEl = document.getElementById("road-time-spent");
  if (spentEl) spentEl.textContent = formatHoursMinutes(totalMinutes);

  const lessonsEl = document.getElementById("road-lessons-completed");
  if (lessonsEl) lessonsEl.textContent = "4";

  const weights = [0.18, 0.23, 0.2, 0.25, 0.05, 0.05, 0.04];
  const dayMinutes = weights.map((w) => Math.round(totalMinutes * w));
  const maxValue = Math.max(1, ...dayMinutes);
  for (let i = 0; i < 7; i += 1) {
    const bar = document.getElementById(`road-wbar-${i}`);
    if (!bar) continue;
    const value = dayMinutes[i];
    const px = value <= 0 ? 8 : Math.max(12, Math.min(96, Math.round((value / maxValue) * 96)));
    bar.style.height = `${px}px`;
    if (i <= 3) {
      bar.className = i === 3
        ? "w-full bg-primary rounded-t-sm"
        : i === 2
          ? "w-full bg-primary/40 rounded-t-sm"
          : "w-full bg-primary/20 rounded-t-sm";
    } else {
      bar.className = "w-full bg-background-light border border-border-light border-b-0 rounded-t-sm";
    }
  }
}

function renderStudyStreak(streakDays) {
  const el = document.getElementById("road-study-streak");
  if (!el) return;
  const days = Math.max(0, Number(streakDays || 0));
  el.textContent = `${days} DAY STREAK!`;
}

function isRoadmapInitialized(aiRoadmap) {
  return Boolean(aiRoadmap && aiRoadmap.roadmapInitialized);
}

function roadmapBuildLabel(aiRoadmap) {
  if (!isRoadmapInitialized(aiRoadmap)) return "Khởi tạo roadmap";
  const remaining = Math.max(0, Number(aiRoadmap && aiRoadmap.refreshCountRemaining ? aiRoadmap.refreshCountRemaining : 0));
  if (remaining > 0) return `Refresh roadmap (${remaining} lượt free)`;
  return "Refresh roadmap";
}

function setRoadmapBuildButton(btn, aiRoadmap) {
  if (!btn) return;
  const locked = btn.getAttribute("data-locked") === "1";
  if (locked) {
    btn.disabled = true;
    return;
  }
  btn.disabled = false;
  const label = roadmapBuildLabel(aiRoadmap);
  btn.innerHTML = `${escapeHtml(label)}<span class="material-symbols-outlined text-[20px]">arrow_forward</span>`;
}

function startRoadmapBuildDots(btn, baseText) {
  if (!btn) return () => {};
  let step = 0;
  btn.disabled = true;
  const render = () => {
    const dots = ".".repeat((step % 3) + 1);
    btn.innerHTML = `${escapeHtml(baseText)}${dots}`;
    step += 1;
  };
  render();
  const timer = window.setInterval(render, 400);
  return () => {
    window.clearInterval(timer);
    btn.disabled = false;
  };
}

async function maybePromptCheckpoint(roadmap, subjectId) {
  if (!roadmap || !roadmap.checkpointDue) return roadmap;
  const completedModules = Number(roadmap.completedModules || 0);
  if (completedModules <= 0) return roadmap;
  const key = `compassed_checkpoint_seen_${subjectId}_${completedModules}`;
  if (sessionStorage.getItem(key) === "1") return roadmap;
  sessionStorage.setItem(key, "1");
  const keepUp = window.confirm(
    `B?n dÃ£ hoÃ n thÃ nh ${completedModules} module. B?n cÃ³ dang theo k?p ti?n d? roadmap khÃ´ng?`
  );
  if (keepUp) {
    toast("T?t, ti?p t?c h?c theo roadmap hi?n t?i.");
    return roadmap;
  }
  try {
    const replanned = await api(`/api/subjects/${subjectId}/roadmap/replan`, "POST", null, true);
    toast("ÃÃ£ di?u ch?nh roadmap theo ti?n d? ch?m hon.", "warn");
    return replanned || roadmap;
  } catch (err) {
    toast(`KhÃ´ng th? replan roadmap: ${err.message}`, "error");
    return roadmap;
  }
}

async function maybePromptUpLevel(roadmap, subjectId) {
  if (!roadmap || !roadmap.upLevelEligible) return roadmap;
  const level = String(roadmap.level || "");
  const key = `compassed_up_level_seen_${subjectId}_${level}_${Number(roadmap.completedModules || 0)}`;
  if (sessionStorage.getItem(key) === "1") return roadmap;
  sessionStorage.setItem(key, "1");
  const wantUpLevel = window.confirm("Báº¡n Ä‘ang há»c nhanh hÆ¡n lá»™ trÃ¬nh. Báº¡n cÃ³ muá»‘n lÃ m bÃ i thi up-level khÃ´ng?");
  if (!wantUpLevel) {
    toast("Báº¡n chá»n tiáº¿p tá»¥c há»c theo lá»™ trÃ¬nh hiá»‡n táº¡i.");
    return roadmap;
  }
  const raw = window.prompt("Nháº­p Ä‘iá»ƒm bÃ i thi up-level (0-100):", "75");
  const score = Math.max(0, Math.min(100, Number(raw)));
  if (Number.isNaN(score)) {
    toast("Äiá»ƒm khÃ´ng há»£p lá»‡.", "warn");
    return roadmap;
  }
  try {
    const updated = await api(`/api/subjects/${subjectId}/roadmap/up-level-test`, "POST", { score }, true);
    if (score >= 70) {
      toast("Up-level thÃ nh cÃ´ng.");
    } else {
      toast("ChÆ°a Ä‘áº¡t Ä‘iá»ƒm up-level, tiáº¿p tá»¥c há»c level hiá»‡n táº¡i.", "warn");
    }
    return updated || roadmap;
  } catch (err) {
    toast(`KhÃ´ng thá»ƒ ná»™p bÃ i thi up-level: ${err.message}`, "error");
    return roadmap;
  }
}
function buildModuleRows(roadmap, aiRoadmap, userId) {
  const lessons = Array.isArray(roadmap && roadmap.lessons) ? roadmap.lessons : [];
  const miniTests = Array.isArray(roadmap && roadmap.miniTests) ? roadmap.miniTests : [];
  const frameworkModules = Array.isArray(aiRoadmap && aiRoadmap.frameworkModules) ? aiRoadmap.frameworkModules : [];
  const guideModules = Array.isArray(aiRoadmap && aiRoadmap.roadmapModules) ? aiRoadmap.roadmapModules : [];
  const total = Math.max(5, lessons.length, frameworkModules.length, guideModules.length);
  const rows = [];
  let prevModuleDone = true;
  for (let i = 0; i < total; i += 1) {
    const lesson = lessons[i] || null;
    const miniTest = miniTests[i] || null;
    const guideModule = guideModules[i] || null;
    const title = (frameworkModules[i] || (guideModule && guideModule.title) || (lesson && lesson.title) || `Module ${i + 1}`).trim();
    const lessonDone = !!(lesson && lesson.completed);
    const miniTestDone = !!(miniTest && (miniTest.completed || miniTest.score != null));
    const localModuleDone = getCompletedLessonCount(userId, roadmap && roadmap.subjectId, i) >= 10;
    const moduleDone = (lessonDone && miniTestDone) || localModuleDone;
    const needSubscription = i > 0 && !(roadmap && roadmap.subscribed);
    const lockBySequence = i > 0 && !prevModuleDone;
    const locked = i === 0 ? false : (needSubscription || lockBySequence);
    rows.push({
      index: i,
      userId: Number(userId || 0),
      subjectId: roadmap && roadmap.subjectId ? Number(roadmap.subjectId) : 0,
      title,
      lesson,
      guideModule,
      miniTest,
      lessonDone,
      miniTestDone,
      completed: moduleDone,
      needSubscription,
      locked
    });
    prevModuleDone = moduleDone;
  }
  return rows.slice(0, 5);
}

function normalizeModuleTitle(raw, index) {
  const text = String(raw || "").trim();
  if (!text) return `Chuyen de ${index + 1}`;
  return text.replace(/^module\s*\d+\s*[:.\-]\s*/i, "").trim() || `Chuyen de ${index + 1}`;
}

function summarizeContent(content) {
  const text = String(content || "").trim();
  if (!text) return "Noi dung chuyen de duoc ca nhan hoa theo level va lo trinh hoc.";
  const first = text.split(".").map((s) => s.trim()).find((s) => s.length > 0) || text;
  return first.length > 110 ? `${first.slice(0, 107)}...` : first;
}

function enrichRows(rows) {
  return rows.map((row) => {
    const displayTitle = normalizeModuleTitle(row.title || (row.lesson && row.lesson.title), row.index);
    const guideModule = row.guideModule || {};
    const focusSkills = Array.isArray(guideModule.focusSkills) ? guideModule.focusSkills.filter(Boolean) : [];
    const studyGuide = String(guideModule.studyGuide || "").trim();
    const displayDesc = summarizeContent(studyGuide || (row.lesson && row.lesson.content));
    const displayMeta = [
      focusSkills.length ? `Kỹ năng trọng tâm: ${focusSkills.slice(0, 3).join(", ")}` : "",
      guideModule.targetScore != null ? `Mục tiêu: ${Number(guideModule.targetScore)}%` : "",
      guideModule.duration ? `Thời lượng: ${guideModule.duration}` : ""
    ].filter(Boolean).join(" | ");
    return { ...row, displayTitle, displayDesc, displayMeta };
  });
}

function moduleLessonProgressKey(userId, subjectId, moduleIndex) {
  return `compassed_module_lessons_done_u${Number(userId || 0)}_s${Number(subjectId || 0)}_m${Number(moduleIndex || 0)}`;
}

function getCompletedLessonCount(userId, subjectId, moduleIndex) {
  const key = moduleLessonProgressKey(userId, subjectId, moduleIndex);
  const value = Number(localStorage.getItem(key) || 0);
  return Math.max(0, Math.min(10, Number.isFinite(value) ? value : 0));
}

function setCompletedLessonCount(userId, subjectId, moduleIndex, count) {
  const key = moduleLessonProgressKey(userId, subjectId, moduleIndex);
  localStorage.setItem(key, String(Math.max(0, Math.min(10, Number(count || 0)))));
}

function buildLessonPlanRows(row) {
  const guideModule = row && row.guideModule ? row.guideModule : {};
  const rawPlan = Array.isArray(guideModule.lessonPlan) ? guideModule.lessonPlan : [];
  const lessons = rawPlan
    .map((x, idx) => ({
      lessonNo: Number(x && x.lessonNo ? x.lessonNo : idx + 1),
      title: repairVietnameseText(String((x && x.title) || "").trim()),
      summary: repairVietnameseText(String((x && x.summary) || "").trim()),
      duration: repairVietnameseText(String((x && x.duration) || "").trim())
    }))
    .filter((x) => x.title || x.summary);

  const out = [];
  for (let i = 0; i < 10; i += 1) {
    const existing = lessons[i];
    if (existing) {
      out.push(existing);
      continue;
    }
    out.push({
      lessonNo: i + 1,
      title: `Bài ${String(i + 1).padStart(2, "0")} - ${row.displayTitle}`,
      summary: row.displayDesc || "Nội dung bài học được cá nhân hóa theo level.",
      duration: "45 phút"
    });
  }
  return out;
}

function renderLegacyRoadmapBlocks(rows, selectedIndex, onSelect, roadmapInitialized) {
  const richRows = enrichRows(rows);
  const topCards = document.querySelector(".grid.grid-cols-1.sm\\:grid-cols-3.gap-4.mb-8");
  if (topCards) {
    topCards.innerHTML = richRows.slice(0, 3).map((row) => {
      const active = Number(row.index) === Number(selectedIndex);
      const completed = row.completed;
      const locked = row.locked && !completed && !active;
      const cardClass = active
        ? "relative flex flex-col gap-3 rounded-xl border-2 border-primary bg-primary-light/10 p-4 shadow-sm roadmap-module-card cursor-pointer"
        : locked
          ? "relative flex flex-col gap-3 rounded-xl border border-border-light bg-background-light p-4 opacity-75 roadmap-module-card cursor-pointer"
          : "group relative flex flex-col gap-3 rounded-xl border border-border-light bg-background-light p-4 hover:border-primary/30 transition-colors roadmap-module-card cursor-pointer";
      const title = escapeHtml(row.displayTitle);
      const statusText = completed ? "Hoàn thành module" : locked ? "Locked" : "Unlocked";
      const rightIcon = completed ? "check_circle" : active ? "play_circle" : (locked ? "lock" : "lock_open");
      const rightIconColor = completed ? "text-success" : active ? "text-primary animate-pulse" : locked ? "text-text-sub" : "text-emerald-600";
      return `
        <div class="${cardClass}" data-index="${row.index}">
          <div class="absolute top-4 right-4 ${rightIconColor}">
            <span class="material-symbols-outlined filled">${rightIcon}</span>
          </div>
          <div class="size-10 rounded-lg ${active ? "bg-primary text-white" : "bg-white text-text-main"} flex items-center justify-center border border-border-light">
            <span class="material-symbols-outlined">${active ? "show_chart" : "functions"}</span>
          </div>
          <div>
            <p class="text-xs font-semibold ${active ? "text-primary" : "text-text-sub"} uppercase tracking-wider">MODULE ${row.index + 1}${active ? " • CURRENT" : ""}</p>
            <h4 class="font-bold text-text-main mt-1">${title}</h4>
            <p class="text-sm ${completed ? "text-success" : "text-text-sub"} mt-1">${statusText}</p>
          </div>
        </div>`;
    }).join("");
    topCards.querySelectorAll(".roadmap-module-card").forEach((card) => {
      card.addEventListener("click", () => onSelect(Number(card.getAttribute("data-index") || 0)));
    });
  }

  const pathWrap = document.querySelector(".relative.pl-2.sm\\:pl-4.pr-2");
  if (!pathWrap) return;
  const row = richRows.find((x) => Number(x.index) === Number(selectedIndex)) || richRows[0];
  if (!row) {
    pathWrap.innerHTML = "";
    return;
  }

  const requireSubscription = !!row.needSubscription;
  const lessonPlan = buildLessonPlanRows(row);
  const completedCount = requireSubscription ? 0 : getCompletedLessonCount(row.userId, row.subjectId, row.index);
  const visibleLessonPlan = lessonPlan.slice(0, Math.min(10, completedCount + 1));

  if (!roadmapInitialized) {
    pathWrap.innerHTML = `
      <div class="mb-5 rounded-xl border border-border-light bg-white p-5 shadow-sm">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h4 class="text-xl font-bold text-text-main">${escapeHtml(row.displayTitle)}</h4>
            <p class="text-sm text-text-sub mt-1">Roadmap đang chờ khởi tạo theo level và lớp của bạn.</p>
            <p class="text-xs text-text-sub mt-2">Nhấn "Khởi tạo roadmap" để AI tạo lộ trình học cá nhân hóa, sau đó hệ thống mới hiển thị danh sách bài học.</p>
          </div>
          <button id="roadmap-start-lesson-btn" data-locked="0" class="flex-none bg-primary hover:bg-primary-dark text-white px-6 py-3 rounded-lg font-bold shadow-sm shadow-primary/30 transition-all flex items-center justify-center gap-2">Khởi tạo roadmap<span class="material-symbols-outlined text-[20px]">arrow_forward</span></button>
        </div>
      </div>
      <div class="rounded-xl border border-dashed border-border-light bg-background-light p-6 text-sm text-text-sub">
        Các bài học sẽ hiển thị sau khi khởi tạo roadmap thành công.
      </div>`;
    return;
  }

  const lessonRowsHtml = visibleLessonPlan.map((lesson, idx) => {
    const done = idx < completedCount;
    const isCurrent = idx === completedCount;
    const badge = done
      ? '<span class="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">Completed</span>'
      : requireSubscription
        ? '<span class="inline-flex items-center rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600 ring-1 ring-inset ring-slate-300">Locked</span>'
        : isCurrent
          ? '<span class="inline-flex items-center rounded-md bg-primary/10 px-2 py-1 text-xs font-medium text-primary ring-1 ring-inset ring-primary/20">Up Next</span>'
          : '<span class="inline-flex items-center rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600 ring-1 ring-inset ring-slate-300">Locked</span>';
    const actionButton = done
      ? '<button type="button" class="rounded-lg bg-emerald-100 text-emerald-700 px-3 py-2 text-sm font-semibold cursor-default">Đã xong</button>'
      : requireSubscription
        ? '<button type="button" class="rounded-lg bg-slate-200 text-slate-500 px-3 py-2 text-sm font-semibold cursor-not-allowed">Đã khóa</button>'
        : `<button type="button" class="lesson-start-btn rounded-lg bg-primary hover:bg-primary-dark text-white px-4 py-2 text-sm font-semibold" data-lesson-index="${idx}">Bắt đầu</button>`;
    return `
      <div class="group flex gap-4 sm:gap-6 mb-6 relative ${requireSubscription ? "opacity-70" : ""}">
        <div class="flex-none">
          <div class="size-10 sm:size-14 rounded-full ${done ? "bg-success" : "bg-primary"} text-white flex items-center justify-center ring-4 ${done ? "ring-white" : "ring-primary/20"} shadow-md z-10 relative">
            <span class="material-symbols-outlined filled text-xl sm:text-2xl">${done ? "check" : "play_arrow"}</span>
          </div>
        </div>
        <div class="flex-grow bg-white rounded-xl border ${done ? "border-green-300" : "border-border-light"} p-4 flex items-center justify-between shadow-sm">
          <div>
            <div class="flex items-center gap-2 mb-1">
              ${badge}
              <span class="text-xs text-text-sub font-medium">${escapeHtml(lesson.duration || "45 phút")}</span>
            </div>
            <h5 class="font-bold text-text-main text-lg">${escapeHtml(lesson.title || `Bài ${lesson.lessonNo}`)}</h5>
            <p class="text-text-sub text-sm">${escapeHtml(lesson.summary || "Nội dung bài học theo lộ trình.")}</p>
          </div>
          <div class="ml-3">${actionButton}</div>
        </div>
      </div>`;
  }).join("");
  const canGoNextModule = !requireSubscription && completedCount >= 10 && Number(row.index) < 4;
  const nextModuleCta = canGoNextModule
    ? `
      <div class="mt-2 mb-2 flex justify-end">
        <button type="button" id="roadmap-next-module-btn" class="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-bold text-white hover:bg-primary-dark">
          Tiếp theo
          <span class="material-symbols-outlined text-[18px]">arrow_forward</span>
        </button>
      </div>`
    : "";

  pathWrap.innerHTML = `
    <div class="mb-5 rounded-xl border border-border-light bg-white p-5 shadow-sm">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h4 class="text-xl font-bold text-text-main">${escapeHtml(row.displayTitle)}</h4>
          <p class="text-sm text-text-sub mt-1">${escapeHtml(row.displayDesc)}</p>
          ${row.displayMeta ? `<p class="text-xs text-text-sub mt-2">${escapeHtml(row.displayMeta)}</p>` : ""}
        </div>
        <button id="roadmap-start-lesson-btn" data-locked="${requireSubscription ? "1" : "0"}" class="flex-none bg-primary hover:bg-primary-dark text-white px-6 py-3 rounded-lg font-bold shadow-sm shadow-primary/30 transition-all flex items-center justify-center gap-2 ${requireSubscription ? "opacity-60 pointer-events-none" : ""}">Refresh roadmap<span class="material-symbols-outlined text-[20px]">arrow_forward</span></button>
      </div>
      ${requireSubscription ? '<div class="mt-3 rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">Muốn học tiếp phải đăng ký để mở khóa các bài học của module này.</div>' : ""}
    </div>
    ${lessonRowsHtml}
    ${nextModuleCta}`;

  pathWrap.querySelectorAll(".lesson-start-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const idx = Number(btn.getAttribute("data-lesson-index") || -1);
      if (idx !== completedCount) return;
      setCompletedLessonCount(row.userId, row.subjectId, row.index, completedCount + 1);
      renderLegacyRoadmapBlocks(rows, selectedIndex, onSelect, roadmapInitialized);
      const startBtn = document.getElementById("roadmap-start-lesson-btn");
      if (startBtn) startBtn.textContent = roadmapActionLabel("LESSONS");
      toast("Đã hoàn thành bài học, mở khóa bài tiếp theo.");
    });
  });

  const nextModuleBtn = pathWrap.querySelector("#roadmap-next-module-btn");
  if (nextModuleBtn) {
    nextModuleBtn.addEventListener("click", () => {
      const nextIndex = Number(row.index) + 1;
      onSelect(nextIndex);
      toast(`Đã chuyển sang Module ${nextIndex + 1}.`);
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  }
}

async function initRoadmap() {
  if (!(await checkSession())) {
    goAuthWithRedirect("/learning-roadmap", "roadmap.html");
    return;
  }
  const subjectId = getSubjectId();
  let roadmap = null;
  let aiRoadmap = null;
  let myProfile = null;
  let currentUserId = 0;
  let streakDays = 0;
  let selectedModuleIndex = 0;

  const reloadRoadmap = async () => {
    roadmap = await api(`/api/subjects/${subjectId}/roadmap`, "GET", null, true);
    const track = (aiRoadmap && aiRoadmap.academicTrack) || (myProfile && myProfile.academicTrack) || "GRADE_11";
    renderRoadmapStatus(roadmap, track);
    renderWeeklyProgress(roadmap);
    roadmap = await maybePromptCheckpoint(roadmap, subjectId);
    roadmap = await maybePromptUpLevel(roadmap, subjectId);
    renderRoadmapStatus(roadmap, track);
    renderWeeklyProgress(roadmap);
  };

  const genericAction = async () => {
    if (!roadmap) return;
    try {
      showLoading("Updating roadmap...");
      if (roadmap.phase === "LOCKED") {
        if (!roadmap.placementReady) {
          nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
          return;
        }
        nav("/checkout", "checkout.html");
        return;
      }
      if (!roadmap.subscribed) {
        toast("Báº¡n Ä‘ang há»c thá»­ miá»…n phÃ­ Module 1. ÄÄƒng kÃ½ Ä‘á»ƒ má»Ÿ khÃ³a cÃ¡c module tiáº¿p theo.", "warn");
        return;
      }
      if (roadmap.phase === "WAITING_PLACEMENT") {
        nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
        return;
      }
      if (roadmap.phase === "LESSONS") {
        const nextLesson = (roadmap.lessons || []).find((x) => !x.completed);
        if (nextLesson) {
          await api(`/api/lessons/${nextLesson.id}/complete`, "POST", {
            subjectId,
            timeSpentSeconds: Math.max(300, Number(nextLesson.estimatedMinutes || 10) * 60),
            score: 100
          }, true);
        }
      } else if (roadmap.phase === "MINI_TESTS") {
        const nextMini = (roadmap.miniTests || []).find((x) => !x.completed);
        if (nextMini) {
          const raw = prompt("Mini test score (0-100):", "75");
          const score = Math.max(0, Math.min(100, Number(raw)));
          await api(`/api/subjects/${subjectId}/mini-tests/${nextMini.id}/submit`, "POST", { score }, true);
        }
      } else if (roadmap.phase === "FINAL_TEST") {
        const raw = prompt("Final test score (0-100):", "75");
        const score = Math.max(0, Math.min(100, Number(raw)));
        await api(`/api/subjects/${subjectId}/final-test/submit`, "POST", { score }, true);
      }
      await reloadRoadmap();
      await reloadAiRoadmap();
    } catch (err) {
      toast(`Roadmap action failed: ${err.message}`, "error");
    } finally {
      hideLoading();
    }
  };

  const handleRoadmapBuildAction = async () => {
    const button = document.getElementById("roadmap-start-lesson-btn");
    if (!button) return;
    const initialized = isRoadmapInitialized(aiRoadmap);
    const action = initialized ? "refresh" : "initialize";
    if (initialized) {
      const canRefresh = Boolean(aiRoadmap && aiRoadmap.canRefresh);
      const remaining = Math.max(0, Number(aiRoadmap && aiRoadmap.refreshCountRemaining ? aiRoadmap.refreshCountRemaining : 0));
      if (!canRefresh || remaining <= 0) {
        toast("Bạn đã dùng hết lượt refresh miễn phí cho môn này.", "warn");
        return;
      }
      const ok = window.confirm(`Bạn có muốn refresh roadmap không? Còn ${remaining} lượt miễn phí.`);
      if (!ok) return;
    } else {
      toast("Roadmap đang được khởi tạo cho riêng bạn...");
    }

    const stopDots = startRoadmapBuildDots(button, initialized ? "Đang refresh roadmap" : "Đang khởi tạo roadmap");
    try {
      aiRoadmap = await api(`/api/me/subjects/${subjectId}/ai-roadmap?action=${action}`, "GET", null, true);
      if (action === "initialize") {
        toast("Đã khởi tạo roadmap thành công.");
      } else {
        toast("Đã refresh roadmap thành công.");
      }
      await reloadAiRoadmap("view");
    } catch (err) {
      toast(`Không thể ${action === "initialize" ? "khởi tạo" : "refresh"} roadmap: ${err.message}`, "error");
      setRoadmapBuildButton(button, aiRoadmap);
    } finally {
      stopDots();
    }
  };

  const reloadAiRoadmap = async (mode = "view") => {
    try {
      aiRoadmap = await api(`/api/me/subjects/${subjectId}/ai-roadmap?action=${mode}`, "GET", null, true);
    } catch {
      aiRoadmap = null;
    }
    const track = (aiRoadmap && aiRoadmap.academicTrack) || (myProfile && myProfile.academicTrack) || "GRADE_11";
    renderRoadmapStatus(roadmap, track);
    const rows = buildModuleRows(roadmap, aiRoadmap, currentUserId);
    const firstOpen = rows.find((x) => !x.locked && !x.completed) || rows[0];
    if (firstOpen && (selectedModuleIndex == null || selectedModuleIndex < 0 || selectedModuleIndex > 4)) {
      selectedModuleIndex = firstOpen.index;
    }
    const rerender = () => {
      renderLegacyRoadmapBlocks(rows, selectedModuleIndex, (nextIndex) => {
        selectedModuleIndex = nextIndex;
        rerender();
      }, isRoadmapInitialized(aiRoadmap));
      const btn = document.getElementById("roadmap-start-lesson-btn");
      if (btn) {
        setRoadmapBuildButton(btn, aiRoadmap);
        btn.addEventListener("click", handleRoadmapBuildAction);
      }
    };
    rerender();
  };

  try {
    showLoading("Loading roadmap...");
    try {
      const [profileResp, subscriptionResp] = await Promise.all([
        api("/api/me/profile", "GET", null, true),
        api("/api/me/subscriptions", "GET", null, true)
      ]);
      myProfile = profileResp;
      currentUserId = Number((profileResp && profileResp.id) || 0);
      streakDays = Number((subscriptionResp && subscriptionResp.studyStreakDays) || 0);
      renderStudyStreak(streakDays);
    } catch {
      myProfile = null;
      currentUserId = 0;
      streakDays = 0;
      renderStudyStreak(0);
    }
    await reloadRoadmap();
    if (roadmap && !roadmap.placementReady) {
      const goPlacement = window.confirm("Báº¡n cáº§n lÃ m placement test vÃ  cÃ³ káº¿t quáº£ trÆ°á»›c khi vÃ o roadmap mÃ´n nÃ y. LÃ m placement test ngay?");
      if (goPlacement) {
        nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
      } else {
        nav("/roadmap-dashboard", "roadmapDashboard.html");
      }
      return;
    }
    await reloadAiRoadmap();
    if (roadmap && roadmap.phase === "WAITING_PLACEMENT") {
      toast("You need to complete placement test for this subject first.", "warn");
    }
  } catch (err) {
    toast(`Roadmap load failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  const resume = document.getElementById("roadmap-resume-btn");
  if (resume) resume.addEventListener("click", genericAction);
  const review = document.getElementById("roadmap-review-btn");
  if (review) review.addEventListener("click", genericAction);
  const quick1 = document.getElementById("roadmap-quick-practice-1");
  if (quick1) quick1.addEventListener("click", genericAction);
  const quick2 = document.getElementById("roadmap-quick-practice-2");
  if (quick2) quick2.addEventListener("click", genericAction);

  const syllabusBtn = document.getElementById("roadmap-syllabus-btn");
  if (syllabusBtn) syllabusBtn.addEventListener("click", () => toast("Syllabus loaded"));
  const historyBtn = document.getElementById("roadmap-history-btn");
  if (historyBtn) historyBtn.addEventListener("click", () => nav("/history", "history.html"));
  const backAllCourses = document.getElementById("road-back-all-courses");
  if (backAllCourses) backAllCourses.addEventListener("click", (e) => {
    e.preventDefault();
    nav("/roadmap-dashboard", "roadmapDashboard.html");
  });
  const navDash = document.getElementById("road-nav-dashboard");
  if (navDash) navDash.addEventListener("click", () => nav("/dashboard", "dashboard.html"));
  const navHistory = document.getElementById("road-nav-history");
  if (navHistory) navHistory.addEventListener("click", () => nav("/history", "history.html"));
  const navProfile = document.getElementById("road-nav-profile");
  if (navProfile) navProfile.addEventListener("click", () => openInlineProfilePanel());
  const navOverview = document.getElementById("road-nav-overview");
  if (navOverview) navOverview.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));
  const navLogout = document.getElementById("road-nav-logout");
  if (navLogout) navLogout.addEventListener("click", () => {
    clearAuth();
    nav("/landing", "landingPage.html");
  });
  const menuBtn = document.getElementById("road-menu-btn");
  if (menuBtn) {
    menuBtn.addEventListener("click", () => {
      const sidebar = document.querySelector("body > div > aside");
      if (!sidebar) return;
      if (sidebar.classList.contains("hidden")) {
        sidebar.classList.remove("hidden");
        sidebar.classList.add("flex");
      } else {
        sidebar.classList.add("hidden");
        sidebar.classList.remove("flex");
      }
    });
  }

  await setupNotificationCenter();
}

export { initRoadmap };


