import {
  api,
  checkSession,
  clearAuth,
  currentRole,
  getAuth,
  getSubjectId,
  goAuthWithRedirect,
  hideLoading,
  nav,
  showLoading,
  toast
} from "./core.js";

function parseGuide(guideJson) {
  if (!guideJson) return {};
  if (typeof guideJson === "object") return guideJson;
  try {
    return JSON.parse(String(guideJson));
  } catch (e) {
    return { rawText: String(guideJson) };
  }
}

function escapeHtml(text) {
  return String(text ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}

function normalizeIntent(text) {
  return String(text || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replaceAll("đ", "d");
}

function appendMessage(role, text) {
  const list = document.getElementById("rt-chat-list");
  if (!list) return;
  const row = document.createElement("div");
  row.className = role === "user" ? "flex justify-end" : "flex justify-start";

  const bubble = document.createElement("div");
  bubble.className =
    (role === "user" ? "bg-blue-600 text-white" : "bg-white text-slate-800 border border-slate-200") +
    " max-w-[82%] rounded-xl px-4 py-3 text-sm whitespace-pre-wrap";
  bubble.textContent = text;
  row.appendChild(bubble);
  list.appendChild(row);
  list.scrollTop = list.scrollHeight;
}

function summarizeProgress(roadmap) {
  const lessons = Array.isArray(roadmap && roadmap.lessons) ? roadmap.lessons : [];
  const mini = Array.isArray(roadmap && roadmap.miniTests) ? roadmap.miniTests : [];
  const doneLessons = lessons.filter((x) => x && x.completed).length;
  const doneMini = mini.filter((x) => x && x.completed).length;
  return {
    doneLessons,
    totalLessons: lessons.length,
    doneMini,
    totalMini: mini.length
  };
}

function renderContext(model) {
  const roadmap = model.roadmap || {};
  const aiRoadmap = model.aiRoadmap || {};
  const subscribed = !!roadmap.subscribed;
  const progress = summarizeProgress(roadmap);
  const level = roadmap.level || aiRoadmap.level || "N/A";
  const phase = roadmap.phase || "N/A";
  const progressPercent = Number(roadmap.progressPercent || 0);
  const placementScore =
    aiRoadmap.placementScorePercent != null
      ? `${Number(aiRoadmap.placementScorePercent).toFixed(1)}%`
      : roadmap.placementReady
      ? "READY"
      : "NOT_READY";

  const subjectLabel = document.getElementById("rt-subject-label");
  const levelEl = document.getElementById("rt-level");
  const phaseEl = document.getElementById("rt-phase");
  const progressEl = document.getElementById("rt-progress");
  const placementEl = document.getElementById("rt-placement");
  const nextEl = document.getElementById("rt-next-step");
  const lessonEl = document.getElementById("rt-lesson-summary");
  const miniEl = document.getElementById("rt-mini-summary");
  const barEl = document.getElementById("rt-progress-bar");

  if (subjectLabel) subjectLabel.textContent = `Môn học ID: ${roadmap.subjectId || aiRoadmap.subjectId || "-"}`;
  if (levelEl) levelEl.textContent = String(level);
  if (phaseEl) phaseEl.textContent = String(phase);
  if (progressEl) progressEl.textContent = `${progressPercent}%`;
  if (placementEl) placementEl.textContent = placementScore;
  if (nextEl) nextEl.textContent = !subscribed ? "Đăng ký môn để mở roadmap" : String(roadmap.nextStep || "N/A");
  if (lessonEl) lessonEl.textContent = `Bài học: ${progress.doneLessons}/${progress.totalLessons}`;
  if (miniEl) miniEl.textContent = `Mini test: ${progress.doneMini}/${progress.totalMini}`;
  if (barEl) barEl.style.width = `${Math.max(0, Math.min(100, progressPercent))}%`;
}

function roadmapReply(message, model) {
  const text = normalizeIntent(message);
  const roadmap = model.roadmap || {};
  const guide = model.guide || {};
  const subscribed = !!roadmap.subscribed;
  const lessons = Array.isArray(roadmap.lessons) ? roadmap.lessons : [];
  const miniTests = Array.isArray(roadmap.miniTests) ? roadmap.miniTests : [];
  const weakTopics = Array.isArray(guide.weak_topics) ? guide.weak_topics : [];
  const recommendations = Array.isArray(guide.recommendations) ? guide.recommendations : [];
  const steps = Array.isArray(guide.roadmapSteps) ? guide.roadmapSteps : [];
  const currentLesson = lessons.find((x) => !x.completed);
  const currentMini = miniTests.find((x) => !x.completed);

  if (text.includes("tom tat") || text.includes("summary") || text.includes("roadmap")) {
    return [
      `Môn: ${roadmap.subjectName || model.aiRoadmap?.subjectName || "N/A"}`,
      `Level: ${roadmap.level || model.aiRoadmap?.level || "N/A"}`,
      `Giai đoạn hiện tại: ${roadmap.phase || "N/A"}`,
      `Tiến độ: ${roadmap.progressPercent || 0}%`,
      `Bước tiếp theo: ${roadmap.nextStep || "N/A"}`
    ].join("\n");
  }

  if (text.includes("tiep theo") || text.includes("next")) {
    if (!subscribed) return "Môn này chưa đăng ký. Bạn cần đăng ký để mở roadmap.";
    if (roadmap.phase === "WAITING_PLACEMENT") return "Bạn cần hoàn thành placement test cho môn này trước.";
    if (roadmap.phase === "LOCKED") return "Môn này chưa mở khóa. Bạn cần đăng ký gói trước.";
    if (roadmap.phase === "LESSONS" && currentLesson) {
      return `Bước tiếp theo: hoàn thành bài học "${currentLesson.title || "Lesson"}".`;
    }
    if (roadmap.phase === "MINI_TESTS" && currentMini) {
      return `Bước tiếp theo: nộp "${currentMini.title || "Mini test"}".`;
    }
    if (roadmap.phase === "FINAL_TEST") return "Bước tiếp theo: làm final test của level hiện tại.";
    if (roadmap.phase === "COURSE_COMPLETED") return "Bạn đã hoàn thành roadmap môn này.";
  }

  if (text.includes("yeu") || text.includes("weak")) {
    if (!weakTopics.length) return "Chưa có danh sách điểm yếu chi tiết. Hãy tiếp tục làm mini test để AI cập nhật.";
    return `Các điểm cần cải thiện:\n${weakTopics.map((x) => `- ${x}`).join("\n")}`;
  }

  if (text.includes("mini") || text.includes("final")) {
    const doneMini = miniTests.filter((x) => x.completed).length;
    const avgMini = roadmap.miniTestAverageScore == null ? "N/A" : Number(roadmap.miniTestAverageScore).toFixed(1);
    return [
      `Mini test đã xong: ${doneMini}/${miniTests.length}`,
      `Điểm trung bình mini test: ${avgMini}`,
      `Điểm final test: ${roadmap.finalTestScore == null ? "N/A" : roadmap.finalTestScore}`
    ].join("\n");
  }

  if (text.includes("goi y") || text.includes("plan") || text.includes("strategy")) {
    if (recommendations.length) {
      return `Gợi ý từ AI:\n${recommendations.map((x) => `- ${x}`).join("\n")}`;
    }
    if (steps.length) {
      const stepText = steps.slice(0, 4).map((s, i) => `${i + 1}. ${s.title || "Module"} (${s.duration || "N/A"})`);
      return `Kế hoạch học gợi ý:\n${stepText.join("\n")}`;
    }
    return "Hãy học theo thứ tự roadmap: Lessons -> Mini tests -> Final test.";
  }

  return "Bạn có thể hỏi: tóm tắt roadmap, bước tiếp theo, điểm yếu, mini/final test, hoặc gợi ý học tập.";
}

async function loadRoadmapModel(subjectId) {
  const roadmap = await api(`/api/subjects/${subjectId}/roadmap`, "GET", null, true);
  let aiRoadmap = null;
  if (roadmap && roadmap.subscribed && roadmap.placementReady) {
    aiRoadmap = await api(`/api/me/subjects/${subjectId}/ai-roadmap`, "GET", null, true);
  }
  return {
    roadmap,
    aiRoadmap,
    guide: parseGuide(aiRoadmap && aiRoadmap.roadmapGuideJson)
  };
}

function initHeaderForAuthenticatedUser() {
  const loginLink = document.getElementById("landing-login-link");
  const getStartedTop = document.getElementById("landing-get-started-top");
  const { user } = getAuth();
  if (!loginLink || !getStartedTop || !user) return;

  let profileMenu = document.getElementById("rt-profile-menu");
  if (!profileMenu) {
    profileMenu = document.createElement("div");
    profileMenu.id = "rt-profile-menu";
    profileMenu.style.position = "absolute";
    profileMenu.style.minWidth = "250px";
    profileMenu.style.background = "#fff";
    profileMenu.style.border = "1px solid #e2e8f0";
    profileMenu.style.borderRadius = "12px";
    profileMenu.style.boxShadow = "0 10px 30px rgba(2,6,23,0.12)";
    profileMenu.style.padding = "12px";
    profileMenu.style.zIndex = "2500";
    profileMenu.style.display = "none";
    profileMenu.innerHTML = `
      <div style="padding:6px 4px 10px 4px;border-bottom:1px solid #f1f5f9;">
        <div style="font-size:13px;font-weight:700;color:#0f172a;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${user.fullName || user.email || "User"}</div>
        <div style="font-size:11px;color:#64748b;">${user.email || ""}</div>
      </div>
      <div style="display:grid;gap:6px;padding-top:10px;">
        <button id="rt-profile-menu-profile" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Profile</button>
        <button id="rt-profile-menu-dashboard" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Dashboard</button>
        <button id="rt-profile-menu-logout" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;color:#b91c1c;background:#fef2f2;">Logout</button>
      </div>`;
    document.body.appendChild(profileMenu);

    profileMenu.querySelector("#rt-profile-menu-profile")?.addEventListener("click", () => {
      profileMenu.style.display = "none";
      nav("/profile", "profile.html");
    });
    profileMenu.querySelector("#rt-profile-menu-dashboard")?.addEventListener("click", () => {
      profileMenu.style.display = "none";
      if (currentRole() === "ADMIN") {
        nav("/admin-dashboard", "admin/adminDashboard.html");
        return;
      }
      nav("/dashboard", "dashboard.html");
    });
    profileMenu.querySelector("#rt-profile-menu-logout")?.addEventListener("click", () => {
      clearAuth();
      profileMenu.style.display = "none";
      nav("/landing", "landingPage.html");
    });
  }

  const base = String(user.fullName || user.email || "U").trim();
  loginLink.textContent = base ? base.charAt(0).toUpperCase() : "U";
  loginLink.className =
    "flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-700 text-sm font-bold ring-1 ring-slate-200 hover:bg-slate-200";
  loginLink.title = "Profile";
  loginLink.href = "#";
  loginLink.onclick = (e) => {
    e.preventDefault();
    const rect = loginLink.getBoundingClientRect();
    const menuWidth = profileMenu.offsetWidth || 250;
    const centerLeft = rect.left + window.scrollX + rect.width / 2 - menuWidth / 2;
    const minLeft = window.scrollX + 8;
    const maxLeft = window.scrollX + window.innerWidth - menuWidth - 8;
    profileMenu.style.top = `${rect.bottom + window.scrollY + 8}px`;
    profileMenu.style.left = `${Math.max(minLeft, Math.min(centerLeft, maxLeft))}px`;
    profileMenu.style.display = profileMenu.style.display === "block" ? "none" : "block";
  };

  getStartedTop.textContent = currentRole() === "ADMIN" ? "Admin Dashboard" : "Dashboard";
  getStartedTop.href = "#";
  getStartedTop.onclick = (e) => {
    e.preventDefault();
    if (currentRole() === "ADMIN") {
      nav("/admin-dashboard", "admin/adminDashboard.html");
      return;
    }
    nav("/dashboard", "dashboard.html");
  };

  document.addEventListener("click", (e) => {
    if (!profileMenu) return;
    if (e.target === loginLink || profileMenu.contains(e.target)) return;
    profileMenu.style.display = "none";
  });
  window.addEventListener("scroll", () => {
    if (profileMenu) profileMenu.style.display = "none";
  });
  window.addEventListener("resize", () => {
    if (profileMenu) profileMenu.style.display = "none";
  });
}

async function initRoadmapTutor() {
  const app = document.getElementById("rt-app");
  if (app) app.classList.add("hidden");

  const hasToken = !!localStorage.getItem("compassed_auth_token");
  if (!hasToken) {
    goAuthWithRedirect("/roadmap-tutor", "roadmapTutor.html");
    return;
  }

  if (!(await checkSession())) {
    goAuthWithRedirect("/roadmap-tutor", "roadmapTutor.html");
    return;
  }
  initHeaderForAuthenticatedUser();
  if (app) app.classList.remove("hidden");

  const title = document.getElementById("rt-title");
  const subjectSelect = document.getElementById("rt-subject-select");
  const form = document.getElementById("rt-chat-form");
  const input = document.getElementById("rt-chat-input");
  const placementBtn = document.getElementById("rt-go-placement");
  const dashboardBtn = document.getElementById("rt-go-dashboard");
  const quickButtons = Array.from(document.querySelectorAll(".rt-prompt"));
  const navLearning = document.getElementById("rt-nav-learning");
  const navLearningWrap = document.getElementById("rt-learning-dropdown");
  const navLearningMenu = document.getElementById("rt-nav-learning-menu");
  const navLearningPractice = document.getElementById("rt-nav-learning-practice");
  const navLearningRoadmap = document.getElementById("rt-nav-learning-roadmap");
  const navLearningAi = document.getElementById("rt-nav-learning-ai");
  let model = null;

  if (dashboardBtn) {
    dashboardBtn.addEventListener("click", () => nav("/roadmap-dashboard", "roadmapDashboard.html"));
  }
  if (navLearning && navLearningMenu) {
    navLearning.addEventListener("click", (e) => {
      e.preventDefault();
      navLearningMenu.classList.toggle("hidden");
    });
  }
  if (navLearningPractice) {
    navLearningPractice.addEventListener("click", () => {
      const subjectId = Number(subjectSelect && subjectSelect.value ? subjectSelect.value : getSubjectId());
      nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
    });
  }
  if (navLearningRoadmap) {
    navLearningRoadmap.addEventListener("click", () => {
      nav("/roadmap-tutor", "roadmapTutor.html");
    });
  }
  if (navLearningAi) {
    navLearningAi.addEventListener("click", () => {
      nav("/learning-ai", "learningAi.html");
    });
  }
  document.addEventListener("click", (e) => {
    if (!navLearningWrap || !navLearningMenu) return;
    if (navLearningWrap.contains(e.target)) return;
    navLearningMenu.classList.add("hidden");
  });

  const loadBySubject = async (subjectId) => {
    showLoading("Loading roadmap tutor context...");
    const chatList = document.getElementById("rt-chat-list");
    if (chatList) chatList.innerHTML = "";
    try {
      model = await loadRoadmapModel(subjectId);
      renderContext(model);
      const subjectName = model.roadmap?.subjectName || model.aiRoadmap?.subjectName || `Subject ${subjectId}`;
      if (title) title.textContent = `Roadmap Tutor - ${subjectName}`;
      if (placementBtn) placementBtn.classList.add("hidden");
      if (!model.roadmap?.subscribed) {
        appendMessage("assistant", `Môn ${subjectName} chưa đăng ký. Bạn có thể đăng ký ở trang Checkout để mở roadmap.`);
      } else if (!model.roadmap?.placementReady) {
        appendMessage("assistant", `Môn ${subjectName} chưa có placement result. Bạn cần làm placement test trước.`);
        if (placementBtn) {
          placementBtn.classList.remove("hidden");
          placementBtn.onclick = () => nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
        }
      } else {
        appendMessage(
          "assistant",
          [
            `Đã tải roadmap của bạn cho môn ${subjectName}.`,
            `Giai đoạn: ${model.roadmap?.phase || "N/A"} | Tiến độ: ${model.roadmap?.progressPercent || 0}%`,
            "Bạn có thể hỏi về bước tiếp theo, điểm yếu, mini/final test, kế hoạch học."
          ].join("\n")
        );
      }
    } catch (e) {
      const msg = String(e && e.message ? e.message : "");
      if (msg.toLowerCase().includes("placement")) {
        appendMessage("assistant", "Môn này chưa có placement result. Hãy làm placement test trước để AI phân tích roadmap.");
        if (placementBtn) {
          placementBtn.classList.remove("hidden");
          placementBtn.onclick = () => nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
        }
      } else {
        toast(`Không tải được Roadmap Tutor: ${msg}`, "error");
      }
    } finally {
      hideLoading();
    }
  };

  try {
    const [subData, allSubjects] = await Promise.all([
      api("/api/me/subscriptions", "GET", null, true),
      api("/api/subjects", "GET", null, false)
    ]);
    const active = Array.isArray(subData && subData.activeSubscriptions) ? subData.activeSubscriptions : [];
    const activeMap = new Map(active.map((x) => [Number(x.subjectId), true]));
    const subjects = Array.isArray(allSubjects) ? allSubjects : [];
    if (!subjects.length) {
      toast("Không tải được danh sách môn học.", "error");
      return;
    }
    if (subjectSelect) {
      subjectSelect.innerHTML = subjects
        .map((x) => {
          const sid = Number(x.id);
          const subscribedText = activeMap.has(sid) ? "" : " (Chưa đăng ký)";
          return `<option value="${sid}">${escapeHtml(x.name || `Môn ${sid}`)}${subscribedText}</option>`;
        })
        .join("");
      const defaultId = getSubjectId();
      const selected = subjects.some((x) => Number(x.id) === Number(defaultId))
        ? Number(defaultId)
        : Number(subjects[0].id);
      subjectSelect.value = String(selected);
      subjectSelect.addEventListener("change", () => loadBySubject(Number(subjectSelect.value)));
      await loadBySubject(selected);
    }
  } catch (e) {
    toast(`Không tải được danh sách môn học: ${e.message}`, "error");
    return;
  }

  if (form && input) {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      const text = String(input.value || "").trim();
      if (!text) return;
      appendMessage("user", text);
      input.value = "";
      appendMessage("assistant", roadmapReply(text, model || {}));
    });
  }

  quickButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const text = String(btn.getAttribute("data-prompt") || "").trim();
      if (!text) return;
      appendMessage("user", text);
      appendMessage("assistant", roadmapReply(text, model || {}));
    });
  });
}

export { initRoadmapTutor };
