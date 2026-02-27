# ✅ ADMIN - HƯỚNG DẪN ĐƠN GIẢN

## 📋 TÀI KHOẢN ADMIN ĐÃ CÓ SẴN

```
Email:    hoangtinh12122003@gmail.com
Password: (password bạn đã đăng ký)
Role:     ADMIN ✅
```

## 🚀 CÁCH SỬ DỤNG

### 1. Restart Backend (BẮT BUỘC)

```bash
# Stop backend cũ
pkill -9 -f "spring-boot:run"

# Đợi 2 giây
sleep 2

# Start lại
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run
```

**Đợi khoảng 10-15 giây** để backend khởi động xong.

### 2. Đăng nhập Admin

Truy cập: **http://localhost:3000/admin-login**

Nhập:
- Email: `hoangtinh12122003@gmail.com`
- Password: (password của bạn)

### 3. Xong!

Sau khi đăng nhập, bạn sẽ có quyền ADMIN và có thể truy cập:
- Admin Dashboard
- User Management
- Question Bank

## 🔍 KIỂM TRA

### Xem users trong database:

```bash
mysql -u root -proot compassed_db -e "SELECT id, email, role FROM users;"
```

### Test login với API:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"hoangtinh12122003@gmail.com","password":"YOUR_PASSWORD"}'
```

Sẽ trả về role = `"ADMIN"` ✅

## 💡 NẾU CẦN TẠO ADMIN MỚI

```bash
# 1. Đăng ký user mới qua API hoặc UI
# 2. Lấy user ID
# 3. Set role ADMIN:

mysql -u root -proot compassed_db -e \
  "UPDATE users SET role='ADMIN' WHERE id=YOUR_USER_ID;"

# 4. RESTART backend (quan trọng!)
pkill -9 -f "spring-boot:run"
cd BE/compassed-api && ./mvnw spring-boot:run &
```

## ⚠️ LƯU Ý

- **Backend PHẢI restart** sau khi thay đổi role
- Clear localStorage trong browser nếu cần: `localStorage.clear()`
- Password phải là password bạn đã đăng ký cho tài khoản đó

## 🌐 URLS

- Admin Login: `http://localhost:3000/admin-login`
- User Login: `http://localhost:3000/auth`
- Admin Dashboard: `http://localhost:3000/admin-dashboard`

---

**Đơn giản thế thôi! Chỉ cần restart backend là xong! 🎉**
