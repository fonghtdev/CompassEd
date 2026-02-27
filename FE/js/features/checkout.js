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
  const loginLink = document.getElementById("landing-login-link");
  const getStartedTop = document.getElementById("landing-get-started-top");
  const notifWrap = document.getElementById("landing-notif-wrap");
  if (!listEl || !totalEl) return;

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
    const [subjects, plans] = await Promise.all([
      api("/api/subjects", "GET", null, false),
      api("/api/pricing/plans", "GET", null, false)
    ]);

    const subjectRows = Array.isArray(subjects) ? subjects : [];
    const planMap = new Map((plans || []).map((p) => [Number(p.subjectCount), Number(p.amountVnd)]));
    const single = priceByCount(1, planMap);

    listEl.innerHTML = "";
    subjectRows.forEach((subject) => {
      const row = document.createElement("label");
      row.className =
        "group relative flex cursor-pointer flex-col gap-5 rounded-xl border border-slate-200 bg-white p-6 hover:border-primary/50 transition-all shadow-sm";
      row.innerHTML = `
        <div class="absolute top-4 right-4">
          <input class="checkout-subject size-5 rounded border-slate-300 text-primary focus:ring-primary" type="checkbox" value="${subject.id}" />
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
          <li class="flex items-center gap-2 text-sm text-slate-600">
            <span class="material-symbols-outlined text-green-500 text-sm">check_circle</span>
            Personalized roadmap
          </li>
          <li class="flex items-center gap-2 text-sm text-slate-600">
            <span class="material-symbols-outlined text-green-500 text-sm">check_circle</span>
            Progress tracking
          </li>
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

      if (selectedCountEl) {
        selectedCountEl.textContent = `${ids.length}/${subjectRows.length}`;
      }

      if (selectedListEl) {
        const picked = subjectRows.filter((s) => ids.includes(Number(s.id)));
        if (!picked.length) {
          selectedListEl.innerHTML = '<div class="text-slate-400">No subject selected</div>';
        } else {
          selectedListEl.innerHTML = picked
            .map((s) => `<div class="flex justify-between items-center"><span class="text-slate-500">${s.name}</span><span class="text-slate-700">${formatVnd(single)}</span></div>`)
            .join("");
        }
      }

      const original = ids.length * single;
      const discount = Math.max(0, original - total);
      if (discountEl) discountEl.textContent = formatVnd(discount);

      if (tipEl) {
        if (ids.length >= 3) {
          tipEl.textContent = "Best value bundle applied.";
        } else if (ids.length === 2) {
          tipEl.textContent = "Great! You are saving with 2-subject bundle.";
        } else {
          tipEl.textContent = "Tip: Buy 2 subjects to save more.";
        }
      }
    }

    rerenderTotal();

    if (payBtn) {
      payBtn.addEventListener("click", async () => {
        const ids = selectedIds();
        if (!ids.length) {
          toast("Select at least 1 subject", "warn");
          return;
        }

        showLoading("Processing checkout...");
        payBtn.disabled = true;
        if (payText) payText.textContent = "Processing...";

        try {
          const sub = await api("/api/subscriptions/checkout", "POST", { subjectIds: ids }, true);
          localStorage.setItem(KEYS.subscription, JSON.stringify(sub));
          localStorage.setItem(KEYS.subjectId, String(ids[0]));
          nav(`/learning-roadmap?subjectId=${ids[0]}`, `coursesDetail.html?subjectId=${ids[0]}`);
        } catch (err) {
          toast(`Checkout failed: ${err.message}`, "error");
        } finally {
          hideLoading();
          payBtn.disabled = false;
          if (payText) payText.textContent = "Pay and Unlock Roadmap";
        }
      });
    }
  } catch (err) {
    toast(`Checkout load failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }
}

export { initCheckout };
