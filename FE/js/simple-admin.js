// Simple Admin for Question Bank
const API_BASE = window.APP_CONFIG?.API_BASE || 'http://localhost:8080';

// Get auth token
function getToken() {
    return localStorage.getItem('compassed_auth_token');
}

function getUser() {
    const user = localStorage.getItem('compassed_auth_user');
    return user ? JSON.parse(user) : null;
}

// Check auth
function checkAuth() {
    const token = getToken();
    const user = getUser();
    if (!token || !user || user.role !== 'ADMIN') {
        window.location.href = '/auth';
        return false;
    }
    return true;
}

// API call helper
async function apiCall(endpoint, method = 'GET', body = null) {
    const token = getToken();
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    };
    if (body) options.body = JSON.stringify(body);
    
    const response = await fetch(`${API_BASE}${endpoint}`, options);
    if (!response.ok) {
        const error = await response.text();
        throw new Error(error);
    }
    return response.json();
}

// Load subjects
async function loadSubjects() {
    try {
        const subjects = await fetch(`${API_BASE}/api/subjects`).then(r => r.json());
        const select = document.getElementById('subject-id');
        select.innerHTML = '<option value="">Chọn môn học...</option>';
        subjects.forEach(s => {
            select.innerHTML += `<option value="${s.id}">${s.name}</option>`;
        });
    } catch (error) {
        console.error('Load subjects error:', error);
    }
}

// Load questions
async function loadQuestions() {
    try {
        const questions = await apiCall('/api/admin/question-bank');
        const container = document.getElementById('questions-container');
        
        if (!questions || questions.length === 0) {
            container.innerHTML = '<div class="p-6 text-center text-gray-500">Chưa có câu hỏi nào</div>';
            return;
        }

        container.innerHTML = questions.map(q => `
            <div class="p-6 hover:bg-gray-50">
                <div class="flex justify-between items-start">
                    <div class="flex-1">
                        <div class="flex items-center gap-3 mb-2">
                            <span class="px-2 py-1 text-xs font-semibold rounded bg-blue-100 text-blue-800">
                                ${q.subjectName || 'N/A'}
                            </span>
                            <span class="px-2 py-1 text-xs rounded bg-gray-100 text-gray-600">
                                Level ${q.level}
                            </span>
                            <span class="px-2 py-1 text-xs rounded bg-purple-100 text-purple-600">
                                ${q.skillType || 'General'}
                            </span>
                        </div>
                        <p class="text-gray-900 font-medium mb-2">${q.questionText}</p>
                        <div class="space-y-1 text-sm">
                            ${JSON.parse(q.options || '[]').map((opt, i) => `
                                <div class="${opt === q.correctAnswer ? 'text-green-600 font-semibold' : 'text-gray-600'}">
                                    ${String.fromCharCode(65 + i)}. ${opt}
                                    ${opt === q.correctAnswer ? ' ✓' : ''}
                                </div>
                            `).join('')}
                        </div>
                    </div>
                    <div class="flex gap-2 ml-4">
                        <button onclick="editQuestion(${q.id})" class="px-3 py-1 text-sm bg-yellow-500 text-white rounded hover:bg-yellow-600">
                            Sửa
                        </button>
                        <button onclick="deleteQuestion(${q.id})" class="px-3 py-1 text-sm bg-red-500 text-white rounded hover:bg-red-600">
                            Xóa
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Load questions error:', error);
        alert('Không thể tải danh sách câu hỏi: ' + error.message);
    }
}

// Show modal
function showModal(title = 'Thêm câu hỏi mới') {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('question-modal').classList.remove('hidden');
}

// Hide modal
function hideModal() {
    document.getElementById('question-modal').classList.add('hidden');
    document.getElementById('question-form').reset();
    document.getElementById('question-id').value = '';
}

// Edit question
window.editQuestion = async function(id) {
    try {
        const question = await apiCall(`/api/admin/question-bank/${id}`);
        document.getElementById('question-id').value = question.id;
        document.getElementById('subject-id').value = question.subjectId;
        document.getElementById('question-text').value = question.questionText;
        
        const options = JSON.parse(question.options || '[]');
        for (let i = 0; i < 4; i++) {
            document.getElementById(`option-${i}`).value = options[i] || '';
        }
        
        document.getElementById('correct-answer').value = question.correctAnswer;
        document.getElementById('difficulty').value = question.difficulty;
        document.getElementById('level').value = question.level;
        document.getElementById('skill-type').value = question.skillType;
        
        showModal('Sửa câu hỏi');
    } catch (error) {
        alert('Không thể tải câu hỏi: ' + error.message);
    }
}

// Delete question
window.deleteQuestion = async function(id) {
    if (!confirm('Bạn có chắc muốn xóa câu hỏi này?')) return;
    
    try {
        await apiCall(`/api/admin/question-bank/${id}`, 'DELETE');
        alert('Đã xóa câu hỏi');
        loadQuestions();
    } catch (error) {
        alert('Không thể xóa: ' + error.message);
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (!checkAuth()) return;
    
    loadSubjects();
    loadQuestions();
    
    // Add question button
    document.getElementById('add-question-btn').addEventListener('click', () => {
        showModal('Thêm câu hỏi mới');
    });
    
    // Cancel button
    document.getElementById('cancel-btn').addEventListener('click', hideModal);
    
    // Logout
    document.getElementById('logout-btn').addEventListener('click', () => {
        localStorage.removeItem('compassed_auth_token');
        localStorage.removeItem('compassed_auth_user');
        window.location.href = '/auth';
    });
    
    // Form submit
    document.getElementById('question-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const id = document.getElementById('question-id').value;
        const options = [
            document.getElementById('option-0').value,
            document.getElementById('option-1').value,
            document.getElementById('option-2').value,
            document.getElementById('option-3').value
        ];
        
        const data = {
            subjectId: parseInt(document.getElementById('subject-id').value),
            questionText: document.getElementById('question-text').value,
            options: JSON.stringify(options),
            correctAnswer: document.getElementById('correct-answer').value,
            difficulty: parseInt(document.getElementById('difficulty').value),
            level: document.getElementById('level').value,
            skillType: document.getElementById('skill-type').value,
            questionType: 'MULTIPLE_CHOICE',
            isActive: true
        };
        
        try {
            if (id) {
                await apiCall(`/api/admin/question-bank/${id}`, 'PUT', data);
                alert('Đã cập nhật câu hỏi');
            } else {
                await apiCall('/api/admin/question-bank', 'POST', data);
                alert('Đã thêm câu hỏi mới');
            }
            hideModal();
            loadQuestions();
        } catch (error) {
            alert('Lỗi: ' + error.message);
        }
    });
});
