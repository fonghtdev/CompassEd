import { api, toast } from "./core.js";

function ensureInlineProfilePanel() {
  let wrap = document.getElementById("inline-profile-wrap");
  if (wrap) return wrap;

  wrap = document.createElement("div");
  wrap.id = "inline-profile-wrap";
  wrap.style.position = "fixed";
  wrap.style.inset = "0";
  wrap.style.background = "rgba(15,23,42,0.32)";
  wrap.style.display = "none";
  wrap.style.zIndex = "5000";
  wrap.innerHTML = `
    <div id="inline-profile-panel" style="position:absolute;top:0;right:0;width:min(440px,96vw);height:100%;background:#fff;border-left:1px solid #e2e8f0;box-shadow:-12px 0 28px rgba(2,6,23,0.18);display:flex;flex-direction:column;">
      <div style="padding:14px 16px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;">
        <strong style="font-size:16px;color:#0f172a;">Profile</strong>
        <button id="inline-profile-close" style="border:0;background:#f8fafc;border-radius:8px;padding:6px 10px;cursor:pointer;">X</button>
      </div>
      <div id="inline-profile-content" style="padding:14px 16px;overflow:auto;display:grid;gap:14px;"></div>
    </div>`;
  wrap.addEventListener("click", (e) => {
    if (e.target === wrap) wrap.style.display = "none";
  });
  document.body.appendChild(wrap);
  wrap.querySelector("#inline-profile-close").addEventListener("click", () => {
    wrap.style.display = "none";
  });
  return wrap;
}

function renderProfileHtml(profile) {
  return `
    <form id="inline-profile-form" style="display:grid;gap:10px;">
      <label style="font-size:12px;font-weight:700;color:#334155;">Email</label>
      <input id="inline-profile-email" disabled value="${profile.email || ""}" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;background:#f8fafc;" />
      <label style="font-size:12px;font-weight:700;color:#334155;">Full Name</label>
      <input id="inline-profile-name" value="${profile.fullName || ""}" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
      <label style="display:flex;align-items:center;gap:8px;">
        <input id="inline-profile-notify-email" type="checkbox" ${profile.notifyEmail ? "checked" : ""} />
        <span style="font-size:13px;color:#334155;">Receive Email Notifications</span>
      </label>
      <label style="display:flex;align-items:center;gap:8px;">
        <input id="inline-profile-notify-inapp" type="checkbox" ${profile.notifyInApp ? "checked" : ""} />
        <span style="font-size:13px;color:#334155;">Receive In-App Notifications</span>
      </label>
      <button type="submit" style="margin-top:4px;padding:10px 12px;border:none;border-radius:8px;background:#0d7ff2;color:#fff;font-weight:700;cursor:pointer;">Save Profile</button>
    </form>
    <div style="border-top:1px solid #e2e8f0;padding-top:12px;display:grid;gap:10px;">
      <div style="font-size:14px;font-weight:800;color:#0f172a;">Change Password</div>
      <form id="inline-password-form" style="display:grid;gap:10px;">
        <input id="inline-current-password" type="password" placeholder="Current Password" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
        <input id="inline-new-password" type="password" minlength="6" placeholder="New Password (>=6)" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
        <input id="inline-confirm-password" type="password" minlength="6" placeholder="Confirm New Password" style="padding:10px;border:1px solid #e2e8f0;border-radius:8px;" />
        <button type="submit" style="padding:10px 12px;border:none;border-radius:8px;background:#111827;color:#fff;font-weight:700;cursor:pointer;">Update Password</button>
      </form>
    </div>`;
}

async function openInlineProfilePanel() {
  const wrap = ensureInlineProfilePanel();
  const content = wrap.querySelector("#inline-profile-content");
  content.innerHTML = "Loading...";
  wrap.style.display = "block";

  try {
    const profile = await api("/api/me/profile", "GET", null, true);
    content.innerHTML = renderProfileHtml(profile);

    const profileForm = content.querySelector("#inline-profile-form");
    if (profileForm) {
      profileForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
          const fullName = content.querySelector("#inline-profile-name").value.trim();
          const notifyEmail = !!content.querySelector("#inline-profile-notify-email").checked;
          const notifyInApp = !!content.querySelector("#inline-profile-notify-inapp").checked;
          await api("/api/me/profile", "PUT", { fullName, notifyEmail, notifyInApp }, true);
          toast("Profile updated");
        } catch (err) {
          toast(`Save profile failed: ${err.message}`, "error");
        }
      });
    }

    const passForm = content.querySelector("#inline-password-form");
    if (passForm) {
      passForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const currentPassword = content.querySelector("#inline-current-password").value;
        const newPassword = content.querySelector("#inline-new-password").value;
        const confirmPassword = content.querySelector("#inline-confirm-password").value;
        if (!newPassword || newPassword.length < 6) {
          toast("New password must be at least 6 characters", "warn");
          return;
        }
        if (newPassword !== confirmPassword) {
          toast("Confirm password does not match", "warn");
          return;
        }
        try {
          await api("/api/me/password", "PUT", { currentPassword, newPassword }, true);
          content.querySelector("#inline-current-password").value = "";
          content.querySelector("#inline-new-password").value = "";
          content.querySelector("#inline-confirm-password").value = "";
          toast("Password updated");
        } catch (err) {
          toast(`Update password failed: ${err.message}`, "error");
        }
      });
    }
  } catch (err) {
    content.innerHTML = `<div style="color:#b91c1c;">Cannot load profile: ${err.message}</div>`;
  }
}

export { openInlineProfilePanel };
