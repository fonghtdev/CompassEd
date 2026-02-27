# Hướng dẫn Quản lý Người dùng - CompassED

## 📋 Tổng quan

Chức năng quản lý người dùng cho phép Admin thực hiện các thao tác CRUD (Create, Read, Update, Delete) với tài khoản người dùng trong hệ thống CompassED.

## 🚀 Khởi động hệ thống

### Backend (Spring Boot)
```bash
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run
```
- Port: **8080**
- URL: http://localhost:8080

### Frontend (Flask)
```bash
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py
```
- Port: **3000**
- URL: http://localhost:3000

## 🔑 Đăng nhập Admin

1. Truy cập: http://localhost:3000/admin-login
2. Thông tin đăng nhập:
   - **Email**: admin@compassed.com
   - **Password**: admin123

## 👥 Các chức năng quản lý User

### 1. Xem danh sách Users

1. Đăng nhập vào Admin Dashboard
2. Click menu **"Quản lý Users"** (biểu tượng 👥)
3. Danh sách hiển thị các thông tin:
   - **ID**: Mã định danh user
   - **Email**: Email đăng nhập
   - **Họ tên**: Tên đầy đủ
   - **Role**: USER hoặc ADMIN
   - **Provider**: local (đăng ký thủ công) hoặc google (đăng nhập Google)
   - **Thao tác**: Các nút chức năng

### 2. Tạo User mới

1. Click nút **"Tạo User"** (màu xanh lá, biểu tượng ➕)
2. Điền thông tin:
   - **Email** (bắt buộc): Địa chỉ email duy nhất
   - **Họ tên** (tùy chọn): Tên đầy đủ của user
   - **Mật khẩu** (bắt buộc): Tối thiểu 6 ký tự
   - **Role**: Chọn USER hoặc ADMIN
3. Click **"Tạo User"**

**Ví dụ:**
- Email: nguoidung@example.com
- Họ tên: Nguyễn Văn A
- Mật khẩu: 123456
- Role: USER

### 3. Chỉnh sửa thông tin User

1. Click nút **"Sửa"** (màu xanh dương, biểu tượng ✏️) ở hàng của user
2. Có thể thay đổi:
   - Email
   - Họ tên
   - Role (USER ↔ ADMIN)
3. Click **"Cập nhật"**

**Lưu ý**: Không thể đổi password qua chức năng này, dùng "Đổi MK" riêng.

### 4. Đổi mật khẩu User

1. Click nút **"Đổi MK"** (màu tím, biểu tượng 🔑)
2. Nhập:
   - **Mật khẩu mới**: Tối thiểu 6 ký tự
   - **Xác nhận mật khẩu**: Phải giống mật khẩu mới
3. Click **"Đổi mật khẩu"**

**Trường hợp sử dụng:**
- User quên mật khẩu
- Admin muốn reset mật khẩu cho user
- Tăng cường bảo mật tài khoản

### 5. Toggle Admin Role

1. Click nút **"Set Admin"** (màu xanh lá) để nâng USER lên ADMIN
2. Click nút **"Hủy Admin"** (màu vàng) để hạ ADMIN xuống USER
3. Xác nhận trong popup

**Quyền hạn:**
- **USER**: Sử dụng các tính năng học tập cơ bản
- **ADMIN**: Toàn quyền quản lý hệ thống (users, questions, roadmaps, ...)

### 6. Xóa User

1. Click nút **"Xóa"** (màu đỏ, biểu tượng 🗑️)
2. Xác nhận trong popup: **"⚠️ XÓA VĨNH VIỄN user?"**
3. Hành động **KHÔNG THỂ HOÀN TÁC**!

**Cảnh báo:**
- Xóa user sẽ xóa toàn bộ dữ liệu liên quan (placement tests, subscriptions, ...)
- Chỉ xóa khi thật sự cần thiết
- Không thể phục hồi sau khi xóa

### 7. Làm mới danh sách

Click nút **"Làm mới"** (màu xanh dương, biểu tượng 🔄) để tải lại danh sách users mới nhất từ database.

## 🔧 API Endpoints Backend

### GET /api/admin/users
Lấy danh sách tất cả users (passwordHash đã được xóa)

**Response:**
```json
[
  {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "USER",
    "provider": "local",
    "passwordHash": null
  }
]
```

### GET /api/admin/users/{id}
Lấy thông tin chi tiết 1 user

### POST /api/admin/users
Tạo user mới

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "fullName": "New User",
  "password": "123456",
  "role": "USER"
}
```

### PUT /api/admin/users/{id}
Cập nhật thông tin user (email, fullName, role)

**Request Body:**
```json
{
  "email": "updated@example.com",
  "fullName": "Updated Name",
  "role": "ADMIN"
}
```

### PUT /api/admin/users/{id}/password
Đổi password cho user

**Request Body:**
```json
{
  "newPassword": "newpass123"
}
```

### POST /api/admin/users/{id}/toggle-admin
Toggle role giữa USER và ADMIN

### DELETE /api/admin/users/{id}
Xóa user vĩnh viễn

## 🔒 Bảo mật

- Tất cả endpoints đều yêu cầu JWT token với role ADMIN
- Password được mã hóa bằng BCrypt trước khi lưu database
- Password không bao giờ được trả về trong API response
- CORS được cấu hình cho localhost:3000

## 📝 Lưu ý khi sử dụng

1. **Email phải duy nhất**: Không được tạo 2 users có cùng email
2. **Password tối thiểu 6 ký tự**: Đảm bảo an toàn cơ bản
3. **Không tự xóa tài khoản admin đang đăng nhập**: Có thể mất quyền truy cập
4. **Backup database trước khi xóa nhiều users**: Phòng trường hợp cần phục hồi

## 🐛 Troubleshooting

### Backend không khởi động được
```bash
# Kiểm tra port 8080 có bị chiếm không
lsof -ti:8080 | xargs kill -9

# Kiểm tra MySQL đang chạy
mysql -u root -proot -e "SELECT 1"
```

### Frontend không kết nối được Backend
- Kiểm tra backend đã chạy: http://localhost:8080/api/subjects
- Kiểm tra CORS trong `AdminController.java`: `@CrossOrigin(origins = {"http://localhost:3000"})`

### Không xem được danh sách users
- Kiểm tra đã đăng nhập với tài khoản ADMIN
- Mở Console Browser (F12) để xem lỗi API
- Kiểm tra JWT token trong localStorage: `compassed_admin_token`

### Lỗi "Email đã tồn tại"
- Mỗi user chỉ có 1 email duy nhất
- Kiểm tra database: `SELECT * FROM users WHERE email='...'`

## 📊 Database Schema

### Table: users
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    password_hash VARCHAR(255),
    provider VARCHAR(50),
    provider_user_id VARCHAR(255),
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);
```

## 🎯 Tính năng tiếp theo (Roadmap)

- [ ] Tìm kiếm và lọc users (theo email, role, provider)
- [ ] Phân trang danh sách users (pagination)
- [ ] Export danh sách users ra CSV/Excel
- [ ] Xem lịch sử hoạt động của user (placement tests, subscriptions)
- [ ] Khóa/mở khóa tài khoản user (ban/unban)
- [ ] Gửi email reset password tự động
- [ ] Thống kê số lượng users theo thời gian
- [ ] Bulk operations (xóa nhiều users cùng lúc)

## 📞 Liên hệ hỗ trợ

Nếu gặp vấn đề khi sử dụng, vui lòng:
1. Kiểm tra phần Troubleshooting
2. Xem logs ở terminal Backend và Frontend
3. Kiểm tra database bằng MySQL Workbench hoặc CLI

---

**Phiên bản**: 1.0  
**Ngày cập nhật**: 26/02/2026  
**Tác giả**: CompassED Development Team
