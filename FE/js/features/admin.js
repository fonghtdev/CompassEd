import { api, checkSession, currentRole, nav, toast } from "./core.js";

function openAdminModal(title, bodyHtml) {
  let overlay = document.getElementById("admin-action-modal");
  if (!overlay) {
    overlay = document.createElement("div");
    overlay.id = "admin-action-modal";
    overlay.style.position = "fixed";
    overlay.style.inset = "0";
    overlay.style.background = "rgba(15,23,42,0.45)";
    overlay.style.display = "none";
    overlay.style.alignItems = "center";
    overlay.style.justifyContent = "center";
    overlay.style.zIndex = "9999";
    overlay.innerHTML = `
      <div style="width:min(1160px,96vw);max-height:90vh;overflow:auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;box-shadow:0 20px 42px rgba(2,6,23,0.24);">
        <div style="padding:14px 16px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between;gap:10px;position:sticky;top:0;background:#fff;z-index:1;">
          <strong id="admin-modal-title" style="font-size:16px;color:#0f172a;"></strong>
          <button id="admin-modal-close" style="border:0;background:#f8fafc;color:#0f172a;padding:6px 10px;border-radius:8px;cursor:pointer;">X</button>
        </div>
        <div id="admin-modal-body" style="padding:14px 16px;"></div>
      </div>`;
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) overlay.style.display = "none";
    });
    document.body.appendChild(overlay);
    overlay.querySelector("#admin-modal-close").addEventListener("click", () => {
      overlay.style.display = "none";
    });
  }
  const titleEl = overlay.querySelector("#admin-modal-title");
  const bodyEl = overlay.querySelector("#admin-modal-body");
  if (titleEl) titleEl.textContent = title;
  if (bodyEl) bodyEl.innerHTML = bodyHtml;
  overlay.style.display = "flex";
  return bodyEl;
}

function esc(text) {
  return String(text == null ? "" : text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}

function dt(value) {
  if (!value) return "-";
  try {
    return new Date(value).toLocaleString();
  } catch (e) {
    return String(value);
  }
}

async function showUsersModal() {
  const users = await api("/api/admin/users", "GET", null, true);
  const rows = (users || [])
    .map(
      (u) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${u.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(u.fullName || "-")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(u.email || "-")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;font-weight:700;">${esc(u.role || "USER")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">
          <button data-user-id="${u.id}" data-role="${u.role}" class="admin-user-role-btn" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Toggle Role</button>
        </td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    `Users (${Array.isArray(users) ? users.length : 0})`,
    `<div style="overflow:auto;">
      <table style="width:100%;border-collapse:collapse;font-size:13px;">
        <thead>
          <tr style="background:#f8fafc;text-align:left;">
            <th style="padding:8px;">ID</th>
            <th style="padding:8px;">Name</th>
            <th style="padding:8px;">Email</th>
            <th style="padding:8px;">Role</th>
            <th style="padding:8px;">Action</th>
          </tr>
        </thead>
        <tbody>${rows || '<tr><td colspan="5" style="padding:12px;">No users</td></tr>'}</tbody>
      </table>
    </div>`
  );
  body.querySelectorAll(".admin-user-role-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const userId = Number(btn.getAttribute("data-user-id"));
      const oldRole = String(btn.getAttribute("data-role") || "USER").toUpperCase();
      const newRole = oldRole === "ADMIN" ? "USER" : "ADMIN";
      try {
        await api(`/api/admin/users/${userId}/role`, "PUT", { role: newRole }, true);
        toast(`Updated user ${userId} to ${newRole}`);
        await showUsersModal();
      } catch (e) {
        toast(`Role update failed: ${e.message}`, "error");
      }
    });
  });
}

async function showSubjectsRoadmapsModal() {
  const [subjects, roadmaps] = await Promise.all([
    api("/api/admin/subjects", "GET", null, true),
    api("/api/admin/roadmaps", "GET", null, true)
  ]);
  const subjectOptions = (subjects || [])
    .map((s) => `<option value="${s.id}">${esc(s.code)} - ${esc(s.name)}</option>`)
    .join("");
  const subjectRows = (subjects || [])
    .map(
      (s) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${s.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="sub-code-${s.id}" value="${esc(s.code)}" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="sub-name-${s.id}" value="${esc(s.name)}" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button class="admin-subject-save-btn" data-id="${s.id}" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Save</button>
          <button class="admin-subject-delete-btn" data-id="${s.id}" style="border:1px solid #fecaca;background:#fff1f2;color:#b91c1c;padding:4px 8px;border-radius:8px;cursor:pointer;margin-left:4px;">Delete</button>
        </td>
      </tr>`
    )
    .join("");
  const roadmapRows = (roadmaps || [])
    .map(
      (r) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${r.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc((r.subject && r.subject.code) || "-")} / ${esc((r.subject && r.subject.name) || "-")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(r.level || "-")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="road-title-${r.id}" value="${esc(r.title || "")}" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="road-order-${r.id}" type="number" value="${Number(r.displayOrder || 0)}" style="width:90px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><textarea id="road-desc-${r.id}" rows="2" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;">${esc(r.description || "")}</textarea></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button class="admin-roadmap-save-btn" data-id="${r.id}" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Save</button>
          <button class="admin-roadmap-delete-btn" data-id="${r.id}" style="border:1px solid #fecaca;background:#fff1f2;color:#b91c1c;padding:4px 8px;border-radius:8px;cursor:pointer;margin-left:4px;">Delete</button>
        </td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    "Subjects + Roadmaps",
    `<div style="display:grid;gap:16px;">
      <div style="border:1px solid #e2e8f0;border-radius:12px;padding:12px;">
        <div style="font-weight:800;font-size:14px;margin-bottom:8px;">Create Subject</div>
        <div style="display:grid;grid-template-columns:120px 1fr auto;gap:8px;align-items:center;">
          <input id="subject-create-code" placeholder="CODE" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="subject-create-name" placeholder="Subject name" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <button id="subject-create-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Create</button>
        </div>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">ID</th><th style="padding:8px;">Code</th><th style="padding:8px;">Name</th><th style="padding:8px;">Actions</th></tr></thead>
          <tbody>${subjectRows || '<tr><td colspan="4" style="padding:12px;">No subjects</td></tr>'}</tbody>
        </table>
      </div>
      <div style="border:1px solid #e2e8f0;border-radius:12px;padding:12px;">
        <div style="font-weight:800;font-size:14px;margin-bottom:8px;">Create Roadmap</div>
        <div style="display:grid;grid-template-columns:1fr 120px 1fr 120px auto;gap:8px;align-items:center;">
          <select id="roadmap-create-subject" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;">${subjectOptions}</select>
          <select id="roadmap-create-level" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;"><option value="L1">L1</option><option value="L2">L2</option><option value="L3">L3</option></select>
          <input id="roadmap-create-title" placeholder="Roadmap title" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="roadmap-create-order" type="number" placeholder="Order" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <button id="roadmap-create-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Create</button>
        </div>
        <textarea id="roadmap-create-desc" rows="2" placeholder="Description" style="margin-top:8px;width:100%;padding:8px;border:1px solid #cbd5e1;border-radius:8px;"></textarea>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">ID</th><th style="padding:8px;">Subject</th><th style="padding:8px;">Level</th><th style="padding:8px;">Title</th><th style="padding:8px;">Order</th><th style="padding:8px;">Description</th><th style="padding:8px;">Actions</th></tr></thead>
          <tbody>${roadmapRows || '<tr><td colspan="7" style="padding:12px;">No roadmaps</td></tr>'}</tbody>
        </table>
      </div>
    </div>`
  );

  const createSubjectBtn = body.querySelector("#subject-create-btn");
  if (createSubjectBtn) {
    createSubjectBtn.addEventListener("click", async () => {
      const code = body.querySelector("#subject-create-code").value.trim();
      const name = body.querySelector("#subject-create-name").value.trim();
      if (!code || !name) {
        toast("Subject code and name are required", "warn");
        return;
      }
      try {
        await api("/api/admin/subjects", "POST", { code, name }, true);
        toast("Subject created");
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Create subject failed: ${e.message}`, "error");
      }
    });
  }
  body.querySelectorAll(".admin-subject-save-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      const code = body.querySelector(`#sub-code-${id}`).value.trim();
      const name = body.querySelector(`#sub-name-${id}`).value.trim();
      try {
        await api(`/api/admin/subjects/${id}`, "PUT", { code, name }, true);
        toast(`Subject ${id} updated`);
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Update subject failed: ${e.message}`, "error");
      }
    });
  });
  body.querySelectorAll(".admin-subject-delete-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      if (!confirm(`Delete subject ${id}?`)) return;
      try {
        await api(`/api/admin/subjects/${id}`, "DELETE", null, true);
        toast(`Subject ${id} deleted`);
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Delete subject failed: ${e.message}`, "error");
      }
    });
  });

  const createRoadmapBtn = body.querySelector("#roadmap-create-btn");
  if (createRoadmapBtn) {
    createRoadmapBtn.addEventListener("click", async () => {
      const subjectId = Number(body.querySelector("#roadmap-create-subject").value);
      const level = body.querySelector("#roadmap-create-level").value;
      const title = body.querySelector("#roadmap-create-title").value.trim();
      const description = body.querySelector("#roadmap-create-desc").value.trim();
      const displayOrder = Number(body.querySelector("#roadmap-create-order").value || 0);
      if (!subjectId || !level || !title) {
        toast("Subject, level and title are required", "warn");
        return;
      }
      try {
        await api("/api/admin/roadmaps", "POST", { subjectId, level, title, description, displayOrder }, true);
        toast("Roadmap created");
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Create roadmap failed: ${e.message}`, "error");
      }
    });
  }
  body.querySelectorAll(".admin-roadmap-save-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      const title = body.querySelector(`#road-title-${id}`).value.trim();
      const description = body.querySelector(`#road-desc-${id}`).value.trim();
      const displayOrder = Number(body.querySelector(`#road-order-${id}`).value || 0);
      try {
        await api(`/api/admin/roadmaps/${id}`, "PUT", { title, description, displayOrder }, true);
        toast(`Roadmap ${id} updated`);
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Update roadmap failed: ${e.message}`, "error");
      }
    });
  });
  body.querySelectorAll(".admin-roadmap-delete-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      if (!confirm(`Delete roadmap ${id}?`)) return;
      try {
        await api(`/api/admin/roadmaps/${id}`, "DELETE", null, true);
        toast(`Roadmap ${id} deleted`);
        await showSubjectsRoadmapsModal();
      } catch (e) {
        toast(`Delete roadmap failed: ${e.message}`, "error");
      }
    });
  });
}

async function showLessonsMiniTestsModal() {
  const [lessons, miniTests] = await Promise.all([
    api("/api/admin/lessons", "GET", null, true),
    api("/api/admin/mini-tests", "GET", null, true)
  ]);
  const lessonRows = (lessons || [])
    .map(
      (l) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${l.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="lesson-sub-${l.id}" value="${esc(l.subject || "")}" style="width:90px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="lesson-level-${l.id}" value="${esc(l.level || "")}" style="width:70px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="lesson-order-${l.id}" type="number" value="${Number(l.orderIndex || 1)}" style="width:80px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="lesson-title-${l.id}" value="${esc(l.title || "")}" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><textarea id="lesson-content-${l.id}" rows="2" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;">${esc(l.content || "")}</textarea></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button class="admin-lesson-save-btn" data-id="${l.id}" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Save</button>
          <button class="admin-lesson-delete-btn" data-id="${l.id}" style="border:1px solid #fecaca;background:#fff1f2;color:#b91c1c;padding:4px 8px;border-radius:8px;cursor:pointer;margin-left:4px;">Delete</button>
        </td>
      </tr>`
    )
    .join("");
  const miniRows = (miniTests || [])
    .map(
      (m) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${m.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="mini-sub-${m.id}" value="${esc(m.subject || "")}" style="width:90px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="mini-level-${m.id}" value="${esc(m.level || "")}" style="width:70px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="mini-lesson-${m.id}" type="number" value="${Number(m.lessonId || 1)}" style="width:80px;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><input id="mini-title-${m.id}" value="${esc(m.title || "")}" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;" /></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><textarea id="mini-questions-${m.id}" rows="2" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;">${esc(m.questions || "")}</textarea></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button class="admin-mini-save-btn" data-id="${m.id}" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Save</button>
          <button class="admin-mini-delete-btn" data-id="${m.id}" style="border:1px solid #fecaca;background:#fff1f2;color:#b91c1c;padding:4px 8px;border-radius:8px;cursor:pointer;margin-left:4px;">Delete</button>
        </td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    "Lessons + Mini-tests",
    `<div style="display:grid;gap:16px;">
      <div style="border:1px solid #e2e8f0;border-radius:12px;padding:12px;">
        <div style="font-weight:800;font-size:14px;margin-bottom:8px;">Create Lesson</div>
        <div style="display:grid;grid-template-columns:120px 80px 80px 1fr auto;gap:8px;align-items:center;">
          <input id="lesson-create-sub" placeholder="SUBJECT" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="lesson-create-level" placeholder="L1" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="lesson-create-order" type="number" placeholder="Order" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="lesson-create-title" placeholder="Lesson title" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <button id="lesson-create-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Create</button>
        </div>
        <textarea id="lesson-create-content" rows="2" placeholder="Lesson content" style="margin-top:8px;width:100%;padding:8px;border:1px solid #cbd5e1;border-radius:8px;"></textarea>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">ID</th><th style="padding:8px;">Subject</th><th style="padding:8px;">Level</th><th style="padding:8px;">Order</th><th style="padding:8px;">Title</th><th style="padding:8px;">Content</th><th style="padding:8px;">Actions</th></tr></thead>
          <tbody>${lessonRows || '<tr><td colspan="7" style="padding:12px;">No lessons</td></tr>'}</tbody>
        </table>
      </div>
      <div style="border:1px solid #e2e8f0;border-radius:12px;padding:12px;">
        <div style="font-weight:800;font-size:14px;margin-bottom:8px;">Create Mini-test</div>
        <div style="display:grid;grid-template-columns:120px 80px 100px 1fr auto;gap:8px;align-items:center;">
          <input id="mini-create-sub" placeholder="SUBJECT" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="mini-create-level" placeholder="L1" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="mini-create-lesson-id" type="number" placeholder="Lesson ID" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <input id="mini-create-title" placeholder="Mini-test title" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <button id="mini-create-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Create</button>
        </div>
        <textarea id="mini-create-questions" rows="2" placeholder="Questions (json/text)" style="margin-top:8px;width:100%;padding:8px;border:1px solid #cbd5e1;border-radius:8px;"></textarea>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">ID</th><th style="padding:8px;">Subject</th><th style="padding:8px;">Level</th><th style="padding:8px;">Lesson</th><th style="padding:8px;">Title</th><th style="padding:8px;">Questions</th><th style="padding:8px;">Actions</th></tr></thead>
          <tbody>${miniRows || '<tr><td colspan="7" style="padding:12px;">No mini-tests</td></tr>'}</tbody>
        </table>
      </div>
    </div>`
  );

  const lessonCreateBtn = body.querySelector("#lesson-create-btn");
  if (lessonCreateBtn) {
    lessonCreateBtn.addEventListener("click", async () => {
      const subject = body.querySelector("#lesson-create-sub").value.trim();
      const level = body.querySelector("#lesson-create-level").value.trim();
      const orderIndex = Number(body.querySelector("#lesson-create-order").value || 1);
      const title = body.querySelector("#lesson-create-title").value.trim();
      const content = body.querySelector("#lesson-create-content").value.trim();
      if (!subject || !level || !title || !orderIndex) {
        toast("subject, level, orderIndex and title are required", "warn");
        return;
      }
      try {
        await api("/api/admin/lessons", "POST", { subject, level, orderIndex, title, content }, true);
        toast("Lesson created");
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Create lesson failed: ${e.message}`, "error");
      }
    });
  }
  body.querySelectorAll(".admin-lesson-save-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      const subject = body.querySelector(`#lesson-sub-${id}`).value.trim();
      const level = body.querySelector(`#lesson-level-${id}`).value.trim();
      const orderIndex = Number(body.querySelector(`#lesson-order-${id}`).value || 1);
      const title = body.querySelector(`#lesson-title-${id}`).value.trim();
      const content = body.querySelector(`#lesson-content-${id}`).value.trim();
      try {
        await api(`/api/admin/lessons/${id}`, "PUT", { subject, level, orderIndex, title, content }, true);
        toast(`Lesson ${id} updated`);
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Update lesson failed: ${e.message}`, "error");
      }
    });
  });
  body.querySelectorAll(".admin-lesson-delete-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      if (!confirm(`Delete lesson ${id}?`)) return;
      try {
        await api(`/api/admin/lessons/${id}`, "DELETE", null, true);
        toast(`Lesson ${id} deleted`);
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Delete lesson failed: ${e.message}`, "error");
      }
    });
  });

  const miniCreateBtn = body.querySelector("#mini-create-btn");
  if (miniCreateBtn) {
    miniCreateBtn.addEventListener("click", async () => {
      const subject = body.querySelector("#mini-create-sub").value.trim();
      const level = body.querySelector("#mini-create-level").value.trim();
      const lessonId = Number(body.querySelector("#mini-create-lesson-id").value || 0);
      const title = body.querySelector("#mini-create-title").value.trim();
      const questions = body.querySelector("#mini-create-questions").value.trim();
      if (!subject || !level || !lessonId || !title) {
        toast("subject, level, lessonId and title are required", "warn");
        return;
      }
      try {
        await api("/api/admin/mini-tests", "POST", { subject, level, lessonId, title, questions }, true);
        toast("Mini-test created");
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Create mini-test failed: ${e.message}`, "error");
      }
    });
  }
  body.querySelectorAll(".admin-mini-save-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      const subject = body.querySelector(`#mini-sub-${id}`).value.trim();
      const level = body.querySelector(`#mini-level-${id}`).value.trim();
      const lessonId = Number(body.querySelector(`#mini-lesson-${id}`).value || 0);
      const title = body.querySelector(`#mini-title-${id}`).value.trim();
      const questions = body.querySelector(`#mini-questions-${id}`).value.trim();
      try {
        await api(`/api/admin/mini-tests/${id}`, "PUT", { subject, level, lessonId, title, questions }, true);
        toast(`Mini-test ${id} updated`);
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Update mini-test failed: ${e.message}`, "error");
      }
    });
  });
  body.querySelectorAll(".admin-mini-delete-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-id"));
      if (!confirm(`Delete mini-test ${id}?`)) return;
      try {
        await api(`/api/admin/mini-tests/${id}`, "DELETE", null, true);
        toast(`Mini-test ${id} deleted`);
        await showLessonsMiniTestsModal();
      } catch (e) {
        toast(`Delete mini-test failed: ${e.message}`, "error");
      }
    });
  });
}

async function showConfigsModal() {
  const configs = await api("/api/admin/configs", "GET", null, true);
  const rows = (configs || [])
    .map(
      (c, idx) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;font-weight:700;">${esc(c.configKey)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;"><textarea id="cfg-val-${idx}" rows="2" style="width:100%;padding:6px;border:1px solid #cbd5e1;border-radius:8px;">${esc(c.configValue || "")}</textarea></td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${dt(c.updatedAt)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button class="admin-config-save-btn" data-key="${esc(c.configKey)}" data-value-id="cfg-val-${idx}" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Save</button>
          <button class="admin-config-delete-btn" data-key="${esc(c.configKey)}" style="border:1px solid #fecaca;background:#fff1f2;color:#b91c1c;padding:4px 8px;border-radius:8px;cursor:pointer;margin-left:4px;">Delete</button>
        </td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    "System Configs",
    `<div style="display:grid;gap:12px;">
      <div style="display:grid;grid-template-columns:240px 1fr auto;gap:8px;">
        <input id="config-create-key" placeholder="CONFIG_KEY" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
        <input id="config-create-value" placeholder="Config value" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
        <button id="config-create-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Create/Upsert</button>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">Key</th><th style="padding:8px;">Value</th><th style="padding:8px;">Updated</th><th style="padding:8px;">Actions</th></tr></thead>
          <tbody>${rows || '<tr><td colspan="4" style="padding:12px;">No configs</td></tr>'}</tbody>
        </table>
      </div>
    </div>`
  );
  const createBtn = body.querySelector("#config-create-btn");
  if (createBtn) {
    createBtn.addEventListener("click", async () => {
      const key = body.querySelector("#config-create-key").value.trim();
      const value = body.querySelector("#config-create-value").value;
      if (!key) {
        toast("Config key is required", "warn");
        return;
      }
      try {
        await api(`/api/admin/configs/${encodeURIComponent(key)}`, "PUT", { value }, true);
        toast(`Config ${key} upserted`);
        await showConfigsModal();
      } catch (e) {
        toast(`Upsert config failed: ${e.message}`, "error");
      }
    });
  }
  body.querySelectorAll(".admin-config-save-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const key = btn.getAttribute("data-key");
      const valueId = btn.getAttribute("data-value-id");
      const value = body.querySelector(`#${valueId}`)?.value ?? "";
      try {
        await api(`/api/admin/configs/${encodeURIComponent(key)}`, "PUT", { value }, true);
        toast(`Config ${key} updated`);
        await showConfigsModal();
      } catch (e) {
        toast(`Update config failed: ${e.message}`, "error");
      }
    });
  });
  body.querySelectorAll(".admin-config-delete-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const key = btn.getAttribute("data-key");
      if (!confirm(`Delete config ${key}?`)) return;
      try {
        await api(`/api/admin/configs/${encodeURIComponent(key)}`, "DELETE", null, true);
        toast(`Config ${key} deleted`);
        await showConfigsModal();
      } catch (e) {
        toast(`Delete config failed: ${e.message}`, "error");
      }
    });
  });
}

async function showNotificationCenterModal() {
  const [notifications, users] = await Promise.all([
    api("/api/admin/notifications", "GET", null, true),
    api("/api/admin/users", "GET", null, true)
  ]);
  const userOptions = (users || [])
    .map((u) => `<option value="${u.id}">${u.id} - ${esc(u.email || u.fullName || "User")}</option>`)
    .join("");
  const rows = (notifications || [])
    .slice(0, 100)
    .map(
      (n) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${n.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(n.userEmail || n.userId || "-")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(n.title)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(n.type)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${n.read ? "Read" : "Unread"}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${dt(n.createdAt)}</td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    "Notification Center",
    `<div style="display:grid;gap:12px;">
      <div style="border:1px solid #e2e8f0;border-radius:12px;padding:12px;display:grid;gap:8px;">
        <div style="font-weight:800;font-size:14px;">Compose notification</div>
        <div style="display:grid;grid-template-columns:180px 180px 1fr auto;gap:8px;">
          <select id="notify-type" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;">
            <option value="GENERAL">GENERAL</option>
            <option value="MAIL">MAIL</option>
            <option value="SYSTEM">SYSTEM</option>
          </select>
          <select id="notify-user-id" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;">
            <option value="">Broadcast to all users</option>
            ${userOptions}
          </select>
          <input id="notify-title" placeholder="Notification title" style="padding:8px;border:1px solid #cbd5e1;border-radius:8px;" />
          <button id="notify-send-btn" style="border:0;background:#0d7ff2;color:#fff;padding:8px 10px;border-radius:8px;cursor:pointer;">Send</button>
        </div>
        <textarea id="notify-message" rows="3" placeholder="Notification message" style="width:100%;padding:8px;border:1px solid #cbd5e1;border-radius:8px;"></textarea>
      </div>
      <div style="overflow:auto;border:1px solid #e2e8f0;border-radius:12px;">
        <table style="width:100%;border-collapse:collapse;font-size:13px;">
          <thead><tr style="background:#f8fafc;text-align:left;"><th style="padding:8px;">ID</th><th style="padding:8px;">User</th><th style="padding:8px;">Title</th><th style="padding:8px;">Type</th><th style="padding:8px;">Status</th><th style="padding:8px;">Created</th></tr></thead>
          <tbody>${rows || '<tr><td colspan="6" style="padding:12px;">No notifications</td></tr>'}</tbody>
        </table>
      </div>
    </div>`
  );

  const sendBtn = body.querySelector("#notify-send-btn");
  if (sendBtn) {
    sendBtn.addEventListener("click", async () => {
      const type = body.querySelector("#notify-type").value;
      const userIdRaw = body.querySelector("#notify-user-id").value;
      const title = body.querySelector("#notify-title").value.trim();
      const message = body.querySelector("#notify-message").value.trim();
      if (!title || !message) {
        toast("Title and message are required", "warn");
        return;
      }
      const payload = {
        title,
        message,
        type,
        broadcast: !userIdRaw
      };
      if (userIdRaw) payload.userId = Number(userIdRaw);
      try {
        const res = await api("/api/admin/notifications", "POST", payload, true);
        toast(`Sent notifications: ${Number(res.created || 0)}`);
        await showNotificationCenterModal();
      } catch (e) {
        toast(`Send notification failed: ${e.message}`, "error");
      }
    });
  }
}

async function showAiLogsModal() {
  const logs = await api("/api/admin/ai/logs", "GET", null, true);
  const rows = (logs || [])
    .slice(0, 80)
    .map(
      (x) => `
      <tr>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${x.id}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(x.taskType)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${esc(x.subjectCode)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;font-weight:700;">${esc(x.reviewStatus || "PENDING")}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;">${dt(x.createdAt)}</td>
        <td style="padding:8px;border-bottom:1px solid #f1f5f9;white-space:nowrap;">
          <button data-log-id="${x.id}" class="admin-log-review-btn" style="border:1px solid #cbd5e1;background:#fff;padding:4px 8px;border-radius:8px;cursor:pointer;">Mark Reviewed</button>
        </td>
      </tr>`
    )
    .join("");
  const body = openAdminModal(
    `AI Logs (${Array.isArray(logs) ? logs.length : 0})`,
    `<div style="overflow:auto;">
      <table style="width:100%;border-collapse:collapse;font-size:13px;">
        <thead>
          <tr style="background:#f8fafc;text-align:left;">
            <th style="padding:8px;">ID</th>
            <th style="padding:8px;">Task</th>
            <th style="padding:8px;">Subject</th>
            <th style="padding:8px;">Review</th>
            <th style="padding:8px;">Created</th>
            <th style="padding:8px;">Action</th>
          </tr>
        </thead>
        <tbody>${rows || '<tr><td colspan="6" style="padding:12px;">No AI logs</td></tr>'}</tbody>
      </table>
    </div>`
  );
  body.querySelectorAll(".admin-log-review-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const id = Number(btn.getAttribute("data-log-id"));
      if (!id) return;
      try {
        await api(`/api/admin/ai/logs/${id}/review`, "POST", { reviewStatus: "APPROVED", reviewNote: "Reviewed from dashboard" }, true);
        toast(`Reviewed AI log ${id}`);
        await showAiLogsModal();
      } catch (e) {
        toast(`Review failed: ${e.message}`, "error");
      }
    });
  });
}

function bindAction(id, fn) {
  const el = document.getElementById(id);
  if (!el) return;
  el.addEventListener("click", async (e) => {
    e.preventDefault();
    try {
      await fn();
    } catch (err) {
      toast(err.message || "Action failed", "error");
    }
  });
}

async function initAdmin() {
  const ok = await checkSession();
  if (!ok) {
    nav("/auth", "auth.html");
    return;
  }
  if (currentRole() !== "ADMIN") {
    toast("Admin access required", "warn");
    nav("/landing", "landingPage.html");
    return;
  }

  try {
    const overview = await api("/api/admin/analytics/overview", "GET", null, true);
    const set = (id, value) => {
      const el = document.getElementById(id);
      if (el) el.textContent = value;
    };
    set("admin-total-users", String(overview.totalUsers ?? 0));
    set("admin-active-subs", String(overview.activeSubscriptions ?? 0));
    set("admin-pass-rate", `${Number(overview.passRatePercent ?? 0).toFixed(1)}%`);
    set("admin-avg-score", `${Number(overview.averageScorePercent ?? 0).toFixed(1)}%`);
  } catch (e) {
    toast(`Cannot load admin analytics: ${e.message}`, "error");
  }

  const menuBtn = document.getElementById("admin-menu-btn");
  if (menuBtn) {
    menuBtn.addEventListener("click", () => {
      const sidebar = document.querySelector("body > div > div:first-child");
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

  bindAction("admin-view-all-users-btn", showUsersModal);
  bindAction("admin-more-updates-btn", showAiLogsModal);
  bindAction("admin-notifications-btn", showNotificationCenterModal);
  bindAction("admin-mail-btn", showNotificationCenterModal);

  bindAction("admin-open-subject-roadmap-btn", showSubjectsRoadmapsModal);
  bindAction("admin-open-content-btn", showLessonsMiniTestsModal);
  bindAction("admin-open-config-btn", showConfigsModal);
  bindAction("admin-open-notification-center-btn", showNotificationCenterModal);

  bindAction("admin-nav-users", showUsersModal);
  bindAction("admin-nav-content-bank", showLessonsMiniTestsModal);
  bindAction("admin-nav-roadmaps", showSubjectsRoadmapsModal);
  bindAction("admin-nav-settings", showConfigsModal);
  bindAction("admin-nav-notifications", showNotificationCenterModal);
  bindAction("admin-nav-analytics", showAiLogsModal);

  // Question Bank - allow normal navigation
  const questionBankLink = document.getElementById("admin-nav-question-bank");
  if (questionBankLink) {
    // Don't prevent default - let it navigate normally
  }

  const overviewLink = document.getElementById("admin-nav-overview");
  if (overviewLink) {
    overviewLink.addEventListener("click", (e) => {
      e.preventDefault();
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  }
}

export { initAdmin };
