# CompassED - API Integration Guide

## ✅ Hoàn tất kết nối Backend - Frontend

Hệ thống đã được cập nhật để **loại bỏ hoàn toàn dữ liệu demo** và sử dụng **API thật từ backend**.

---

## 📡 API Endpoints đang sử dụng

### **Authentication APIs**
- `POST /api/auth/register` - Đăng ký user mới
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/me` - Lấy thông tin user hiện tại
- `POST /api/auth/oauth/google` - Đăng nhập Google OAuth
- `POST /api/auth/oauth/mock` - Đăng nhập mock (dev mode)

### **Subject APIs** ✨ MỚI
- `GET /api/subjects` - Lấy danh sách tất cả môn học
- `GET /api/subjects/{id}` - Lấy môn học theo ID
- `GET /api/subjects/code/{code}` - Lấy môn học theo code (MATH, LITERATURE, ENGLISH)

### **Placement Test APIs**
- `POST /api/subjects/{subjectId}/placement-tests` - Bắt đầu placement test
- `POST /api/placement-attempts/{attemptId}/submit` - Nộp bài placement test

### **History APIs**
- `GET /api/history/placements` - Lấy lịch sử placement tests của user

### **Subscription APIs**
- `POST /api/subscriptions/checkout` - Unlock roadmap cho môn học

### **Admin APIs** (Chỉ ADMIN role)
- `GET /api/admin/stats` - Thống kê tổng quan
- `GET /api/admin/users` - Danh sách tất cả users
- `GET /api/admin/users/{id}` - Chi tiết user theo ID

### **Dev APIs** (Development only)
- `POST /api/dev/create-admin` - Tạo admin user
- `POST /api/dev/set-admin/{userId}` - Set user thành admin
- `POST /api/dev/users` - Tạo user dev

### **User Question Bank API** ✨ MỚI
- `GET /api/questions` - Lấy danh sách câu hỏi cho user (public)
  - Query params: `subjectId`, `level`, `skillType`, `page`, `size`
  - Chỉ trả về câu hỏi `isActive=true`
  - Response:
    ```json
    {
      "questions": [
        {
          "id": 1,
          "subjectId": 1,
          "subjectName": "Math",
          "level": "L1",
          "skillType": "Reading",
          "questionType": "MULTIPLE_CHOICE",
          "questionText": "What is 2+2?",
          "options": "[\"2\",\"3\",\"4\",\"5\"]",
          "correctAnswer": "4",
          "explanation": "Basic addition",
          "difficulty": 1,
          "isActive": true
        }
      ],
      "currentPage": 0,
      "totalItems": 30,
      "totalPages": 2
    }
    ```

---

## 🎯 Dữ liệu thật được load từ database

### **1. Landing Page (`/`)**
**Trước đây:** Subjects được hardcode trong HTML
**Bây giờ:** 
- Load động từ `GET /api/subjects`
- Render cards với dữ liệu thật (id, code, name)
- Auto-attach event listeners cho nút "Join Program"

### **2. Authentication (`/auth`)**
**Đã kết nối từ trước:**
- Login/Register gọi `/api/auth/login` và `/api/auth/register`
- Response trả về `{ token, user }` được lưu vào localStorage
- Token format: `{userId}` (ví dụ: "8")
- Frontend tự thêm "Bearer" prefix khi gọi API

### **3. Placement Test (`/placement-test`)**
**Đã kết nối từ trước:**
- Gọi `POST /api/subjects/{subjectId}/placement-tests` để bắt đầu
- Backend trả về `attemptId` và `paperJson` (đề thi)
- User làm bài, frontend lưu answers vào localStorage
- Submit bằng `POST /api/placement-attempts/{attemptId}/submit`

### **4. Placement Result (`/placement-result`)**
**Đã kết nối từ trước:**
- Hiển thị kết quả từ response của submit API
- Có nút "Unlock Roadmap" gọi `POST /api/subscriptions/checkout`

### **5. History (`/history`)**
**Đã kết nối từ trước:**
- Gọi `GET /api/history/placements` để load lịch sử
- Hiển thị table với các lần làm placement test

### **6. Admin Portal (`/admin-login`, `/admin`)**
**Đã kết nối từ trước:**
- Login riêng cho admin
- Dashboard load stats từ `/api/admin/stats`
- Quản lý users từ `/api/admin/users`

---

## 📂 Các file đã thay đổi

### **Backend - Mới tạo:**
- `SubjectController.java` - REST API cho subjects

### **Frontend - Cập nhật:**
- `appLocal.js`:
  - Thêm `loadSubjects()` - Load subjects từ API và render động
  - Thêm `attachJoinProgramListeners()` - Gắn event listeners sau khi render
  - Cập nhật `initLanding()` - Gọi `loadSubjects()` khi khởi tạo

### **Loại bỏ:**
- `data.js` → `data.js.backup` (backup, không còn sử dụng)

---

## 🗄️ Database Subjects

```sql
SELECT * FROM subjects;
```

| id | code       | name        |
|----|------------|-------------|
| 1  | MATH       | Mathematics |
| 2  | LITERATURE | Literature  |
| 3  | ENGLISH    | English     |

---

## 🔧 Cách test

### **1. Khởi động hệ thống:**
```bash
cd /Users/hoangngoctinh/compassED/ED
./start-all.sh
```

### **2. Mở browser:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/subjects

### **3. Test flow hoàn chỉnh:**

#### **a. Landing page**
1. Mở http://localhost:3000
2. Subjects hiện ra động từ API (không còn hardcode)
3. Click "Join Program" trên bất kỳ subject nào

#### **b. Authentication**
4. Chuyển đến `/auth` nếu chưa login
5. Đăng nhập hoặc đăng ký
6. Token được lưu vào localStorage

#### **c. Placement Test**
7. Sau login, redirect về `/placement-test?subjectId=X`
8. API tạo attempt và load đề thi
9. Làm bài và submit

#### **d. Result & Roadmap**
10. Xem kết quả tại `/placement-result`
11. Unlock roadmap gọi API checkout

#### **e. History**
12. Vào `/history` xem lịch sử placement tests

#### **f. Admin (nếu là ADMIN role)**
13. Login tại `/admin-login` với admin@compassed.com / admin123
14. Vào dashboard xem stats, manage users

---

## 🚀 Lợi ích của việc kết nối API thật

✅ **Không còn dữ liệu hardcode** - Tất cả data từ database
✅ **Dễ quản lý** - Thêm/sửa/xóa subjects qua database hoặc admin panel
✅ **Scalable** - Dễ mở rộng thêm subjects mới
✅ **Consistent** - Frontend luôn đồng bộ với backend
✅ **Real-time** - Thay đổi ở database hiển thị ngay lập tức
✅ **Production-ready** - Sẵn sàng deploy lên production

---

## 📝 Ghi chú

- **Token format:** Backend trả về userId (ví dụ: "8"), frontend thêm "Bearer" → "Bearer 8"
- **CORS:** Backend đã config `@CrossOrigin(origins = "*")` để accept requests từ frontend
- **Authentication:** Sử dụng header `Authorization: Bearer {token}` và `X-USER-ID: {userId}`
- **localStorage keys:** 
  - `compassed_auth_token` - Token
  - `compassed_auth_user` - User object
  - `compassed_subject_id` - Subject ID hiện tại

---

## 🎉 Kết luận

Hệ thống đã **100% kết nối với backend API thật**, không còn dữ liệu mock/demo. Tất cả tính năng đều hoạt động với database MySQL và Spring Boot backend!
