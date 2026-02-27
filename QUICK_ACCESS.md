# 🚀 Truy cập nhanh - CompassED Admin

## ✅ Hệ thống đã sẵn sàng!

### 📍 **Đăng nhập Admin**

🌐 **URL**: http://localhost:3000/admin-login

🔑 **Thông tin đăng nhập:**
- **Email**: `admin@compassed.com`
- **Password**: `admin123`

---

### 👥 **Quản lý Người dùng**

Sau khi đăng nhập:

1. ✅ Bạn sẽ thấy **Dashboard** với thống kê
2. 👈 Nhìn bên trái (Sidebar), click vào menu **"Quản lý Users"** 
   - Icon: 👥 (biểu tượng 2 người)
   - Ngay dưới menu "Dashboard"
3. 🎯 Bạn sẽ thấy bảng danh sách users với các nút:
   - 🟢 **Tạo User** - Tạo user mới
   - 🔵 **Sửa** - Chỉnh sửa thông tin
   - 🟣 **Đổi MK** - Đổi mật khẩu
   - 🟢/🟡 **Set Admin / Hủy Admin**
   - 🔴 **Xóa** - Xóa user
   - 🔄 **Làm mới** - Reload danh sách

---

### 🎨 **Giao diện Menu**

```
┌─────────────────────────────┐
│ CompassED                   │
│ Admin Portal                │
├─────────────────────────────┤
│ 👤 Admin                    │
│ admin@compassed.com         │
├─────────────────────────────┤
│ 📊 Dashboard               │  ← Trang chủ
│ 👥 Quản lý Users           │  ← CLICK VÀO ĐÂY!
│ 💳 Subscriptions           │
│ ✅ Placement Tests         │
│ 🛣️  Roadmaps               │
│ 📚 Subjects                │
├─────────────────────────────┤
│ 🚪 Logout                  │
└─────────────────────────────┘
```

---

### 🔍 **Không thấy menu?**

**Kiểm tra:**

1. Bạn đã đăng nhập chưa? → http://localhost:3000/admin-login
2. Bạn đã dùng đúng tài khoản admin chưa?
   - Email: admin@compassed.com
   - Password: admin123
3. Sau khi đăng nhập, bạn có thấy sidebar màu đen bên trái không?
4. Click vào dòng có icon 👥 và text **"Quản lý Users"**

---

### 📸 **Screenshot mô tả vị trí**

```
┌──────────────────────────────────────────────────────────┐
│ [SIDEBAR]              [MAIN CONTENT AREA]               │
│                                                           │
│ CompassED              Dashboard                         │
│ Admin Portal                                             │
│                        Total Users: 1                    │
│ 👤 Admin               Subjects: 3                       │
│ admin@compass.com      ...                               │
│                                                           │
│ 📊 Dashboard      ← Active (màu xanh)                   │
│ 👥 Quản lý Users  ← CLICK VÀO ĐÂY!                     │
│ 💳 Subscriptions                                         │
│ ✅ Placement Tests                                       │
│ 🛣️ Roadmaps                                             │
│ 📚 Subjects                                              │
│                                                           │
│ 🚪 Logout                                                │
└──────────────────────────────────────────────────────────┘
```

---

### ⚡ **Các trang khác**

- 🏠 **Dashboard**: http://localhost:3000/admin (sau khi login)
- 👥 **Users**: Trong admin, click "Quản lý Users"
- 📝 **Question Bank**: http://localhost:3000/admin/question-bank
- 🎓 **Placement Test (User)**: http://localhost:3000/placement-test?subjectId=1

---

### 🛠️ **Backend API**

Backend đang chạy tại: http://localhost:8080

**Test API:**
```bash
# Lấy danh sách subjects
curl http://localhost:8080/api/subjects

# Kiểm tra backend hoạt động
curl http://localhost:8080/api/subjects | jq
```

---

### 📚 **Tài liệu chi tiết**

- 📘 `USER_MANAGEMENT_GUIDE.md` - Hướng dẫn đầy đủ
- 📗 `USER_MANAGEMENT_COMPLETE.md` - Tóm tắt tính năng

---

### ❓ **Vẫn không thấy?**

1. Mở Console Browser (F12)
2. Xem tab "Console" có lỗi gì không
3. Kiểm tra tab "Network" xem API có gọi thành công không
4. Reload lại trang (Ctrl+R hoặc Cmd+R)
5. Clear cache: Ctrl+Shift+Delete (hoặc Cmd+Shift+Delete)

---

**Cần hỗ trợ?** Hãy cho tôi biết bạn đang ở trang nào và thấy gì trên màn hình! 😊
