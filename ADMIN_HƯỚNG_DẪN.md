# Hướng Dẫn Tạo Admin - Đơn Giản Nhất

## ⚠️ VẤN ĐỀ HIỆN TẠI

Backend đang chạy với `application-local.yml` CŨ (không có MySQL). 
Khi bạn đăng ký user, nó lưu vào memory nhưng KHÔNG lưu vào database.

## ✅ GIẢI PHÁP - 2 BƯỚC

### BƯỚC 1: Khởi động lại backend với MySQL

```bash
cd /Users/hoangngoctinh/compassED/ED
bash restart-backend.sh
```

**Script này sẽ:**
1. Dừng backend cũ
2. Khởi động backend mới (kết nối MySQL)
3. Tự động tạo user `admin@test.com`
4. Cập nhật role thành ADMIN trong database

**CHỜ 40 giây** để script chạy xong!

### BƯỚC 2: Khởi động lại backend lần cuối (để role có hiệu lực)

```bash
bash restart-backend-final.sh
```

**Chờ 40 giây** nữa!

## 🎯 ĐĂNG NHẬP

Sau khi cả 2 script chạy xong:

1. Mở: **http://localhost:3000/admin-login**
2. Nhập:
   - **Email:** `admin@test.com`
   - **Password:** `123456`
3. Click **Đăng nhập**

## ❓ NẾU VẪN LỖI

Nếu vẫn báo "Invalid email or password":

```bash
# Kiểm tra user trong database
mysql -u root -proot -D compassed_db -e "SELECT id, email, role FROM users WHERE email='admin@test.com';"

# Nếu không có user, chạy lại:
bash restart-backend.sh
```

## 📝 LƯU Ý

- Backend PHẢI restart **2 LẦN**:
  - Lần 1: Để lưu user vào MySQL
  - Lần 2: Để role ADMIN có hiệu lực trong JWT token

- **ĐỪNG Ctrl+C** khi script đang chạy!

- Nếu muốn xem log backend:
  ```bash
  tail -f /tmp/backend.log
  ```

## 🔐 THÔNG TIN ĐĂNG NHẬP

```
URL:      http://localhost:3000/admin-login
Email:    admin@test.com
Password: 123456
Role:     ADMIN
```

---

**Hoàn tất! Sau 2 lần restart backend (tổng ~80 giây), bạn có thể đăng nhập admin.**
