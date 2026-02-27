# 📚 Hướng dẫn sử dụng Admin Question Bank

## 🎯 Truy cập trang Admin

### Cách 1: Từ Admin Dashboard
1. Mở: `http://localhost:3000/template/admin/adminDashboard.html`
2. Click vào **"Content Bank"** ở sidebar bên trái
3. Sẽ tự động chuyển đến trang Question Bank

### Cách 2: Truy cập trực tiếp
Mở URL: `http://localhost:3000/template/admin/adminQuestionBank.html`

---

## ✨ Tính năng đã có

### 1️⃣ **Thống kê tổng quan**
- 📊 Tổng số câu hỏi
- 📈 Số câu hỏi theo Level (L1, L2, L3)
- 📚 Phân bố theo môn học

### 2️⃣ **Danh sách câu hỏi**
- ✅ Hiển thị bảng với pagination (20 câu/trang)
- ✅ Thông tin: ID, Môn học, Level, Kỹ năng, Câu hỏi, Độ khó, Trạng thái
- ✅ Actions: Xem chi tiết, Sửa, Xóa

### 3️⃣ **Tìm kiếm & Lọc**
- 🔍 Lọc theo môn học (Math, Literature, English)
- 🔍 Lọc theo Level (L1, L2, L3)
- 🔍 Lọc theo kỹ năng (tự động load theo môn + level)
- 🔄 Reset filters

### 4️⃣ **Thêm câu hỏi mới**
- ➕ Nút "Thêm câu hỏi mới"
- 📝 Form với đầy đủ trường:
  - Môn học (dropdown)
  - Level (L1/L2/L3)
  - Loại câu hỏi (MULTIPLE_CHOICE, TRUE_FALSE, etc.)
  - Kỹ năng (text input)
  - Nội dung câu hỏi
  - Các đáp án (JSON format)
  - Đáp án đúng
  - Độ khó (1-5 sao)
  - Giải thích
- ✅ Validation JSON cho options
- ✅ Toast notification khi thành công/lỗi

### 5️⃣ **Chỉnh sửa câu hỏi**
- ✏️ Click "Sửa" để mở form edit
- 📝 Pre-fill dữ liệu hiện tại
- 💾 Lưu thay đổi

### 6️⃣ **Xóa câu hỏi**
- 🗑️ Soft delete (ẩn câu hỏi, không xóa vĩnh viễn)
- ⚠️ Confirm dialog trước khi xóa
- 🔄 Refresh danh sách sau khi xóa

---

## 🔌 Backend APIs đã kết nối

### 1. GET Statistics
```bash
GET http://localhost:8080/api/admin/questions/stats
```
**Response:**
```json
{
  "totalQuestions": 30,
  "byLevel": {"L1": 24, "L2": 3, "L3": 3},
  "bySubject": [
    {"subjectName": "Toán", "count": 18},
    {"subjectName": "Văn", "count": 6},
    {"subjectName": "Anh", "count": 6}
  ],
  "activeQuestions": 30
}
```

### 2. GET Question List (with filters & pagination)
```bash
GET http://localhost:8080/api/admin/questions?page=0&size=20&sortBy=id&sortDir=DESC
GET http://localhost:8080/api/admin/questions?subjectId=1&level=L1
```

### 3. GET Question by ID
```bash
GET http://localhost:8080/api/admin/questions/31
```

### 4. POST Create Question
```bash
POST http://localhost:8080/api/admin/questions
Content-Type: application/json

{
  "subjectId": 1,
  "level": "L1",
  "questionType": "MULTIPLE_CHOICE",
  "skillType": "Đại số",
  "questionText": "Tính: 99 + 1 = ?",
  "options": "[\"A. 98\", \"B. 99\", \"C. 100\", \"D. 101\"]",
  "correctAnswer": "C",
  "difficulty": 1,
  "explanation": "99 + 1 = 100"
}
```

### 5. PUT Update Question
```bash
PUT http://localhost:8080/api/admin/questions/31
```

### 6. DELETE Soft Delete
```bash
DELETE http://localhost:8080/api/admin/questions/31
```

### 7. GET Skill Types (for filter dropdown)
```bash
GET http://localhost:8080/api/admin/questions/skill-types?subjectId=1&level=L1
```

### 8. GET Subjects
```bash
GET http://localhost:8080/api/subjects
```

---

## 📋 Dữ liệu hiện có trong Database

### Question Bank (30 câu)

**Toán (18 câu):**
- Level L1: 12 câu (Đại số, Hình học)
- Level L2: 3 câu (Đại số, Hình học)
- Level L3: 3 câu (Đại số, Hình học)

**Văn (6 câu):**
- Level L1: 6 câu (Đọc hiểu, Tả cảnh)

**Anh (6 câu):**
- Level L1: 6 câu (Vocabulary, Grammar)

---

## 🎨 UI/UX Features

### Design
- ✅ Tailwind CSS modern design
- ✅ Responsive layout
- ✅ Dark sidebar navigation
- ✅ Card-based stats display
- ✅ Clean table with hover effects
- ✅ Modal forms with backdrop blur

### User Experience
- ✅ Loading overlay khi gọi API
- ✅ Toast notifications (success/error/info)
- ✅ Confirm dialog trước khi xóa
- ✅ Truncate long text với tooltip
- ✅ Badge colors theo level/status
- ✅ Star rating cho độ khó
- ✅ Pagination controls
- ✅ Real-time filter

---

## 🧪 Test Cases đã verify

### ✅ Test 1: Load statistics
- Gọi `/api/admin/questions/stats`
- Hiển thị 4 stat cards
- Số liệu chính xác: 30 total, 24 L1, 3 L2, 3 L3

### ✅ Test 2: Load question list
- Pagination 20 items/page
- Sort by ID DESC
- Hiển thị đầy đủ thông tin

### ✅ Test 3: Filter by subject
- Chọn "Toán" → 18 câu hỏi
- Chọn "Văn" → 6 câu hỏi
- Chọn "Anh" → 6 câu hỏi

### ✅ Test 4: Filter by level
- Filter L1 → 24 câu
- Filter L2 → 3 câu
- Filter L3 → 3 câu

### ✅ Test 5: Load skill types
- Chọn "Toán" + "L1" → ["Đại số", "Hình học"]
- Dropdown tự động update

### ✅ Test 6: Create question
- Fill form với dữ liệu hợp lệ
- POST request thành công
- Toast "Đã thêm câu hỏi mới thành công!"
- Table refresh với câu hỏi mới

### ✅ Test 7: Edit question
- Click "Sửa" → Modal mở với data pre-filled
- Sửa nội dung → PUT request
- Toast "Đã cập nhật câu hỏi thành công!"

### ✅ Test 8: Delete question
- Click "Xóa" → Confirm dialog
- DELETE request (soft delete)
- Toast "Đã xóa câu hỏi thành công!"
- Question disappears từ active list

---

## 🔧 Troubleshooting

### Issue 1: Không load được danh sách câu hỏi
**Nguyên nhân:** Backend chưa chạy hoặc port 8080 bị chiếm
**Giải pháp:**
```bash
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run
```

### Issue 2: CORS Error
**Nguyên nhân:** Backend chưa config CORS cho localhost:3000
**Giải pháp:** Backend đã có `@CrossOrigin` trong controller

### Issue 3: 401 Unauthorized
**Nguyên nhân:** Chưa đăng nhập admin
**Giải pháp:** 
- Tạm thời: Comment auth check trong JavaScript
- Hoặc: Đăng nhập qua `/admin-login` trước

### Issue 4: JSON format error khi tạo câu hỏi
**Ví dụ sai:**
```
A. Đáp án 1, B. Đáp án 2
```

**Ví dụ đúng:**
```json
["A. Đáp án 1", "B. Đáp án 2", "C. Đáp án 3", "D. Đáp án 4"]
```

---

## 🚀 Next Steps (Tính năng tiếp theo)

### 1. Excel Import/Export
- [ ] Upload file Excel với nhiều câu hỏi
- [ ] Parse Excel → validate → batch insert
- [ ] Download Excel template
- [ ] Export danh sách câu hỏi ra Excel

### 2. Authentication & Authorization
- [ ] JWT token authentication
- [ ] Role-based access (chỉ ADMIN mới vào)
- [ ] Session management
- [ ] Logout functionality

### 3. Advanced Filters
- [ ] Search by question text
- [ ] Filter by difficulty range
- [ ] Filter by date created
- [ ] Multi-select filters

### 4. Bulk Operations
- [ ] Select multiple questions
- [ ] Bulk delete
- [ ] Bulk update (change level, subject)
- [ ] Bulk activate/deactivate

### 5. Question Preview
- [ ] Render question như học sinh sẽ thấy
- [ ] Interactive preview (click answer)
- [ ] Show explanation on hover

### 6. Analytics
- [ ] Most used questions
- [ ] Question difficulty distribution
- [ ] Success rate per question
- [ ] Admin activity logs

---

## 📖 Code Structure

```
FE/
├── config.js                        # API config
├── js/
│   └── adminQuestionBank.js         # Question Bank logic (600+ lines)
├── template/
│   └── admin/
│       ├── adminDashboard.html      # Main admin dashboard
│       └── adminQuestionBank.html   # Question Bank UI
```

**adminQuestionBank.js functions:**
- `api()` - Fetch wrapper with auth headers
- `toast()` - Show notifications
- `loading()` - Show/hide loading overlay
- `loadStats()` - Load statistics cards
- `loadSubjects()` - Load subject dropdowns
- `loadQuestions()` - Load question list with filters
- `renderQuestions()` - Render table rows
- `updatePagination()` - Update page controls
- `openCreateModal()` - Open create form
- `closeModal()` - Close modal
- `viewQuestion()` - Show detail alert
- `editQuestion()` - Open edit form
- `deleteQuestion()` - Soft delete
- `saveQuestion()` - Create/update handler
- `applyFilters()` - Apply filter changes
- `prevPage() / nextPage()` - Pagination
- `init()` - Initialize on page load

---

## ✅ Checklist hoàn thành

- [x] Backend APIs (8 endpoints)
- [x] MySQL database với 30 câu hỏi mẫu
- [x] QuestionBank entity + repository
- [x] QuestionBankService với filters
- [x] AdminQuestionBankController
- [x] Frontend HTML UI
- [x] Frontend JavaScript logic
- [x] API integration
- [x] Toast notifications
- [x] Loading states
- [x] Form validation
- [x] Pagination
- [x] Filters (subject, level, skill)
- [x] CRUD operations (Create, Read, Update, Delete)
- [x] Statistics dashboard
- [x] Responsive design
- [x] Error handling
- [x] Navigation từ admin dashboard
- [x] Documentation

---

## 🎉 Kết luận

Hệ thống Admin Question Bank đã **HOÀN CHỈNH** và **SẴN SÀNG SỬ DỤNG**!

- ✅ Backend: 8 REST APIs working với MySQL real data
- ✅ Frontend: Full-featured admin UI với CRUD
- ✅ Integration: Frontend-Backend hoàn toàn kết nối
- ✅ Data: 30 câu hỏi mẫu đã có trong database
- ✅ Testing: All endpoints tested và verified

**Bắt đầu sử dụng ngay:** `http://localhost:3000/template/admin/adminQuestionBank.html`
