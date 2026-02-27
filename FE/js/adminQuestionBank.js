// Admin Question Bank Management JavaScript
(function() {
    const API_BASE = (window.APP_CONFIG && window.APP_CONFIG.API_BASE) || "http://localhost:8080";
    const TOKEN_KEY = "compassed_admin_token";
    const USER_KEY = "compassed_admin_user";
    
    let currentPage = 0;
    const pageSize = 20;
    let totalPages = 0;
    let totalItems = 0;
    let currentFilters = {
        subjectId: null,
        level: null,
        skillType: null
    };

    // ========== AUTH HELPERS ==========
    
    function getAuth() {
        const token = localStorage.getItem(TOKEN_KEY);
        const user = JSON.parse(localStorage.getItem(USER_KEY) || "null");
        return { token, user };
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

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || `HTTP ${res.status}`);
        }
        return res.json();
    }

    function toast(msg, type = "info") {
        const container = document.getElementById("toast-container");
        if (!container) return;

        const div = document.createElement("div");
        div.className = `px-6 py-3 rounded-lg shadow-lg text-white transform transition-all duration-300 ${
            type === "error" ? "bg-red-600" : 
            type === "success" ? "bg-green-600" : 
            type === "warning" ? "bg-yellow-600" :
            "bg-blue-600"
        }`;
        div.textContent = msg;
        container.appendChild(div);
        
        setTimeout(() => {
            div.style.opacity = "0";
            setTimeout(() => div.remove(), 300);
        }, 3000);
    }

    function loading(show) {
        const el = document.getElementById("loading-overlay");
        if (el) el.style.display = show ? "flex" : "none";
    }

    // Update time display
    function updateTime() {
        const el = document.getElementById("current-time");
        if (el) {
            el.textContent = new Date().toLocaleTimeString("vi-VN");
        }
    }

    // ========== LOAD DATA ==========
    
    async function loadStats() {
        try {
            const stats = await api("/api/admin/questions/stats");
            document.getElementById("stat-total").textContent = stats.totalQuestions || 0;
            document.getElementById("stat-l1").textContent = stats.byLevel?.L1 || 0;
            document.getElementById("stat-l2").textContent = stats.byLevel?.L2 || 0;
            document.getElementById("stat-l3").textContent = stats.byLevel?.L3 || 0;
        } catch (err) {
            console.error("Load stats error:", err);
            toast("Không tải được thống kê", "error");
        }
    }

    async function loadSubjects() {
        try {
            const subjects = await api("/api/subjects");
            
            const filterSelect = document.getElementById("filter-subject");
            const inputSelect = document.getElementById("input-subject");
            
            subjects.forEach(subject => {
                const option = new Option(subject.name, subject.id);
                filterSelect.appendChild(option.cloneNode(true));
                inputSelect.appendChild(option);
            });
        } catch (err) {
            console.error("Load subjects error:", err);
        }
    }

    async function loadQuestions() {
        try {
            loading(true);
            
            let url = `/api/admin/questions?page=${currentPage}&size=${pageSize}&sortBy=id&sortDir=DESC`;
            
            if (currentFilters.subjectId) url += `&subjectId=${currentFilters.subjectId}`;
            if (currentFilters.level) url += `&level=${currentFilters.level}`;
            if (currentFilters.skillType) url += `&skillType=${encodeURIComponent(currentFilters.skillType)}`;
            
            const data = await api(url);
            
            totalPages = data.totalPages || 0;
            totalItems = data.totalItems || 0;
            
            renderQuestions(data.questions || []);
            updatePagination();
            
        } catch (err) {
            console.error("Load questions error:", err);
            toast("Không tải được danh sách câu hỏi: " + err.message, "error");
        } finally {
            loading(false);
        }
    }

    function renderQuestions(questions) {
        const tbody = document.getElementById("questions-tbody");
        
        if (!questions || questions.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" class="px-6 py-12 text-center text-slate-500">
                        Không tìm thấy câu hỏi nào
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = questions.map(q => {
            const levelColor = {
                L1: "bg-green-100 text-green-700",
                L2: "bg-yellow-100 text-yellow-700",
                L3: "bg-red-100 text-red-700"
            }[q.level] || "bg-slate-100 text-slate-700";
            
            const truncatedQuestion = q.questionText.length > 50 
                ? q.questionText.substring(0, 50) + "..." 
                : q.questionText;
            
            return `
                <tr class="hover:bg-slate-50 transition">
                    <td class="px-6 py-4 text-sm font-medium text-slate-900">#${q.id}</td>
                    <td class="px-6 py-4 text-sm text-slate-600">${q.subjectName}</td>
                    <td class="px-6 py-4">
                        <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full ${levelColor}">
                            ${q.level}
                        </span>
                    </td>
                    <td class="px-6 py-4 text-sm text-slate-600">${q.skillType}</td>
                    <td class="px-6 py-4 text-sm text-slate-700" title="${q.questionText}">${truncatedQuestion}</td>
                    <td class="px-6 py-4 text-sm">
                        <span class="inline-flex items-center">
                            ${"⭐".repeat(q.difficulty)}
                        </span>
                    </td>
                    <td class="px-6 py-4">
                        <span class="inline-flex px-2 py-1 text-xs font-semibold rounded-full ${q.isActive ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'}">
                            ${q.isActive ? 'Active' : 'Inactive'}
                        </span>
                    </td>
                    <td class="px-6 py-4 text-right space-x-2">
                        <button onclick="window.viewQuestion(${q.id})" class="text-blue-600 hover:text-blue-800 font-medium text-sm">
                            👁️ Xem
                        </button>
                        <button onclick="window.editQuestion(${q.id})" class="text-green-600 hover:text-green-800 font-medium text-sm">
                            ✏️ Sửa
                        </button>
                        <button onclick="window.deleteQuestion(${q.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">
                            🗑️ Xóa
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }

    function updatePagination() {
        const from = totalItems > 0 ? currentPage * pageSize + 1 : 0;
        const to = Math.min((currentPage + 1) * pageSize, totalItems);
        
        document.getElementById("showing-from").textContent = from;
        document.getElementById("showing-to").textContent = to;
        document.getElementById("showing-total").textContent = totalItems;
        
        document.getElementById("btn-prev").disabled = currentPage === 0;
        document.getElementById("btn-next").disabled = currentPage >= totalPages - 1;
    }

    // ========== MODAL ==========
    
    window.openCreateModal = function() {
        document.getElementById("modal-title").textContent = "Thêm câu hỏi mới";
        document.getElementById("question-form").reset();
        document.getElementById("question-id").value = "";
        document.getElementById("question-modal").classList.remove("hidden");
    };

    window.closeModal = function() {
        document.getElementById("question-modal").classList.add("hidden");
    };

    window.viewQuestion = async function(id) {
        try {
            loading(true);
            const question = await api(`/api/admin/questions/${id}`);
            
            let optionsHtml = "";
            try {
                const options = JSON.parse(question.options);
                optionsHtml = options.map(opt => `<li class="ml-4">${opt}</li>`).join("");
            } catch {
                optionsHtml = `<li class="ml-4">${question.options}</li>`;
            }
            
            alert(`
📚 Câu hỏi #${question.id}

Môn học: ${question.subjectName}
Level: ${question.level}
Kỹ năng: ${question.skillType}
Loại: ${question.questionType}

Câu hỏi:
${question.questionText}

Các đáp án:
${optionsHtml.replace(/<li class="ml-4">/g, "\n- ").replace(/<\/li>/g, "")}

✅ Đáp án đúng: ${question.correctAnswer}

💡 Giải thích:
${question.explanation || "Không có"}

⭐ Độ khó: ${question.difficulty}/5
🔘 Trạng thái: ${question.isActive ? "Active" : "Inactive"}
            `.trim());
            
        } catch (err) {
            toast("Không tải được chi tiết câu hỏi: " + err.message, "error");
        } finally {
            loading(false);
        }
    };

    window.editQuestion = async function(id) {
        try {
            loading(true);
            const question = await api(`/api/admin/questions/${id}`);
            
            document.getElementById("modal-title").textContent = "Chỉnh sửa câu hỏi #" + id;
            document.getElementById("question-id").value = question.id;
            document.getElementById("input-subject").value = question.subjectId;
            document.getElementById("input-level").value = question.level;
            document.getElementById("input-type").value = question.questionType;
            document.getElementById("input-skill").value = question.skillType;
            document.getElementById("input-question").value = question.questionText;
            document.getElementById("input-options").value = question.options;
            document.getElementById("input-answer").value = question.correctAnswer;
            document.getElementById("input-difficulty").value = question.difficulty;
            document.getElementById("input-explanation").value = question.explanation || "";
            
            document.getElementById("question-modal").classList.remove("hidden");
            
        } catch (err) {
            toast("Không tải được câu hỏi: " + err.message, "error");
        } finally {
            loading(false);
        }
    };

    window.deleteQuestion = async function(id) {
        if (!confirm("Bạn có chắc muốn xóa câu hỏi #" + id + "?\n\n(Soft delete - câu hỏi sẽ bị ẩn nhưng không bị xóa vĩnh viễn)")) {
            return;
        }
        
        try {
            loading(true);
            await api(`/api/admin/questions/${id}`, "DELETE");
            toast("Đã xóa câu hỏi thành công!", "success");
            loadQuestions();
            loadStats();
        } catch (err) {
            toast("Không thể xóa câu hỏi: " + err.message, "error");
        } finally {
            loading(false);
        }
    };

    window.saveQuestion = async function() {
        try {
            // Validate options JSON
            const optionsText = document.getElementById("input-options").value;
            try {
                JSON.parse(optionsText);
            } catch {
                toast("Định dạng JSON của đáp án không hợp lệ!", "error");
                return;
            }
            
            loading(true);
            
            const questionData = {
                subjectId: parseInt(document.getElementById("input-subject").value),
                level: document.getElementById("input-level").value,
                questionType: document.getElementById("input-type").value,
                skillType: document.getElementById("input-skill").value,
                questionText: document.getElementById("input-question").value,
                options: optionsText,
                correctAnswer: document.getElementById("input-answer").value,
                difficulty: parseInt(document.getElementById("input-difficulty").value),
                explanation: document.getElementById("input-explanation").value || null
            };
            
            const questionId = document.getElementById("question-id").value;
            
            if (questionId) {
                // Update
                await api(`/api/admin/questions/${questionId}`, "PUT", questionData);
                toast("Đã cập nhật câu hỏi thành công!", "success");
            } else {
                // Create
                await api("/api/admin/questions", "POST", questionData);
                toast("Đã thêm câu hỏi mới thành công!", "success");
            }
            
            closeModal();
            loadQuestions();
            loadStats();
            
        } catch (err) {
            toast("Lỗi khi lưu câu hỏi: " + err.message, "error");
        } finally {
            loading(false);
        }
    };

    // ========== FILTERS & PAGINATION ==========
    
    window.applyFilters = function() {
        currentFilters.subjectId = document.getElementById("filter-subject").value || null;
        currentFilters.level = document.getElementById("filter-level").value || null;
        currentFilters.skillType = document.getElementById("filter-skill").value || null;
        
        currentPage = 0;
        loadQuestions();
    };

    window.prevPage = function() {
        if (currentPage > 0) {
            currentPage--;
            loadQuestions();
        }
    };

    window.nextPage = function() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadQuestions();
        }
    };

    window.logout = function() {
        if (confirm("Bạn có chắc muốn đăng xuất?")) {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
            window.location.href = "/admin-login";
        }
    };

    // ========== INIT ==========
    
    async function init() {
        const { user } = getAuth();
        if (!user) {
            window.location.href = "/admin-login";
            return;
        }
        
        if (user.role !== "ADMIN") {
            alert("Bạn không có quyền truy cập trang này!");
            window.location.href = "/";
            return;
        }
        
        // Update admin info
        document.getElementById("admin-name").textContent = user.fullName || "Admin";
        document.getElementById("admin-email").textContent = user.email;
        document.getElementById("admin-initial").textContent = (user.fullName || "A")[0].toUpperCase();
        
        // Update time
        updateTime();
        setInterval(updateTime, 1000);
        
        // Load data
        await loadSubjects();
        await loadStats();
        await loadQuestions();
        
        // Setup filter change listeners
        document.getElementById("filter-subject").addEventListener("change", async function() {
            if (this.value) {
                const level = document.getElementById("filter-level").value || "L1";
                try {
                    const skills = await api(`/api/admin/questions/skill-types?subjectId=${this.value}&level=${level}`);
                    const skillSelect = document.getElementById("filter-skill");
                    skillSelect.innerHTML = '<option value="">Tất cả kỹ năng</option>';
                    skills.forEach(skill => {
                        skillSelect.add(new Option(skill, skill));
                    });
                } catch (err) {
                    console.error("Load skills error:", err);
                }
            }
        });
    }

    document.addEventListener("DOMContentLoaded", init);

})();
