import {
  KEYS,
  api,
  checkSession,
  clearAuth,
  currentRole,
  formatVnd,
  getAuth,
  getSubjectId,
  goAuthWithRedirect,
  hideLoading,
  nav,
  showLoading,
  toast
} from "./core.js";
import { openInlineProfilePanel } from "./inlineProfilePanel.js";

const CHECKOUT_PAYMENT_ID_KEY = "compassed_checkout_payment_id";
const CHECKOUT_SUBJECT_IDS_KEY = "compassed_checkout_subject_ids";
const CHECKOUT_OWNER_KEY = "compassed_checkout_owner";
const CHECKOUT_URL_KEY = "compassed_checkout_url";

function priceByCount(count, planMap) {
  if (planMap.has(count)) return planMap.get(count);
  if (count <= 0) return 0;
  if (count === 1) return 50000;
  if (count === 2) return 90000;
  return 130000;
}

async function initCheckout() {
  if (!(await checkSession())) {
    goAuthWithRedirect("/checkout", "checkout.html");
    return;
  }

  const selectedDefault = getSubjectId();
  const listEl = document.getElementById("checkout-subject-list");
  const totalEl = document.getElementById("checkout-total");
  const selectedCountEl = document.getElementById("checkout-selected-count");
  const selectedListEl = document.getElementById("checkout-selected-list");
  const discountEl = document.getElementById("checkout-discount");
  const tipEl = document.getElementById("checkout-tip");
  const payBtn = document.getElementById("checkout-pay");
  const payText = document.getElementById("checkout-pay-text");
  const qrImageEl = document.getElementById("checkout-qr-image");
  const qrPlaceholderEl = document.getElementById("checkout-qr-placeholder");
  const qrStatusEl = document.getElementById("checkout-qr-status");
  const bankNameEl = document.getElementById("checkout-bank-name");
  const accountNoEl = document.getElementById("checkout-account-no");
  const accountNameEl = document.getElementById("checkout-account-name");
  const transferContentEl = document.getElementById("checkout-transfer-content");
  const loginLink = document.getElementById("landing-login-link");
  const getStartedTop = document.getElementById("landing-get-started-top");
  const notifWrap = document.getElementById("landing-notif-wrap");
  if (!listEl || !totalEl || !payBtn) return;

  const state = {
    paymentId: null,
    checkoutUrl: null,
    selectedSubjectIds: [],
    pollTimer: null,
    unlocked: false
  };

  const userScope = (() => {
    const { user } = getAuth();
    if (user && user.id != null) return `id_${user.id}`;
    if (user && user.email) return `email_${String(user.email).toLowerCase()}`;
    return "anonymous";
  })();
  const ownerValue = userScope;
  const scopedPaymentKey = `${CHECKOUT_PAYMENT_ID_KEY}_${userScope}`;
  const scopedSubjectsKey = `${CHECKOUT_SUBJECT_IDS_KEY}_${userScope}`;
  const scopedOwnerKey = `${CHECKOUT_OWNER_KEY}_${userScope}`;
  const scopedCheckoutUrlKey = `${CHECKOUT_URL_KEY}_${userScope}`;

  const stopPolling = () => {
    if (state.pollTimer) {
      clearInterval(state.pollTimer);
      state.pollTimer = null;
    }
  };

  const setPayText = (text) => {
    if (payText) payText.textContent = text;
  };

  const setQrStatus = (text) => {
    if (qrStatusEl) qrStatusEl.textContent = text;
  };

  const isUnauthorizedError = (err) => {
    const msg = String((err && err.message) || "").toUpperCase();
    return msg.includes("NOT AUTHENTICATED")
      || msg.includes("UNAUTHORIZED")
      || msg.includes("AUTHENTICATION REQUIRED")
      || msg.includes("HTTP 401");
  };

  const clearPendingCheckoutState = () => {
    localStorage.removeItem(scopedPaymentKey);
    localStorage.removeItem(scopedSubjectsKey);
    localStorage.removeItem(scopedOwnerKey);
    localStorage.removeItem(scopedCheckoutUrlKey);
    // Cleanup old global keys from previous versions.
    localStorage.removeItem(CHECKOUT_PAYMENT_ID_KEY);
    localStorage.removeItem(CHECKOUT_SUBJECT_IDS_KEY);
    localStorage.removeItem(CHECKOUT_OWNER_KEY);
    localStorage.removeItem(CHECKOUT_URL_KEY);
  };

  const savePendingCheckoutState = (paymentId, subjectIds, checkoutUrl) => {
    if (paymentId) localStorage.setItem(scopedPaymentKey, String(paymentId));
    if (Array.isArray(subjectIds)) localStorage.setItem(scopedSubjectsKey, JSON.stringify(subjectIds));
    if (typeof checkoutUrl === "string" && checkoutUrl.trim()) {
      localStorage.setItem(scopedCheckoutUrlKey, checkoutUrl.trim());
    }
    localStorage.setItem(scopedOwnerKey, ownerValue);
  };

  const restorePendingCheckoutState = () => {
    const storedOwner = localStorage.getItem(scopedOwnerKey);
    if (storedOwner && storedOwner !== ownerValue) {
      return { paymentId: null, subjectIds: [], checkoutUrl: null };
    }
    const paymentId = Number(localStorage.getItem(scopedPaymentKey) || 0);
    const checkoutUrl = (localStorage.getItem(scopedCheckoutUrlKey) || "").trim();
    let subjectIds = [];
    try {
      subjectIds = JSON.parse(localStorage.getItem(scopedSubjectsKey) || "[]");
    } catch {
      subjectIds = [];
    }
    return {
      paymentId: paymentId || null,
      subjectIds: Array.isArray(subjectIds) ? subjectIds.map((x) => Number(x)).filter(Boolean) : [],
      checkoutUrl: checkoutUrl || null
    };
  };

  const resolvePayOsReturnReference = () => {
    const params = new URLSearchParams(window.location.search);
    const orderCode = (params.get("orderCode") || params.get("order_code") || "").trim();
    const status = (params.get("status") || "").trim().toUpperCase();
    const paymentLinkId = (params.get("id") || params.get("paymentLinkId") || "").trim();
    return { orderCode, status, paymentLinkId };
  };

  const renderQrInfo = (payload) => {
    if (qrImageEl) {
      const url = payload && payload.qrImageUrl;
      if (url) {
        qrImageEl.src = url;
        qrImageEl.classList.remove("hidden");
        if (qrPlaceholderEl) qrPlaceholderEl.classList.add("hidden");
      }
    }
    if (bankNameEl) bankNameEl.textContent = (payload && payload.bankName) || "-";
    if (accountNoEl) accountNoEl.textContent = (payload && payload.accountNo) || "-";
    if (accountNameEl) accountNameEl.textContent = (payload && payload.accountName) || "-";
    if (transferContentEl) transferContentEl.textContent = (payload && payload.transferContent) || "-";
  };

  const lockSelection = (boxes, lock) => {
    boxes.forEach((box) => {
      box.disabled = lock;
    });
  };

  const updateByStatus = async (statusPayload) => {
    const status = String((statusPayload && statusPayload.status) || "").toUpperCase();

    if (status === "SUCCESS" || status === "PAID") {
      setQrStatus("Payment confirmed");
      setPayText("Roadmap Unlocked");
      payBtn.disabled = true;
      stopPolling();
      clearPendingCheckoutState();

      if (!state.unlocked) {
        state.unlocked = true;
        const ids = Array.isArray(statusPayload.subjectIds) && statusPayload.subjectIds.length
          ? statusPayload.subjectIds.map((id) => Number(id)).filter(Boolean)
          : state.selectedSubjectIds;
        try {
          if (ids.length) {
            const sub = await api("/api/subscriptions/checkout", "POST", { subjectIds: ids }, true);
            localStorage.setItem(KEYS.subscription, JSON.stringify(sub));
          }
        } catch (_) {
          // Subscription may already be provisioned by payment backend logic.
        }
        toast("Thanh toan thanh cong. Dang chuyen toi Roadmap Dashboard...", "ok");
        setTimeout(() => nav("/roadmap-dashboard", "roadmapDashboard.html"), 900);
      }
      return;
    }

    if (status === "SUBMITTED") {
      setQrStatus("Processing payment...");
      setPayText("Waiting auto confirmation...");
      payBtn.disabled = true;
      return;
    }

    if (status === "FAILED" || status === "CANCELLED") {
      stopPolling();
      clearPendingCheckoutState();
      setQrStatus("Payment failed");
      setPayText("Start Payment");
      payBtn.disabled = false;
      state.paymentId = null;
      toast("Payment failed or cancelled. Please create a new checkout.", "warn");
      return;
    }

    if (state.paymentId) {
      setQrStatus("Waiting for payment...");
      setPayText("Waiting For Payment...");
      payBtn.disabled = true;
      return;
    }

    setQrStatus("Waiting for payment...");
    setPayText("Start Payment");
    payBtn.disabled = false;
  };

  const startPolling = (paymentId) => {
    if (!paymentId) return;
    stopPolling();
    let inFlight = false;

    const tick = async () => {
      if (inFlight) return;
      inFlight = true;
      try {
        const statusPayload = await api(`/api/payments/${paymentId}/status`, "GET", null, true);
        await updateByStatus(statusPayload);
      } catch (err) {
        const msg = String((err && err.message) || "");
        if (msg.includes("Payment not found") || isUnauthorizedError(err)) {
          stopPolling();
          clearPendingCheckoutState();
          state.paymentId = null;
          setQrStatus("Waiting for payment...");
          setPayText("Start Payment");
          payBtn.disabled = false;
          if (isUnauthorizedError(err)) {
            goAuthWithRedirect("/checkout", "checkout.html");
          }
          return;
        }
        // Keep polling on transient issues.
      } finally {
        inFlight = false;
      }
    };

    tick();
    state.pollTimer = setInterval(tick, 5000);
  };

  if (notifWrap) notifWrap.classList.add("hidden");
  if (loginLink) {
    const { user } = getAuth();
    const base = String((user && user.fullName) || (user && user.email) || "U").trim();
    loginLink.textContent = base ? base.charAt(0).toUpperCase() : "U";
    loginLink.className =
      "flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-700 text-sm font-bold ring-1 ring-slate-200 hover:bg-slate-200";
    loginLink.title = "Profile";
    let profileMenu = null;
    let popupOverlay = null;

    const closeProfileMenu = () => {
      if (profileMenu) profileMenu.style.display = "none";
    };

    const ensurePopupOverlay = () => {
      if (popupOverlay) return popupOverlay;
      popupOverlay = document.createElement("div");
      popupOverlay.style.position = "fixed";
      popupOverlay.style.inset = "0";
      popupOverlay.style.background = "rgba(15,23,42,0.4)";
      popupOverlay.style.display = "none";
      popupOverlay.style.alignItems = "center";
      popupOverlay.style.justifyContent = "center";
      popupOverlay.style.zIndex = "3000";
      popupOverlay.innerHTML = `
        <div id="checkout-popup-card" style="width:min(560px,92vw);background:#fff;border-radius:14px;border:1px solid #e2e8f0;box-shadow:0 18px 40px rgba(2,6,23,0.2);">
          <div id="checkout-popup-content"></div>
        </div>`;
      popupOverlay.addEventListener("click", (e) => {
        if (e.target === popupOverlay) popupOverlay.style.display = "none";
      });
      document.body.appendChild(popupOverlay);
      return popupOverlay;
    };

    const closePopup = () => {
      if (popupOverlay) popupOverlay.style.display = "none";
    };

    const openProfilePopup = async () => {
      closePopup();
      await openInlineProfilePanel();
    };

    const openSettingPopup = async () => {
      const overlay = ensurePopupOverlay();
      const content = overlay.querySelector("#checkout-popup-content");
      content.innerHTML = `<div style="padding:20px 18px;">Loading...</div>`;
      overlay.style.display = "flex";
      try {
        const profile = await api("/api/me/profile", "GET", null, true);
        content.innerHTML = `
          <div style="padding:18px 18px 12px 18px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;">
            <div style="font-size:18px;font-weight:800;color:#0f172a;">Setting</div>
            <button id="checkout-setting-popup-close" style="border:none;background:#f8fafc;border-radius:8px;padding:6px 10px;cursor:pointer;">X</button>
          </div>
          <div style="padding:16px 18px;display:grid;gap:12px;">
            <label style="display:flex;align-items:center;gap:8px;">
              <input id="checkout-setting-notify-email" type="checkbox" ${profile.notifyEmail ? "checked" : ""}/>
              <span style="font-size:13px;color:#334155;">Email notification</span>
            </label>
            <label style="display:flex;align-items:center;gap:8px;">
              <input id="checkout-setting-notify-inapp" type="checkbox" ${profile.notifyInApp ? "checked" : ""}/>
              <span style="font-size:13px;color:#334155;">In-app notification</span>
            </label>
            <button id="checkout-setting-popup-save" style="margin-top:6px;padding:10px 12px;border:none;border-radius:8px;background:#0d7ff2;color:#fff;font-weight:700;cursor:pointer;">Save Settings</button>
          </div>`;
        content.querySelector("#checkout-setting-popup-close").addEventListener("click", closePopup);
        content.querySelector("#checkout-setting-popup-save").addEventListener("click", async () => {
          try {
            const notifyEmail = !!content.querySelector("#checkout-setting-notify-email").checked;
            const notifyInApp = !!content.querySelector("#checkout-setting-notify-inapp").checked;
            await api("/api/me/profile", "PUT", { notifyEmail, notifyInApp }, true);
            toast("Settings updated");
            closePopup();
          } catch (e) {
            toast(`Save failed: ${e.message}`, "error");
          }
        });
      } catch (e) {
        content.innerHTML = `<div style="padding:20px 18px;color:#b91c1c;">Cannot load settings</div>`;
      }
    };

    const ensureProfileMenu = async () => {
      if (!profileMenu) {
        profileMenu = document.createElement("div");
        profileMenu.id = "checkout-profile-menu";
        profileMenu.style.position = "absolute";
        profileMenu.style.minWidth = "300px";
        profileMenu.style.background = "#fff";
        profileMenu.style.border = "1px solid #e2e8f0";
        profileMenu.style.borderRadius = "12px";
        profileMenu.style.boxShadow = "0 10px 30px rgba(2,6,23,0.12)";
        profileMenu.style.padding = "12px";
        profileMenu.style.zIndex = "2000";
        profileMenu.style.display = "none";
        profileMenu.innerHTML = `
          <div style="display:flex;align-items:center;gap:10px;padding:6px 4px 10px 4px;border-bottom:1px solid #f1f5f9;">
            <div style="width:34px;height:34px;border-radius:999px;background:#f1f5f9;display:flex;align-items:center;justify-content:center;font-weight:700;">${base ? base.charAt(0).toUpperCase() : "U"}</div>
            <div style="min-width:0;">
              <div style="font-size:13px;font-weight:700;color:#0f172a;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${(user && (user.fullName || user.email)) || "User"}</div>
              <div style="font-size:11px;color:#64748b;">${(user && user.email) || ""}</div>
            </div>
          </div>
          <div style="display:grid;gap:6px;padding-top:10px;">
            <button id="checkout-menu-profile" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Profile</button>
            <button id="checkout-menu-setting" style="text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;background:#f8fafc;">Setting</button>
            <div style="padding:8px 10px;border-radius:8px;border:1px dashed #cbd5e1;background:#f8fafc;">
              <div style="font-size:12px;font-weight:700;color:#0f172a;">Subscriptions</div>
              <div id="checkout-menu-subs-list" style="margin-top:6px;font-size:12px;color:#334155;">Loading...</div>
              <button id="checkout-menu-subscribe-more" style="margin-top:8px;padding:6px 8px;border-radius:6px;background:#0d7ff2;color:white;font-size:12px;font-weight:700;">Đăng ký thêm</button>
            </div>
            <button id="checkout-menu-logout" style="display:flex;align-items:center;gap:8px;text-align:left;padding:8px 10px;border-radius:8px;font-size:13px;color:#b91c1c;background:#fef2f2;">
              <span class="material-symbols-outlined" style="font-size:18px;">logout</span>
              <span>Logout</span>
            </button>
          </div>`;
        document.body.appendChild(profileMenu);

        const profileBtn = profileMenu.querySelector("#checkout-menu-profile");
        const settingBtn = profileMenu.querySelector("#checkout-menu-setting");
        const subscribeMoreBtn = profileMenu.querySelector("#checkout-menu-subscribe-more");
        const logoutBtn = profileMenu.querySelector("#checkout-menu-logout");
        const subsCard = profileMenu.querySelector("#checkout-menu-subs-list")?.parentElement;

        const applyHover = (el, baseColor, hoverColor) => {
          if (!el) return;
          el.style.transition = "background-color 140ms ease, color 140ms ease";
          el.addEventListener("mouseenter", () => {
            el.style.background = hoverColor;
          });
          el.addEventListener("mouseleave", () => {
            el.style.background = baseColor;
          });
        };
        applyHover(profileBtn, "#f8fafc", "#e2e8f0");
        applyHover(settingBtn, "#f8fafc", "#e2e8f0");
        applyHover(subscribeMoreBtn, "#0d7ff2", "#0b6bc9");
        applyHover(logoutBtn, "#fef2f2", "#fee2e2");
        applyHover(subsCard, "#f8fafc", "#eef2f7");

        profileMenu.querySelector("#checkout-menu-profile").addEventListener("click", async () => {
          closeProfileMenu();
          await openProfilePopup();
        });
        profileMenu.querySelector("#checkout-menu-setting").addEventListener("click", async () => {
          closeProfileMenu();
          await openSettingPopup();
        });
        profileMenu.querySelector("#checkout-menu-subscribe-more").addEventListener("click", () => {
          nav("/checkout", "checkout.html");
        });
        profileMenu.querySelector("#checkout-menu-logout").addEventListener("click", () => {
          clearAuth();
          closeProfileMenu();
          nav("/landing", "landingPage.html");
        });
      }
      try {
        const subData = await api("/api/me/subscriptions", "GET", null, true);
        const active = (subData && subData.activeSubscriptions) || [];
        const list = profileMenu.querySelector("#checkout-menu-subs-list");
        if (!active.length) {
          list.textContent = "No active subscription.";
        } else {
          list.innerHTML = active
            .map((s) => `- ${s.subjectName} (${s.level || "N/A"} | ${s.phase || "NOT_STARTED"})`)
            .join("<br/>");
        }
      } catch {
        const list = profileMenu.querySelector("#checkout-menu-subs-list");
        if (list) list.textContent = "Cannot load subscriptions.";
      }
    };

    const showProfileMenu = () => {
      if (!profileMenu) return;
      const rect = loginLink.getBoundingClientRect();
      const menuWidth = profileMenu.offsetWidth || 300;
      const centerLeft = rect.left + window.scrollX + rect.width / 2 - menuWidth / 2;
      const minLeft = window.scrollX + 8;
      const maxLeft = window.scrollX + window.innerWidth - menuWidth - 8;
      profileMenu.style.top = `${rect.bottom + window.scrollY + 8}px`;
      profileMenu.style.left = `${Math.max(minLeft, Math.min(centerLeft, maxLeft))}px`;
      profileMenu.style.display = "block";
    };

    loginLink.onclick = async (e) => {
      e.preventDefault();
      await ensureProfileMenu();
      if (profileMenu.style.display === "block") {
        closeProfileMenu();
        return;
      }
      showProfileMenu();
    };
    document.addEventListener("click", (e) => {
      if (!profileMenu) return;
      if (e.target === loginLink || profileMenu.contains(e.target)) return;
      closeProfileMenu();
    });
    window.addEventListener("scroll", closeProfileMenu);
    window.addEventListener("resize", closeProfileMenu);
  }

  if (getStartedTop) {
    getStartedTop.textContent = currentRole() === "ADMIN" ? "Admin Dashboard" : "Dashboard";
    getStartedTop.onclick = () => {
      if (currentRole() === "ADMIN") {
        nav("/admin-dashboard", "admin/adminDashboard.html");
        return;
      }
      nav("/dashboard", "dashboard.html");
    };
  }

  showLoading("Loading checkout...");
  try {
    const [subjects, plans, mySubs] = await Promise.all([
      api("/api/subjects", "GET", null, false),
      api("/api/pricing/plans", "GET", null, false),
      api("/api/me/subscriptions", "GET", null, true)
    ]);

    const subjectRows = Array.isArray(subjects) ? subjects : [];
    const planMap = new Map((plans || []).map((p) => [Number(p.subjectCount), Number(p.amountVnd)]));
    const single = priceByCount(1, planMap);
    const purchasedSubjectIds = new Set(
      ((mySubs && mySubs.activeSubscriptions) || [])
        .map((row) => Number(row.subjectId))
        .filter(Boolean)
    );

    listEl.innerHTML = "";
    subjectRows.forEach((subject) => {
      const purchased = purchasedSubjectIds.has(Number(subject.id));
      const row = document.createElement("label");
      row.className = `group relative flex flex-col gap-5 rounded-xl border bg-white p-6 transition-all shadow-sm ${purchased ? "border-emerald-200 bg-emerald-50/40" : "cursor-pointer border-slate-200 hover:border-primary/50"
        }`;
      row.innerHTML = `
        <div class="absolute top-4 right-4">
          ${purchased
          ? '<span class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2 py-1 text-xs font-bold text-emerald-700"><span class="material-symbols-outlined text-base">check_circle</span>Purchased</span>'
          : `<input class="checkout-subject size-5 rounded border-slate-300 text-primary focus:ring-primary" type="checkbox" value="${subject.id}" />`}
        </div>
        <div class="flex flex-col gap-2">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-primary">menu_book</span>
            <h3 class="text-xl font-bold">${subject.name}</h3>
          </div>
          <span class="inline-block w-fit px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider bg-slate-100 text-slate-500 rounded">${subject.code || "Subject"}</span>
          <div class="mt-4">
            <span class="text-3xl font-black text-slate-900">${formatVnd(single).replace(" VND", "")}</span>
            <span class="text-sm font-medium text-slate-500">VND</span>
          </div>
        </div>
        <ul class="space-y-3">
          <li class="flex items-center gap-2 text-sm text-slate-600"><span class="material-symbols-outlined text-green-500 text-sm">check_circle</span>Personalized roadmap</li>
          <li class="flex items-center gap-2 text-sm text-slate-600"><span class="material-symbols-outlined text-green-500 text-sm">check_circle</span>Progress tracking</li>
        </ul>`;
      listEl.appendChild(row);
    });

    const boxes = Array.from(document.querySelectorAll(".checkout-subject"));
    boxes.forEach((b) => {
      b.checked = Number(b.value) === selectedDefault;
      b.addEventListener("change", rerenderTotal);
    });

    function selectedIds() {
      return boxes.filter((b) => b.checked).map((b) => Number(b.value)).filter(Boolean);
    }

    function rerenderTotal() {
      const ids = selectedIds();
      const total = priceByCount(ids.length, planMap);
      totalEl.textContent = formatVnd(total);
      if (selectedCountEl) selectedCountEl.textContent = `${ids.length}/${subjectRows.length}`;

      if (selectedListEl) {
        const picked = subjectRows.filter((s) => ids.includes(Number(s.id)));
        selectedListEl.innerHTML = !picked.length
          ? '<div class="text-slate-400">No subject selected</div>'
          : picked
            .map((s) => `<div class="flex justify-between items-center"><span class="text-slate-500">${s.name}</span><span class="text-slate-700">${formatVnd(single)}</span></div>`)
            .join("");
      }

      const original = ids.length * single;
      const discount = Math.max(0, original - total);
      if (discountEl) discountEl.textContent = formatVnd(discount);

      if (tipEl) {
        if (ids.length >= 3) tipEl.textContent = "Best value bundle applied.";
        else if (ids.length === 2) tipEl.textContent = "Great! You are saving with 2-subject bundle.";
        else tipEl.textContent = "Tip: Buy 2 subjects to save more.";
      }
    }

    rerenderTotal();
    setPayText("Start Payment");
    setQrStatus("Waiting for payment...");

    // If user is redirected back from PayOS, verify immediately by orderCode.
    const payOsReturn = resolvePayOsReturnReference();
    if (payOsReturn.orderCode) {
      try {
        setQrStatus("Verifying payment...");
        const byRef = await api(`/api/payments/by-reference/${encodeURIComponent(payOsReturn.orderCode)}/status`, "GET", null, true);
        if (byRef && byRef.paymentId) {
          state.paymentId = Number(byRef.paymentId);
          state.selectedSubjectIds = Array.isArray(byRef.subjectIds)
            ? byRef.subjectIds.map((id) => Number(id)).filter(Boolean)
            : [];
          savePendingCheckoutState(state.paymentId, state.selectedSubjectIds, state.checkoutUrl);
        }
        await updateByStatus(byRef);
        if (!state.unlocked && state.paymentId) {
          lockSelection(boxes, true);
          payBtn.disabled = true;
          startPolling(state.paymentId);
        }
      } catch {
        // Fallback to restore/latest-active flow below.
      }
    }

    const restored = restorePendingCheckoutState();
    if (!state.unlocked && restored.paymentId && !state.paymentId) {
      state.paymentId = restored.paymentId;
      state.selectedSubjectIds = restored.subjectIds;
      state.checkoutUrl = restored.checkoutUrl;
      lockSelection(boxes, true);
      setPayText("Waiting For Payment...");
      setQrStatus("Checking payment status...");
      payBtn.disabled = true;
      startPolling(state.paymentId);
    }

    // Fallback: no local paymentId (new tab/device), ask backend for latest active one.
    if (!state.unlocked && !state.paymentId) {
      try {
        const latest = await api("/api/payments/latest-active/status", "GET", null, true);
        const latestId = Number((latest && latest.paymentId) || 0);
        if (latestId) {
          state.paymentId = latestId;
          state.selectedSubjectIds = Array.isArray(latest.subjectIds)
            ? latest.subjectIds.map((id) => Number(id)).filter(Boolean)
            : [];
          savePendingCheckoutState(state.paymentId, state.selectedSubjectIds, state.checkoutUrl);
          await updateByStatus(latest);
          if (!state.unlocked) {
            lockSelection(boxes, true);
            setPayText("Waiting For Payment...");
            setQrStatus("Checking payment status...");
            payBtn.disabled = true;
            startPolling(state.paymentId);
          }
        }
      } catch (_) {
        // no active payment
      }
    }

    payBtn.addEventListener("click", async () => {
      const ids = selectedIds();
      if (!ids.length) {
        toast("Select at least 1 subject", "warn");
        return;
      }

      showLoading("Processing checkout...");
      payBtn.disabled = true;
      setPayText("Processing...");

      try {
        if (!state.paymentId) {
          const created = await api("/api/payments/checkout-qr", "POST", { subjectIds: ids }, true);
          state.paymentId = Number(created.paymentId);
          state.checkoutUrl = typeof created.checkoutUrl === "string" ? created.checkoutUrl.trim() : null;
          state.selectedSubjectIds = ids;
          savePendingCheckoutState(state.paymentId, state.selectedSubjectIds, state.checkoutUrl);
          renderQrInfo(created);
          lockSelection(boxes, true);
          setQrStatus("Transfer pending");
          setPayText("Waiting For Payment...");
          payBtn.disabled = true;
          startPolling(state.paymentId);
          toast("QR created. He thong se tu dong xac nhan giao dich.", "ok");
          return;
        }

        payBtn.disabled = true;
        setPayText("Waiting For Payment...");
      } catch (err) {
        toast(`Checkout failed: ${err.message}`, "error");
        payBtn.disabled = false;
        setPayText("Start Payment");
      } finally {
        hideLoading();
      }
    });
  } catch (err) {
    toast(`Checkout load failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  window.addEventListener("beforeunload", stopPolling);
}

export { initCheckout };


