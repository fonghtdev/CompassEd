import { api, checkSession, currentRole, hideLoading, nav, showLoading, toast } from "./core.js";

function fillProfile(p) {
  const set = (id, value) => {
    const el = document.getElementById(id);
    if (el) el.value = value == null ? "" : String(value);
  };
  set("profile-email", p.email);
  set("profile-fullname", p.fullName);
  const notifyEmail = document.getElementById("profile-notify-email");
  const notifyInApp = document.getElementById("profile-notify-inapp");
  if (notifyEmail) notifyEmail.checked = !!p.notifyEmail;
  if (notifyInApp) notifyInApp.checked = !!p.notifyInApp;
}

async function initProfile() {
  const ok = await checkSession();
  if (!ok) {
    nav("/auth", "auth.html");
    return;
  }
  if (currentRole() === "ADMIN") {
    nav("/admin-dashboard", "admin/adminDashboard.html");
    return;
  }

  showLoading("Loading profile...");
  try {
    const profile = await api("/api/me/profile", "GET", null, true);
    fillProfile(profile);
  } catch (err) {
    toast(`Load profile failed: ${err.message}`, "error");
  } finally {
    hideLoading();
  }

  const form = document.getElementById("profile-form");
  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      showLoading("Saving profile...");
      try {
        const payload = {
          fullName: document.getElementById("profile-fullname").value.trim(),
          notifyEmail: document.getElementById("profile-notify-email").checked,
          notifyInApp: document.getElementById("profile-notify-inapp").checked
        };
        const updated = await api("/api/me/profile", "PUT", payload, true);
        fillProfile(updated);
        toast("Profile updated");
      } catch (err) {
        toast(`Save failed: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });
  }

  const passwordForm = document.getElementById("profile-password-form");
  if (passwordForm) {
    passwordForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const currentPassword = document.getElementById("profile-current-password").value;
      const newPassword = document.getElementById("profile-new-password").value;
      const confirmPassword = document.getElementById("profile-confirm-password").value;
      if (!newPassword || newPassword.length < 6) {
        toast("New password must be at least 6 characters", "warn");
        return;
      }
      if (newPassword !== confirmPassword) {
        toast("Confirm password does not match", "warn");
        return;
      }
      showLoading("Updating password...");
      try {
        await api("/api/me/password", "PUT", { currentPassword, newPassword }, true);
        document.getElementById("profile-current-password").value = "";
        document.getElementById("profile-new-password").value = "";
        document.getElementById("profile-confirm-password").value = "";
        toast("Password updated");
      } catch (err) {
        toast(`Update password failed: ${err.message}`, "error");
      } finally {
        hideLoading();
      }
    });
  }

  const goDash = document.getElementById("profile-go-dashboard");
  if (goDash) goDash.addEventListener("click", () => nav("/dashboard", "dashboard.html"));
  const goLanding = document.getElementById("profile-go-landing");
  if (goLanding) goLanding.addEventListener("click", () => nav("/landing", "landingPage.html"));
}

export { initProfile };
