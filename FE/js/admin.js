// Admin Dashboard JavaScript for CompassED
(function() {
    const API_BASE = "http://localhost:8080";
    const TOKEN_KEY = "compassed_admin_token";
    const USER_KEY = "compassed_admin_user";

    // ========== AUTH ==========
    
    function getAuth() {
        const token = localStorage.getItem(TOKEN_KEY);
        const user = JSON.parse(localStorage.getItem(USER_KEY) || "null");
        return { token, user };
    }

    function saveAuth(token, user) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }

    function clearAuth() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    }

    async function api(path, method = "GET", body = null) {
        const { token } = getAuth();
        const headers = { "Content-Type": "application/json" };
        
        if (token) headers["Authorization"] = `Bearer ${token}`;

        const res = await fetch(`${API_BASE}${path}`, {
            method,
            headers,
            body: body ? JSON.stringify(body) : undefined
        });

        if (!res.ok) throw new Error(await res.text() || `HTTP ${res.status}`);
        return res.json();
    }

    function toast(msg, type = "info") {
        const container = document.getElementById("toast-container");
        if (!container) return;

        const div = document.createElement("div");
        div.className = `px-6 py-4 rounded-lg shadow-lg text-white ${
            type === "error" ? "bg-red-600" : 
            type === "success" ? "bg-green-600" : "bg-blue-600"
        }`;
        div.textContent = msg;
        container.appendChild(div);
        setTimeout(() => div.remove(), 3000);
    }

    function loading(show) {
        const el = document.getElementById("loading-overlay");
        if (el) el.classList.toggle("hidden", !show);
    }

    // ========== ADMIN LOGIN ==========

    function initLogin() {
        const form = document.getElementById("admin-login-form");
        if (!form) return;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            loading(true);

            try {
                const email = document.getElementById("admin-email").value.trim();
                const password = document.getElementById("admin-password").value;

                const res = await api("/api/auth/login", "POST", { email, password });

                if (res.user.role !== "ADMIN") {
                    throw new Error("Bạn không có quyền Admin");
                }

                saveAuth(res.token, res.user);
                toast("Đăng nhập thành công!", "success");
                setTimeout(() => window.location.href = "/admin", 1000);

            } catch (err) {
                toast(err.message, "error");
            } finally {
                loading(false);
            }
        });
    }

    // ========== ADMIN DASHBOARD ==========

    async function checkAuth() {
        const { token, user } = getAuth();
        if (!token || !user) {
            window.location.href = "/admin-login";
            return false;
        }

        try {
            const me = await api("/api/auth/me");
            if (me.role !== "ADMIN") throw new Error();

            const nameEl = document.getElementById("admin-name");
            const emailEl = document.getElementById("admin-email");
            if (nameEl) nameEl.textContent = me.fullName || "Admin";
            if (emailEl) emailEl.textContent = me.email;

            return true;
        } catch {
            clearAuth();
            window.location.href = "/admin-login";
            return false;
        }
    }

    async function loadStats() {
        try {
            const stats = await api("/api/admin/stats");
            document.getElementById("stat-total-users").textContent = stats.totalUsers || 0;
            document.getElementById("stat-subscriptions").textContent = stats.totalSubscriptions || 0;
            document.getElementById("stat-placements").textContent = stats.totalPlacementAttempts || 0;
            document.getElementById("stat-subjects").textContent = stats.totalSubjects || 3;
        } catch (err) {
            console.error(err);
            toast("Không tải được thống kê", "error");
        }
    }

    // ========== USER MANAGEMENT ==========

    async function loadUsers() {
        const tbody = document.getElementById("users-tbody");
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" class="px-4 py-8 text-center">Đang tải...</td></tr>';

        try {
            const users = await api("/api/admin/users");
            
            if (!users || users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="px-4 py-8 text-center text-slate-500">Chưa có users</td></tr>';
                return;
            }

            tbody.innerHTML = users.map(u => `
                <tr class="border-b hover:bg-slate-50">
                    <td class="px-4 py-3 text-sm">${u.id}</td>
                    <td class="px-4 py-3 text-sm">${u.email}</td>
                    <td class="px-4 py-3 text-sm">${u.fullName || '-'}</td>
                    <td class="px-4 py-3">
                        <span class="px-2 py-1 text-xs rounded ${u.role === 'ADMIN' ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'}">
                            ${u.role}
                        </span>
                    </td>
                    <td class="px-4 py-3 text-sm">${u.provider || 'local'}</td>
                    <td class="px-4 py-3 text-center">
                        <button onclick="window.editUser(${u.id})" 
                                class="px-3 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600 mr-1">
                            <i class="fas fa-edit"></i> Sửa
                        </button>
                        <button onclick="window.toggleAdmin(${u.id}, '${u.role}')" 
                                class="px-3 py-1 text-xs ${u.role === 'ADMIN' ? 'bg-yellow-500 hover:bg-yellow-600' : 'bg-green-500 hover:bg-green-600'} text-white rounded mr-1">
                            ${u.role === 'ADMIN' ? '<i class="fas fa-user-minus"></i> Hủy Admin' : '<i class="fas fa-user-shield"></i> Set Admin'}
                        </button>
                        <button onclick="window.changePassword(${u.id}, '${u.email}')" 
                                class="px-3 py-1 text-xs bg-purple-500 text-white rounded hover:bg-purple-600 mr-1">
                            <i class="fas fa-key"></i> Đổi MK
                        </button>
                        <button onclick="window.deleteUser(${u.id}, '${u.email}')" 
                                class="px-3 py-1 text-xs bg-red-500 text-white rounded hover:bg-red-600">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </td>
                </tr>
            `).join('');

        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="px-4 py-8 text-center text-red-500">❌ ${err.message}</td></tr>`;
        }
    }

    window.toggleAdmin = async function(userId, currentRole) {
        if (!confirm(`Xác nhận ${currentRole === 'ADMIN' ? 'hủy' : 'set'} admin?`)) return;

        try {
            await api(`/api/admin/users/${userId}/toggle-admin`, "POST");
            toast("Cập nhật role thành công!", "success");
            loadUsers();
            loadStats();
        } catch (err) {
            toast(err.message, "error");
        }
    };

    window.editUser = function(userId) {
        // Tìm user data
        api(`/api/admin/users/${userId}`).then(user => {
            document.getElementById("edit-user-id").value = user.id;
            document.getElementById("edit-user-email").value = user.email;
            document.getElementById("edit-user-fullname").value = user.fullName || "";
            document.getElementById("edit-user-role").value = user.role;
            
            document.getElementById("edit-user-modal").classList.remove("hidden");
        }).catch(err => {
            toast("Không tải được thông tin user: " + err.message, "error");
        });
    };

    window.changePassword = function(userId, email) {
        document.getElementById("change-pwd-user-id").value = userId;
        document.getElementById("change-pwd-email-display").textContent = email;
        document.getElementById("change-pwd-new").value = "";
        document.getElementById("change-pwd-confirm").value = "";
        
        document.getElementById("change-password-modal").classList.remove("hidden");
    };

    window.deleteUser = async function(userId, email) {
        if (!confirm(`⚠️ XÓA VĨNH VIỄN user "${email}"?\n\nHành động này KHÔNG THỂ HOÀN TÁC!`)) return;

        try {
            await api(`/api/admin/users/${userId}`, "DELETE");
            toast("Đã xóa user thành công!", "success");
            loadUsers();
            loadStats();
        } catch (err) {
            toast("Lỗi xóa user: " + err.message, "error");
        }
    };

    window.createNewUser = function() {
        document.getElementById("create-user-form").reset();
        document.getElementById("create-user-modal").classList.remove("hidden");
    };

    function initDashboard() {
        checkAuth().then(ok => {
            if (!ok) return;

            loadStats();
            updateTime();
            setInterval(updateTime, 1000);
            initNav();

            document.getElementById("logout-btn")?.addEventListener("click", () => {
                if (confirm("Đăng xuất?")) {
                    clearAuth();
                    window.location.href = "/admin-login";
                }
            });

            // User management buttons
            document.getElementById("create-user-btn")?.addEventListener("click", createNewUser);
            document.getElementById("refresh-users-btn")?.addEventListener("click", loadUsers);

            // Create User Modal
            const createModal = document.getElementById("create-user-modal");
            document.getElementById("cancel-create-user")?.addEventListener("click", () => {
                createModal?.classList.add("hidden");
            });

            document.getElementById("create-user-form")?.addEventListener("submit", async (e) => {
                e.preventDefault();
                
                try {
                    const email = document.getElementById("create-user-email").value.trim();
                    const fullName = document.getElementById("create-user-fullname").value.trim();
                    const password = document.getElementById("create-user-password").value;
                    const role = document.getElementById("create-user-role").value;

                    await api("/api/admin/users", "POST", { email, fullName, password, role });
                    
                    toast("Tạo user thành công!", "success");
                    createModal?.classList.add("hidden");
                    loadUsers();
                    loadStats();
                    
                } catch (err) {
                    toast(err.message, "error");
                }
            });

            // Edit User Modal
            const editModal = document.getElementById("edit-user-modal");
            document.getElementById("cancel-edit-user")?.addEventListener("click", () => {
                editModal?.classList.add("hidden");
            });

            document.getElementById("edit-user-form")?.addEventListener("submit", async (e) => {
                e.preventDefault();
                
                try {
                    const userId = document.getElementById("edit-user-id").value;
                    const email = document.getElementById("edit-user-email").value.trim();
                    const fullName = document.getElementById("edit-user-fullname").value.trim();
                    const role = document.getElementById("edit-user-role").value;

                    await api(`/api/admin/users/${userId}`, "PUT", { email, fullName, role });
                    
                    toast("Cập nhật user thành công!", "success");
                    editModal?.classList.add("hidden");
                    loadUsers();
                    
                } catch (err) {
                    toast(err.message, "error");
                }
            });

            // Change Password Modal
            const pwdModal = document.getElementById("change-password-modal");
            document.getElementById("cancel-change-pwd")?.addEventListener("click", () => {
                pwdModal?.classList.add("hidden");
            });

            document.getElementById("change-password-form")?.addEventListener("submit", async (e) => {
                e.preventDefault();
                
                const newPwd = document.getElementById("change-pwd-new").value;
                const confirmPwd = document.getElementById("change-pwd-confirm").value;
                
                if (newPwd !== confirmPwd) {
                    toast("Password không khớp!", "error");
                    return;
                }
                
                if (newPwd.length < 6) {
                    toast("Password phải có ít nhất 6 ký tự!", "error");
                    return;
                }

                try {
                    const userId = document.getElementById("change-pwd-user-id").value;
                    await api(`/api/admin/users/${userId}/password`, "PUT", { newPassword: newPwd });
                    
                    toast("Đổi password thành công!", "success");
                    pwdModal?.classList.add("hidden");
                    
                } catch (err) {
                    toast(err.message, "error");
                }
            });
        });
    }

    function updateTime() {
        const el = document.getElementById("current-time");
        if (el) el.textContent = new Date().toLocaleTimeString("vi-VN");
    }

    function initNav() {
        const navLinks = document.querySelectorAll(".nav-link");
        const pages = document.querySelectorAll(".content-page");

        navLinks.forEach(link => {
            link.addEventListener("click", (e) => {
                e.preventDefault();
                const page = link.dataset.page;

                navLinks.forEach(l => l.classList.remove("active", "bg-slate-800"));
                link.classList.add("active", "bg-slate-800");

                pages.forEach(p => p.classList.add("hidden"));
                document.getElementById(`content-${page}`)?.classList.remove("hidden");

                const titles = {
                    dashboard: "Dashboard",
                    users: "Quản lý Users",
                    subscriptions: "Subscriptions",
                    placements: "Placement Tests",
                    roadmaps: "Roadmaps",
                    subjects: "Subjects"
                };
                document.getElementById("page-title").textContent = titles[page] || "Dashboard";

                if (page === "users") loadUsers();
            });
        });
    }

    // ========== INIT ==========

    document.addEventListener("DOMContentLoaded", () => {
        if (window.location.pathname.includes("admin-login")) {
            initLogin();
        } else if (window.location.pathname.includes("admin")) {
            initDashboard();
        }
    });

})();
