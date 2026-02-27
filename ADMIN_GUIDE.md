# HƯỚNG DẪN PHÂN QUYỀN VÀ ADMIN DASHBOARD

## 📋 MỤC LỤC

1. [Phân quyền trong hệ thống](#phân-quyền-trong-hệ-thống)
2. [Trang Admin Dashboard](#trang-admin-dashboard)
3. [Cách set role ADMIN](#cách-set-role-admin)
4. [APIs yêu cầu ADMIN](#apis-yêu-cầu-admin)
5. [Test phân quyền](#test-phân-quyền)

---

## 🔐 PHÂN QUYỀN TRONG HỆ THỐNG

### Các Role có sẵn

Hệ thống có **2 roles**:
- `USER` - Người dùng thông thường
- `ADMIN` - Quản trị viên

### User hiện có trong hệ thống

```
+----+-----------------------------+-----------------+-------+
| id | email                       | full_name       | role  |
+----+-----------------------------+-----------------+-------+
|  4 | hoangtinh12122003@gmail.com | Hoang Ngoc Tinh | USER  |
|  5 | h12122003@gmail.com         | Ngoc Tinh       | USER  |
|  6 | 8@gmail.com                 | hoang ngoc tinh | USER  |
|  9 | 1@gmail.com                 | hoang ngoc tinh | USER  |
| 10 | admin@compassed.com         | Admin User      | ADMIN | ✅
+----+-----------------------------+-----------------+-------+
```

**✅ User ADMIN sẵn có:**
- Email: `admin@compassed.com`
- Password: *(Cần kiểm tra hoặc reset)*

### Cấu hình phân quyền trong Backend

File: `BE/compassed-api/src/main/java/com/compassed/compassed_api/config/SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints - không cần đăng nhập
    .requestMatchers("/api/auth/**", "/api/subjects", "/api/pricing/plans")
        .permitAll()
    
    // Admin endpoints - cần role ADMIN
    .requestMatchers("/api/admin/**")
        .hasRole("ADMIN")
    
    // Các endpoints khác - cần đăng nhập
    .anyRequest()
        .authenticated()
)
```

**Giải thích:**
- `/api/auth/**` - Public (đăng ký, đăng nhập)
- `/api/admin/**` - Chỉ ADMIN
- Còn lại - Cần đăng nhập (USER hoặc ADMIN)

---

## 🎯 TRANG ADMIN DASHBOARD

### URL Trang Admin

```
http://localhost:3000/admin-dashboard
```

### Route trong Flask

File: `FE/Extensions.py`

```python
@app.route("/admin-dashboard")
def admin_dashboard():
    return render_template("admin/adminDashboard.html")
```

### File HTML

```
FE/template/admin/adminDashboard.html
```

### Tính năng trong Admin Dashboard

1. **User Management** - Quản lý người dùng
   - Xem danh sách users
   - Thay đổi role
   - Xóa users

2. **Question Bank** - Quản lý ngân hàng câu hỏi
   - Xem danh sách câu hỏi
   - Thêm câu hỏi mới
   - Sửa câu hỏi
   - Xóa câu hỏi

3. **Subject Management** - Quản lý môn học
4. **Roadmap Management** - Quản lý lộ trình học
5. **Content Management** - Quản lý nội dung

---

## 🔧 CÁCH SET ROLE ADMIN

### Cách 1: Qua MySQL (Khuyến nghị - Nhanh nhất)

```bash
# Kiểm tra users hiện có
mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users;"

# Set role ADMIN cho user ID cụ thể (ví dụ: user ID 4)
mysql -u root -proot compassed_db -e "UPDATE users SET role = 'ADMIN' WHERE id = 4;"

# Kiểm tra lại
mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users WHERE id = 4;"
```

**Hoặc dùng script có sẵn:**

```bash
bash set-admin-role.sh <userId>

# Ví dụ:
bash set-admin-role.sh 4
```

### Cách 2: Qua API (Cần có ADMIN user khác)

Endpoint: `PUT /api/admin/users/{userId}/role`

```bash
# Lấy token của ADMIN user
TOKEN="<admin_jwt_token>"

# Set role ADMIN cho user ID 4
curl -X PUT http://localhost:8080/api/admin/users/4/role \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "ADMIN"}'
```

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Response:**
```json
{
  "id": 4,
  "role": "ADMIN"
}
```

### Cách 3: Login với ADMIN có sẵn

Sử dụng tài khoản ADMIN có sẵn:
- Email: `admin@compassed.com`
- Password: *(cần kiểm tra hoặc reset)*

```bash
# Reset password cho admin@compassed.com (nếu cần)
mysql -u root -proot compassed_db -e "UPDATE users SET password = '<bcrypt_hash>' WHERE email = 'admin@compassed.com';"
```

---

## 📡 APIs YÊU CẦU ADMIN

### 1. User Management

#### GET /api/admin/users
Lấy danh sách tất cả users

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/admin/users
```

#### PUT /api/admin/users/{userId}/role
Thay đổi role của user

```bash
curl -X PUT http://localhost:8080/api/admin/users/4/role \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"role": "ADMIN"}'
```

#### DELETE /api/admin/users/{userId}
Xóa user

```bash
curl -X DELETE http://localhost:8080/api/admin/users/4 \
  -H "Authorization: Bearer <token>"
```

### 2. Question Bank

#### GET /api/admin/question-bank
Lấy danh sách câu hỏi

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/admin/question-bank
```

#### POST /api/admin/question-bank
Thêm câu hỏi mới

```bash
curl -X POST http://localhost:8080/api/admin/question-bank \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is Java?",
    "answer": "A",
    "options": ["A programming language", "A coffee", "An island", "A framework"],
    "difficulty": "EASY",
    "subjectId": 1
  }'
```

#### PUT /api/admin/question-bank/{questionId}
Cập nhật câu hỏi

```bash
curl -X PUT http://localhost:8080/api/admin/question-bank/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is Python?",
    "answer": "A",
    "options": ["A programming language", "A snake", "A movie", "A tool"]
  }'
```

#### DELETE /api/admin/question-bank/{questionId}
Xóa câu hỏi

```bash
curl -X DELETE http://localhost:8080/api/admin/question-bank/1 \
  -H "Authorization: Bearer <token>"
```

### 3. Subject Management

#### GET /api/admin/subjects
Lấy danh sách môn học

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/admin/subjects
```

#### POST /api/admin/subjects
Tạo môn học mới

```bash
curl -X POST http://localhost:8080/api/admin/subjects \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Python Programming",
    "description": "Learn Python from scratch",
    "icon": "python-icon.png"
  }'
```

### 4. Roadmap Management

#### GET /api/admin/roadmaps
Lấy danh sách roadmaps

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/admin/roadmaps
```

#### POST /api/admin/roadmaps
Tạo roadmap mới

```bash
curl -X POST http://localhost:8080/api/admin/roadmaps \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Backend Developer",
    "description": "Become a backend developer",
    "subjectId": 1
  }'
```

---

## 🧪 TEST PHÂN QUYỀN

### 1. Login với ADMIN user

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@compassed.com",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 10,
    "email": "admin@compassed.com",
    "fullName": "Admin User",
    "role": "ADMIN"
  }
}
```

**Lưu token:**
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. Test QuestionBank API

```bash
# GET - Lấy danh sách câu hỏi
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/question-bank
```

**Expected:**
- Status: `200 OK`
- Body: Danh sách câu hỏi

### 3. Test User Management API

```bash
# GET - Lấy danh sách users
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/users
```

**Expected:**
- Status: `200 OK`
- Body: Danh sách users với đầy đủ thông tin

### 4. Test với USER role (Nên bị từ chối)

```bash
# Login với USER
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hoangtinh12122003@gmail.com",
    "password": "user_password"
  }'

USER_TOKEN="<user_jwt_token>"

# Try to access admin endpoint
curl -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8080/api/admin/users
```

**Expected:**
- Status: `403 Forbidden`
- Body: `{"error": "ACCESS_DENIED", "message": "Insufficient permissions"}`

---

## 🌐 TRUY CẬP ADMIN DASHBOARD

### Bước 1: Đảm bảo Backend và Frontend đang chạy

```bash
# Backend
cd BE/compassed-api
./mvnw spring-boot:run

# Frontend (terminal khác)
cd FE
python3 Extensions.py
```

### Bước 2: Login với ADMIN user

1. Mở trình duyệt: `http://localhost:3000`
2. Click **Đăng nhập**
3. Nhập:
   - Email: `admin@compassed.com`
   - Password: `admin123` (hoặc password của bạn)
4. Click **Đăng nhập**

### Bước 3: Truy cập Admin Dashboard

**Cách 1:** Trực tiếp URL
```
http://localhost:3000/admin-dashboard
```

**Cách 2:** Qua menu (nếu có)
- Click vào user menu
- Click **Admin Dashboard**

### Bước 4: Kiểm tra tính năng

✅ **User Management:**
- Xem danh sách users
- Set role ADMIN/USER
- Xóa users

✅ **Question Bank:**
- Xem câu hỏi
- Thêm câu hỏi mới
- Sửa câu hỏi
- Xóa câu hỏi

---

## 🔍 TROUBLESHOOTING

### Lỗi 401 UNAUTHORIZED

**Nguyên nhân:** Token không hợp lệ hoặc hết hạn

**Giải pháp:**
1. Login lại để lấy token mới
2. Kiểm tra header `Authorization: Bearer <token>`

### Lỗi 403 FORBIDDEN

**Nguyên nhân:** User không có role ADMIN

**Giải pháp:**
1. Kiểm tra role trong database:
   ```bash
   mysql -u root -proot compassed_db -e "SELECT id, email, role FROM users WHERE email = 'your@email.com';"
   ```

2. Set role ADMIN:
   ```bash
   mysql -u root -proot compassed_db -e "UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';"
   ```

### Admin Dashboard không hiển thị

**Nguyên nhân:** 
- Frontend chưa chạy
- Route không đúng

**Giải pháp:**
1. Kiểm tra Flask đang chạy trên port 3000
2. Kiểm tra console browser xem có lỗi không
3. Kiểm tra file `FE/template/admin/adminDashboard.html` có tồn tại không

---

## 📝 TÓM TẮT

### URLs quan trọng

- **Landing Page:** `http://localhost:3000`
- **Admin Dashboard:** `http://localhost:3000/admin-dashboard`
- **API Base:** `http://localhost:8080/api`

### Tài khoản ADMIN

- **Email:** `admin@compassed.com`
- **ID:** 10
- **Role:** ADMIN

### Endpoints ADMIN

- User Management: `/api/admin/users`
- Question Bank: `/api/admin/question-bank`
- Subjects: `/api/admin/subjects`
- Roadmaps: `/api/admin/roadmaps`
- Lessons: `/api/admin/lessons`
- Mini Tests: `/api/admin/mini-tests`

### Script hữu ích

```bash
# Set ADMIN role
bash set-admin-role.sh <userId>

# Kiểm tra users
mysql -u root -proot compassed_db -e "SELECT id, email, role FROM users;"

# Reset password
mysql -u root -proot compassed_db -e "UPDATE users SET password = '<bcrypt>' WHERE email = 'admin@compassed.com';"
```
