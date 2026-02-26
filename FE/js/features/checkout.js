import {
  KEYS,
  api,
  checkSession,
  formatVnd,
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
  const backBtn = document.getElementById("checkout-back");
  const payBtn = document.getElementById("checkout-pay");
  if (!listEl || !totalEl) return;

  showLoading("Loading checkout...");
  try {
    const [subjects, plans] = await Promise.all([
      api("/api/subjects", "GET", null, false),
      api("/api/pricing/plans", "GET", null, false)
    ]);
    const planMap = new Map((plans || []).map((p) => [Number(p.subjectCount), Number(p.amountVnd)]));
    const single = priceByCount(1, planMap);

    listEl.innerHTML = "";
    (subjects || []).forEach((subject) => {
      const row = document.createElement("label");
      row.className = "flex items-center justify-between rounded-lg border border-slate-200 px-4 py-3";
      row.innerHTML = `
        <span class="font-semibold text-slate-800">${subject.name}</span>
        <div class="flex items-center gap-4">
          <span class="text-slate-500">${formatVnd(single)}</span>
          <input class="checkout-subject" type="checkbox" value="${subject.id}" />
        </div>`;
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
      totalEl.textContent = formatVnd(priceByCount(ids.length, planMap));
    }

    rerenderTotal();

    if (backBtn) backBtn.addEventListener("click", () => nav("/placement-result", "placementTestResult.html"));
    if (payBtn) {
      payBtn.addEventListener("click", async () => {
        const ids = selectedIds();
        if (!ids.length) {
          toast("Select at least 1 subject", "warn");
          return;
        }
        showLoading("Processing checkout...");
        try {
          const sub = await api("/api/subscriptions/checkout", "POST", { subjectIds: ids }, true);
          localStorage.setItem(KEYS.subscription, JSON.stringify(sub));
          localStorage.setItem(KEYS.subjectId, String(ids[0]));
          nav("/learning-roadmap", "coursesDetail.html");
        } catch (err) {
          toast(`Checkout failed: ${err.message}`, "error");
        } finally {
          hideLoading();
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
