# 🎯 Hướng dẫn truy cập User Management

## ✅ URL mới để quản lý user:

```
http://localhost:3000/admin/users
```

## 📋 Các bước:

### Bước 1: Đăng nhập Admin
```
URL: http://localhost:3000/admin-login
Email: admin@compassed.com
Password: admin123
```

### Bước 2: Truy cập User Management
Có **2 cách**:

**Cách 1 - Qua Dashboard:**
1. Sau khi đăng nhập, bạn sẽ ở trang: `http://localhost:3000/admin`
2. Click vào **"User Management"** ở menu bên trái (menu thứ 2)
3. Sẽ chuyển đến: `http://localhost:3000/admin/users`

**Cách 2 - Truy cập trực tiếp:**
```
http://localhost:3000/admin/users
```

## 🎨 Các chức năng trên trang:

✅ **Tạo User Mới**
- Nút màu xanh lá: "Tạo User" ở góc trên bên phải
- Điền email, họ tên, mật khẩu, chọn role (USER/ADMIN)

✅ **Sửa User**
- Nút màu xanh dương "Edit" trên mỗi dòng user
- Có thể đổi email, họ tên, role

✅ **Đổi mật khẩu**
- Nút màu tím "Change Password" 
- Nhập mật khẩu mới (tối thiểu 6 ký tự)

✅ **Xóa User**
- Nút màu đỏ "Delete"
- Sẽ có xác nhận trước khi xóa

✅ **Toggle Admin Role**
- Nút màu vàng/xanh "Set Admin" / "Hủy Admin"
- Chuyển đổi giữa USER và ADMIN

## 🔄 Làm mới danh sách
Nếu không thấy thay đổi, nhấn nút **"🔄 Refresh"** ở góc trên

## ⚠️ Lưu ý:
- **PHẢI đăng nhập** với tài khoản Admin trước
- Nếu click không hoạt động, thử:
  - Nhấn **Cmd + Shift + R** (hard refresh)
  - Xóa cache browser
  - Kiểm tra Console (F12) xem có lỗi gì không

## 🚀 Kiểm tra nhanh:
```bash
# Kiểm tra Backend đang chạy:
curl http://localhost:8080/api/subjects

# Kiểm tra Frontend đang chạy:
curl http://localhost:3000/admin/users | head
```

## 📞 Backend API được sử dụng:
```
GET    /api/admin/users           - Lấy danh sách users
GET    /api/admin/users/{id}      - Chi tiết 1 user
POST   /api/admin/users           - Tạo user mới
PUT    /api/admin/users/{id}      - Cập nhật user
DELETE /api/admin/users/{id}      - Xóa user
PUT    /api/admin/users/{id}/password     - Đổi mật khẩu
POST   /api/admin/users/{id}/toggle-admin - Toggle admin role
```

---

**Tóm lại:** Vào `http://localhost:3000/admin/users` sau khi đăng nhập! 🎉
