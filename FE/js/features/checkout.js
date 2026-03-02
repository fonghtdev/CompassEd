import {
  KEYS,
  api,
  checkSession,
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

const CHECKOUT_PAYMENT_ID_KEY = "compassed_checkout_payment_id";
const CHECKOUT_SUBJECT_IDS_KEY = "compassed_checkout_subject_ids";

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
    selectedSubjectIds: [],
    pollTimer: null,
    unlocked: false
  };

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

  const clearPendingCheckoutState = () => {
    localStorage.removeItem(CHECKOUT_PAYMENT_ID_KEY);
    localStorage.removeItem(CHECKOUT_SUBJECT_IDS_KEY);
  };

  const savePendingCheckoutState = (paymentId, subjectIds) => {
    if (paymentId) localStorage.setItem(CHECKOUT_PAYMENT_ID_KEY, String(paymentId));
    if (Array.isArray(subjectIds)) localStorage.setItem(CHECKOUT_SUBJECT_IDS_KEY, JSON.stringify(subjectIds));
  };

  const restorePendingCheckoutState = () => {
    const paymentId = Number(localStorage.getItem(CHECKOUT_PAYMENT_ID_KEY) || 0);
    let subjectIds = [];
    try {
      subjectIds = JSON.parse(localStorage.getItem(CHECKOUT_SUBJECT_IDS_KEY) || "[]");
    } catch {
      subjectIds = [];
    }
    return {
      paymentId: paymentId || null,
      subjectIds: Array.isArray(subjectIds) ? subjectIds.map((x) => Number(x)).filter(Boolean) : []
    };
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

    if (status === "SUCCESS") {
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
        const sub = await api("/api/subscriptions/checkout", "POST", { subjectIds: ids }, true);
        localStorage.setItem(KEYS.subscription, JSON.stringify(sub));
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
      } catch {
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
    loginLink.onclick = (e) => {
      e.preventDefault();
      nav("/dashboard", "dashboard.html");
    };
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
      row.className = `group relative flex flex-col gap-5 rounded-xl border bg-white p-6 transition-all shadow-sm ${
        purchased ? "border-emerald-200 bg-emerald-50/40" : "cursor-pointer border-slate-200 hover:border-primary/50"
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

    const restored = restorePendingCheckoutState();
    if (restored.paymentId) {
      state.paymentId = restored.paymentId;
      state.selectedSubjectIds = restored.subjectIds;
      lockSelection(boxes, true);
      setPayText("Waiting For Payment...");
      setQrStatus("Checking payment status...");
      payBtn.disabled = true;
      startPolling(state.paymentId);
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
          state.selectedSubjectIds = ids;
          savePendingCheckoutState(state.paymentId, state.selectedSubjectIds);
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
