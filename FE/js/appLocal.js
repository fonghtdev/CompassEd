(function () {
  const API_BASE = (window.APP_CONFIG && window.APP_CONFIG.API_BASE) || "http://localhost:8080";
  const GOOGLE_CLIENT_ID = (window.APP_CONFIG && window.APP_CONFIG.GOOGLE_CLIENT_ID) || "";
  const KEYS = {
    token: "compassed_auth_token",
    user: "compassed_auth_user",
    subjectId: "compassed_subject_id",
    attemptId: "compassed_attempt_id",
    paper: "compassed_paper_json",
    answers: "compassed_answers_json",
    result: "compassed_result_json",
    subscription: "compassed_subscription_json"
  };

  function pageName() {
    const p = window.location.pathname.toLowerCase();
    if (p.endsWith("/landing") || p.endsWith("/")) return "landing";
    if (p.includes("landingpage.html")) return "landing";
    if (p.endsWith("/auth") || p.includes("auth.html")) return "auth";
    if (p.endsWith("/placement-test") || p.includes("placementtest.html")) return "placement";
    if (p.endsWith("/placement-result") || p.includes("placementtestresult.html")) return "result";
    if (p.endsWith("/learning-roadmap") || p.includes("coursesdetail.html")) return "roadmap";
    if (p.endsWith("/history") || p.includes("history.html")) return "history";
    return "";
  }

  function nav(routePath, fileName) {
    if (window.location.protocol === "file:") {
      window.location.href = fileName;
      return;
    }
    window.location.href = routePath;
  }

  function toast(message, kind) {
    let wrap = document.getElementById("app-toast-wrap");
    if (!wrap) {
      wrap = document.createElement("div");
      wrap.id = "app-toast-wrap";
      wrap.style.position = "fixed";
      wrap.style.top = "16px";
      wrap.style.right = "16px";
      wrap.style.zIndex = "9999";
      wrap.style.display = "grid";
      wrap.style.gap = "8px";
      document.body.appendChild(wrap);
    }
    const item = document.createElement("div");
    item.textContent = message;
    item.style.padding = "10px 14px";
    item.style.borderRadius = "10px";
    item.style.color = "white";
    item.style.fontSize = "14px";
    item.style.fontWeight = "600";
    item.style.background = kind === "error" ? "#dc2626" : kind === "warn" ? "#d97706" : "#2563eb";
    wrap.appendChild(item);
    setTimeout(() => item.remove(), 3000);
  }

  function showLoading(text) {
    let el = document.getElementById("app-loading");
    if (!el) {
      el = document.createElement("div");
      el.id = "app-loading";
      el.style.position = "fixed";
      el.style.inset = "0";
      el.style.background = "rgba(15,23,42,0.35)";
      el.style.zIndex = "9998";
      el.style.display = "flex";
      el.style.alignItems = "center";
      el.style.justifyContent = "center";
      el.innerHTML = '<div style="background:white;padding:12px 16px;border-radius:10px;font-weight:700;color:#0f172a">Loading...</div>';
      document.body.appendChild(el);
    }
    el.querySelector("div").textContent = text || "Loading...";
    el.style.display = "flex";
  }

  function hideLoading() {
    const el = document.getElementById("app-loading");
    if (el) el.style.display = "none";
  }

  function getAuth() {
    const token = localStorage.getItem(KEYS.token);
    const user = JSON.parse(localStorage.getItem(KEYS.user) || "null");
    return { token, user };
  }

  function saveAuth(resp) {
    localStorage.setItem(KEYS.token, resp.token);
    localStorage.setItem(KEYS.user, JSON.stringify(resp.user));
  }

  function clearAuth() {
    localStorage.removeItem(KEYS.token);
    localStorage.removeItem(KEYS.user);
  }

  async function api(path, method, body, needAuth) {
    const { token, user } = getAuth();
    const headers = { "Content-Type": "application/json" };
    if (needAuth) {
      if (!token || !user) throw new Error("Not authenticated");
      headers["Authorization"] = `Bearer ${token}`;
      headers["X-USER-ID"] = String(user.id);
    }
    let res;
    try {
      res = await fetch(`${API_BASE}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
      });
    } catch (e) {
      throw new Error("Khong ket noi duoc backend. Hay chay BE tai http://localhost:8080");
    }
    if (!res.ok) {
      let msg = `HTTP ${res.status}`;
      try {
        msg = await res.text();
      } catch (e) {}
      throw new Error(msg);
    }
    return res.json();
  }

  async function checkSession() {
    const { token } = getAuth();
    if (!token) return false;
    try {
      const me = await api("/api/auth/me", "GET", null, true);
      localStorage.setItem(KEYS.user, JSON.stringify(me));
      return true;
    } catch (e) {
      clearAuth();
      return false;
    }
  }

  function getSubjectId() {
    const q = new URLSearchParams(window.location.search);
    const fromUrl = Number(q.get("subjectId"));
    if (fromUrl) {
      localStorage.setItem(KEYS.subjectId, String(fromUrl));
      return fromUrl;
    }
    return Number(localStorage.getItem(KEYS.subjectId) || 1);
  }

  function goAuthWithRedirect(routeAfter, fileAfter) {
    localStorage.setItem("compassed_after_auth_route", routeAfter);
    localStorage.setItem("compassed_after_auth_file", fileAfter);
    nav("/auth", "auth.html");
  }

  function redirectAfterAuthDefault() {
    const route = localStorage.getItem("compassed_after_auth_route") || "/landing";
    const file = localStorage.getItem("compassed_after_auth_file") || "landingPage.html";
    localStorage.removeItem("compassed_after_auth_route");
    localStorage.removeItem("compassed_after_auth_file");
    nav(route, file);
  }

  async function loadSubjects() {
    const container = document.querySelector("#master-core-subjects .grid");
    if (!container) return;

    try {
      const subjects = await api("/api/subjects", "GET", null, false);
      
      // Clear existing cards
      container.innerHTML = "";
      
      // Subject icons mapping
      const iconMap = {
        "MATH": { icon: "calculate", color: "orange" },
        "LITERATURE": { icon: "auto_stories", color: "purple" },
        "ENGLISH": { icon: "edit_note", color: "teal" }
      };
      
      subjects.forEach((subject, idx) => {
        const config = iconMap[subject.code] || { icon: "book", color: "blue" };
        const isPopular = idx === 0;
        
        const card = document.createElement("div");
        card.className = "group flex flex-col rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition-all hover:border-primary/50 hover:shadow-md";
        card.innerHTML = `
          <div class="mb-6 flex items-center justify-between">
            <div class="flex h-12 w-12 items-center justify-center rounded-full bg-${config.color}-100 text-${config.color}-600">
              <span class="material-symbols-outlined">${config.icon}</span>
            </div>
            ${isPopular ? '<div class="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold uppercase tracking-wide text-slate-600">Popular</div>' : ''}
          </div>
          <h3 class="mb-2 text-xl font-bold text-slate-900">${subject.name}</h3>
          <div class="mb-6 flex items-baseline gap-1">
            <span class="text-3xl font-black text-slate-900">$29</span>
            <span class="text-sm font-medium text-slate-500">/month</span>
          </div>
          <ul class="mb-8 flex flex-col gap-3">
            <li class="flex items-center gap-3 text-sm text-slate-600">
              <span class="material-symbols-outlined text-primary" style="font-size: 20px;">check_circle</span>
              Adaptive Learning Path
            </li>
            <li class="flex items-center gap-3 text-sm text-slate-600">
              <span class="material-symbols-outlined text-primary" style="font-size: 20px;">check_circle</span>
              AI-Powered Explanations
            </li>
            <li class="flex items-center gap-3 text-sm text-slate-600">
              <span class="material-symbols-outlined text-primary" style="font-size: 20px;">check_circle</span>
              Practice Exercises
            </li>
            <li class="flex items-center gap-3 text-sm text-slate-600">
              <span class="material-symbols-outlined text-primary" style="font-size: 20px;">check_circle</span>
              Progress Tracking
            </li>
          </ul>
          <button class="js-join-program mt-auto w-full rounded-lg bg-slate-100 py-3 text-sm font-bold text-slate-900 transition-colors hover:bg-primary hover:text-white group-hover:bg-primary group-hover:text-white" data-subject-id="${subject.id}">
            Join Program
          </button>
        `;
        container.appendChild(card);
      });
      
      // Re-attach event listeners after rendering
      attachJoinProgramListeners();
      
    } catch (err) {
      console.error("Failed to load subjects:", err);
      toast("Khong tai duoc danh sach mon hoc", "error");
    }
  }

  function attachJoinProgramListeners() {
    document.querySelectorAll(".js-join-program").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const subjectId = Number(btn.getAttribute("data-subject-id")) || 1;
        localStorage.setItem(KEYS.subjectId, String(subjectId));
        const ok = await checkSession();
        if (!ok) {
          toast("Ban can dang nhap hoac dang ky truoc khi bat dau", "warn");
          goAuthWithRedirect(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
          return;
        }
        nav(`/placement-test?subjectId=${subjectId}`, `placementTest.html?subjectId=${subjectId}`);
      });
    });
  }

  function initLanding() {
    // Load subjects from API
    loadSubjects();
    
    // Check if user is logged in and update UI
    const auth = getAuth();
    const loginLink = document.getElementById("landing-login-link");
    const userMenu = document.getElementById("landing-user-menu");
    const userName = document.getElementById("landing-user-name");
    const userAvatar = document.getElementById("landing-user-avatar");
    const userDropdown = document.getElementById("landing-user-dropdown");
    const logoutBtn = document.getElementById("landing-logout-btn");

    if (auth && auth.user) {
      // User is logged in - show user menu
      if (loginLink) loginLink.classList.add("hidden");
      if (userMenu) {
        userMenu.classList.remove("hidden");
        userMenu.classList.add("flex");
      }
      if (userName) userName.textContent = auth.user.fullName || auth.user.email;
      if (userAvatar) {
        const initials = (auth.user.fullName || auth.user.email || "U").charAt(0).toUpperCase();
        userAvatar.textContent = initials;
        
        // Toggle dropdown
        userAvatar.addEventListener("click", (e) => {
          e.stopPropagation();
          userDropdown && userDropdown.classList.toggle("hidden");
        });
      }
      
      // Close dropdown when clicking outside
      document.addEventListener("click", () => {
        userDropdown && userDropdown.classList.add("hidden");
      });
      
      // Logout handler
      if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
          clearAuth();
          toast("Da dang xuat thanh cong");
          location.reload();
        });
      }
    } else {
      // User not logged in - show login link
      if (loginLink) loginLink.classList.remove("hidden");
      if (userMenu) userMenu.classList.add("hidden");
    }

    const scrollToSubjects = () => {
      const section = document.getElementById("master-core-subjects");
      if (section) {
        section.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    };

    ["landing-get-started-top", "landing-start-learning", "landing-get-started-bottom"].forEach((id) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.addEventListener("click", scrollToSubjects);
    });

    const login = document.getElementById("landing-login-link");
    if (login) {
      login.addEventListener("click", (e) => {
        e.preventDefault();
        nav("/auth", "auth.html");
      });
    }
  }

  function setupAuthTabs() {
    const tabLogin = document.getElementById("tab-login");
    const tabRegister = document.getElementById("tab-register");
    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");
    if (!tabLogin || !tabRegister || !loginForm || !registerForm) return;

    const showLogin = () => {
      tabLogin.className = "px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold";
      tabRegister.className = "px-4 py-2 rounded-lg bg-slate-100 text-slate-700 font-semibold";
      loginForm.classList.remove("hidden");
      registerForm.classList.add("hidden");
    };
    const showRegister = () => {
      tabRegister.className = "px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold";
      tabLogin.className = "px-4 py-2 rounded-lg bg-slate-100 text-slate-700 font-semibold";
      registerForm.classList.remove("hidden");
      loginForm.classList.add("hidden");
    };
    tabLogin.addEventListener("click", showLogin);
    tabRegister.addEventListener("click", showRegister);
  }

  function initAuth() {
    setupAuthTabs();

    const loginForm = document.getElementById("login-form");
    loginForm && loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      showLoading("Dang dang nhap...");
      try {
        const email = document.getElementById("login-email").value.trim();
        const password = document.getElementById("login-password").value;
        const resp = await api("/api/auth/login", "POST", { email, password }, false);
        saveAuth(resp);
        toast("Dang nhap thanh cong");
        redirectAfterAuthDefault();
      } catch (err) {
        toast(`Dang nhap that bai: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });

    const registerForm = document.getElementById("register-form");
    registerForm && registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      showLoading("Dang tao tai khoan...");
      try {
        const fullName = document.getElementById("register-name").value.trim();
        const email = document.getElementById("register-email").value.trim();
        const password = document.getElementById("register-password").value;
        const resp = await api("/api/auth/register", "POST", { fullName, email, password }, false);
        saveAuth(resp);
        toast("Dang ky thanh cong");
        redirectAfterAuthDefault();
      } catch (err) {
        toast(`Dang ky that bai: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });

    const githubBtn = document.getElementById("github-mock-btn");
    githubBtn && githubBtn.addEventListener("click", async () => {
      const email = prompt("Nhap email GitHub (dev mode):");
      if (!email) return;
      showLoading("Dang dang nhap GitHub (dev)...");
      try {
        const fullName = email.split("@")[0];
        const resp = await api("/api/auth/oauth/mock", "POST", { provider: "github", email, fullName }, false);
        saveAuth(resp);
        toast("Dang nhap GitHub (dev) thanh cong");
        redirectAfterAuthDefault();
      } catch (err) {
        toast(`GitHub login that bai: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });

    if (window.google && GOOGLE_CLIENT_ID) {
      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: async (response) => {
          showLoading("Dang xac thuc Google...");
          try {
            const resp = await api("/api/auth/oauth/google", "POST", { idToken: response.credential }, false);
            saveAuth(resp);
            toast("Dang nhap Google thanh cong");
            redirectAfterAuthDefault();
          } catch (err) {
            toast(`Google login that bai: ${err.message}`, "error");
          } finally {
            hideLoading();
          }
        }
      });
      const container = document.getElementById("google-signin-container");
      if (container) {
        window.google.accounts.id.renderButton(container, { theme: "outline", size: "large", width: 320 });
      }
    } else {
      toast("Google login chua duoc cau hinh (GOOGLE_CLIENT_ID)", "warn");
    }
  }

  function renderPlacementOptions(options, selected, onSelect) {
    const wrap = document.getElementById("placement-options");
    if (!wrap) return;
    wrap.innerHTML = "";
    options.forEach((text, idx) => {
      const letter = String.fromCharCode(65 + idx);
      const label = document.createElement("label");
      label.className = "relative group cursor-pointer";
      
      // Create input element
      const input = document.createElement("input");
      input.className = "peer sr-only";
      input.type = "radio";
      input.name = "placement-answer";
      input.value = letter;
      if (selected === letter) input.checked = true;
      
      // Create div wrapper
      const divWrapper = document.createElement("div");
      divWrapper.className = "p-5 rounded-xl border-2 border-border-light bg-surface-light hover:border-primary/50 peer-checked:border-primary peer-checked:bg-primary/5 transition-all duration-200 flex items-center gap-4";
      
      // Create letter circle
      const letterCircle = document.createElement("div");
      letterCircle.className = "size-8 rounded-full border-2 border-border-light flex items-center justify-center text-sm font-bold text-text-secondary-light peer-checked:bg-primary peer-checked:border-primary peer-checked:text-white";
      letterCircle.textContent = letter;
      
      // Create text span
      const textSpan = document.createElement("span");
      textSpan.className = "text-lg font-medium";
      textSpan.textContent = text;
      
      // Assemble elements
      divWrapper.appendChild(letterCircle);
      divWrapper.appendChild(textSpan);
      label.appendChild(input);
      label.appendChild(divWrapper);
      
      // Add event listener AFTER creating all elements
      input.addEventListener("change", (e) => {
        console.log("Answer selected:", e.target.value); // Debug log
        onSelect(e.target.value);
      });
      
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

    showLoading("Dang kiem tra quyen truy cap...");
    try {
      // Check free attempts first
      const { user } = getAuth();
      const checkRes = await api(`/api/placement-tests/check-free-attempts?userId=${user.id}&subjectId=${subjectId}`, "GET", null, true);
      
      if (!checkRes.hasAccess) {
        hideLoading();
        if (confirm("Ban khong con luot placement mien phi. Ban can mua goi de tiep tuc. Chuyen den trang thanh toan?")) {
          nav(`/payment?subjectId=${subjectId}`, `payment.html?subjectId=${subjectId}`);
        } else {
          nav("/landing", "landingPage.html");
        }
        return;
      }
    } catch (err) {
      console.error("Check free attempts failed:", err);
      // Continue anyway if check fails
    }

    showLoading("Dang tai de placement...");
    try {
      // FORCE RELOAD: Luôn gọi API mới để lấy câu hỏi mới nhất từ database
      // Xóa cache cũ nếu có
      const oldAttemptId = Number(localStorage.getItem(attemptKey) || 0);
      const hasOldCache = oldAttemptId > 0 && localStorage.getItem(paperKey);
      
      if (hasOldCache) {
        // Clear old cache to force new questions
        localStorage.removeItem(attemptKey);
        localStorage.removeItem(paperKey);
        localStorage.removeItem(answersKey);
      }
      
      // Always call API to get fresh questions from database
      const started = await api(`/api/subjects/${subjectId}/placement-tests`, "POST", null, true);
      const attemptId = started.attemptId;
      const paper = JSON.parse(started.paperJson || "[]");
      localStorage.setItem(attemptKey, String(attemptId));
      localStorage.setItem(paperKey, JSON.stringify(paper));
      const isNew = true;
      let index = 0;
      const answers = isNew ? {} : JSON.parse(localStorage.getItem(answersKey) || "{}");
      localStorage.setItem(answersKey, JSON.stringify(answers));

      const questionEl = document.getElementById("placement-question-text");
      const descEl = document.getElementById("placement-question-desc");
      const counterEl = document.getElementById("placement-counter-mobile");
      const progressText = document.getElementById("placement-progress-text");
      const progressBar = document.getElementById("placement-progress-bar");
      const prevBtn = document.getElementById("placement-prev-btn");
      const nextBtn = document.getElementById("placement-next-btn");

      function rerender() {
        const q = paper[index];
        if (!q) return;
        console.log(`Rendering question ${index + 1}:`, q.id, q.q); // Debug log
        questionEl.textContent = q.q || `Question ${index + 1}`;
        if (descEl) descEl.textContent = "Chon dap an phu hop nhat.";
        const options = (q.options || []).map((x) => String(x).replace(/^[A-D]\.\s*/, ""));
        renderPlacementOptions(options, answers[String(q.id)], (value) => {
          console.log(`Answer saved for question ${q.id}:`, value); // Debug log
          answers[String(q.id)] = value;
          localStorage.setItem(answersKey, JSON.stringify(answers));
          console.log('Current answers:', answers); // Debug log
        });
        const answeredCount = Object.keys(answers).length;
        console.log(`Answered: ${answeredCount} / ${paper.length}`); // Debug log
        progressText.textContent = `${answeredCount} of ${paper.length} answered`;
        progressBar.style.width = `${Math.round((answeredCount / Math.max(paper.length, 1)) * 100)}%`;
        counterEl.textContent = `Question ${index + 1} of ${paper.length}`;
        prevBtn.disabled = index === 0;
        nextBtn.textContent = index === paper.length - 1 ? "Submit Test" : "Next Question";
      }

      prevBtn.addEventListener("click", () => {
        if (index > 0) {
          index -= 1;
          rerender();
        }
      });

      nextBtn.addEventListener("click", async () => {
        const q = paper[index];
        console.log(`Next button clicked. Current question:`, q.id, `Answer:`, answers[String(q.id)]); // Debug log
        if (!answers[String(q.id)]) {
          toast("Vui long chon dap an truoc khi tiep tuc", "warn");
          return;
        }
        if (index < paper.length - 1) {
          index += 1;
          console.log(`Moving to question ${index + 1}`); // Debug log
          rerender();
          return;
        }
        // Last question - submit
        console.log('Submitting test with answers:', answers); // Debug log
        showLoading("Dang nop bai...");
        try {
          const result = await api(`/api/placement-attempts/${attemptId}/submit`, "POST", { answersJson: JSON.stringify(answers) }, true);
          console.log('Submit result:', result); // Debug log
          localStorage.setItem(KEYS.result, JSON.stringify(result));
          localStorage.removeItem(attemptKey);
          localStorage.removeItem(paperKey);
          localStorage.removeItem(answersKey);
          toast("Nop bai thanh cong");
          nav("/placement-result", "placementTestResult.html");
        } catch (e) {
          console.error('Submit error:', e); // Debug log
          toast(`Nop bai that bai: ${e.message}`, "error");
        } finally {
          hideLoading();
        }
      });

      rerender();
    } catch (err) {
      toast(`Khong tai duoc de placement: ${err.message}`, "error");
    } finally {
      hideLoading();
    }
  }

  function initResult() {
    const data = JSON.parse(localStorage.getItem(KEYS.result) || "{}");
    const score = Math.round(Number(data.scorePercent || 0));
    const level = data.level || "L1";
    const scoreEl = document.getElementById("result-score");
    const levelEl = document.getElementById("result-level");
    const focusEl = document.getElementById("result-focus");
    const progressEl = document.getElementById("result-level-progress");
    if (scoreEl) scoreEl.textContent = String(score);
    if (levelEl) levelEl.textContent = level;
    if (focusEl) focusEl.textContent = level === "L3" ? "Advanced Mixed Skills" : level === "L2" ? "Algebra" : "Core Foundations";
    if (progressEl) progressEl.style.width = `${level === "L3" ? 100 : level === "L2" ? 65 : 35}%`;

    const backBtn = document.getElementById("result-back-btn");
    backBtn && backBtn.addEventListener("click", () => nav("/landing", "landingPage.html"));

    const unlockBtn = document.getElementById("result-unlock-btn");
    unlockBtn && unlockBtn.addEventListener("click", async () => {
      if (!(await checkSession())) {
        goAuthWithRedirect("/placement-result", "placementTestResult.html");
        return;
      }
      // Go directly to roadmap page
      const subjectId = getSubjectId();
      const level = data.level || "L1";
      nav(`/roadmap?subjectId=${subjectId}&level=${level}`, `roadmap.html?subjectId=${subjectId}&level=${level}`);
    });
  }

  function initRoadmap() {
    const status = document.getElementById("roadmap-api-status");
    const sub = JSON.parse(localStorage.getItem(KEYS.subscription) || "{}");
    const item = Array.isArray(sub.items) ? sub.items[0] : null;
    if (status) {
      if (item && item.status === "ROADMAP_UNLOCKED") {
        status.textContent = `Roadmap unlocked: ${item.roadmapTitle || "Ready"}`;
      } else if (item && item.status === "NEED_PLACEMENT") {
        status.textContent = "Can placement de mo roadmap.";
      } else {
        status.textContent = "Dang o local mode. Hoan thanh placement va unlock de dong bo.";
      }
    }

    const resume = document.getElementById("roadmap-resume-btn");
    resume && resume.addEventListener("click", () => {
      const sid = getSubjectId();
      nav(`/placement-test?subjectId=${sid}`, `placementTest.html?subjectId=${sid}`);
    });

    const historyBtn = document.getElementById("roadmap-history-btn");
    historyBtn && historyBtn.addEventListener("click", () => nav("/history", "history.html"));

    const start = document.getElementById("roadmap-start-lesson-btn");
    start && start.addEventListener("click", () => toast("Lesson demo mode"));

    const review = document.getElementById("roadmap-review-btn");
    review && review.addEventListener("click", () => toast("Review demo mode"));
  }

  async function initHistory() {
    if (!(await checkSession())) {
      goAuthWithRedirect("/history", "history.html");
      return;
    }
    const body = document.getElementById("history-table-body");
    if (!body) return;
    showLoading("Dang tai lich su...");
    try {
      const rows = await api("/api/history/placements", "GET", null, true);
      body.innerHTML = "";
      if (!rows.length) {
        body.innerHTML = '<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">Chua co lich su placement</td></tr>';
      } else {
        rows.forEach((r) => {
          const tr = document.createElement("tr");
          tr.innerHTML = `
            <td class="px-4 py-3">${new Date(r.submittedAt).toLocaleString()}</td>
            <td class="px-4 py-3">${r.subjectCode} - ${r.subjectName}</td>
            <td class="px-4 py-3">${r.level}</td>
            <td class="px-4 py-3">${Math.round(Number(r.scorePercent || 0))}%</td>
            <td class="px-4 py-3">#${r.attemptId}</td>`;
          body.appendChild(tr);
        });
      }
    } catch (err) {
      toast(`Tai lich su that bai: ${err.message}`, "error");
    } finally {
      hideLoading();
    }

    const backLanding = document.getElementById("history-back-landing");
    backLanding && backLanding.addEventListener("click", () => nav("/landing", "landingPage.html"));
    const goRoadmap = document.getElementById("history-go-roadmap");
    goRoadmap && goRoadmap.addEventListener("click", () => nav("/learning-roadmap", "coursesDetail.html"));
  }

  document.addEventListener("DOMContentLoaded", () => {
    const p = pageName();
    if (p === "landing") initLanding();
    if (p === "auth") initAuth();
    if (p === "placement") initPlacement();
    if (p === "result") initResult();
    if (p === "roadmap") initRoadmap();
    if (p === "history") initHistory();
  });
})();
