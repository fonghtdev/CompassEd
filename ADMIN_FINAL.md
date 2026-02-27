# ✅ ADMIN LOGIN - ĐÃ HOÀN TẤT

## 📋 THÔNG TIN ĐĂNG NHẬP

```
Email:    sysadmin@ed.com
Password: admin123
Role:     ADMIN (sau khi restart backend)
```

## 🌐 TRUY CẬP

```
http://localhost:3000/admin-login
```

## ⚠️ QUAN TRỌNG: RESTART BACKEND ĐỂ ROLE CÓ HIỆU LỰC!

Backend đang cache user với role USER. Cần restart để role ADMIN có hiệu lực:

```bash
# 1. Stop backend
pkill -9 -f "spring-boot:run"

# 2. Start lại
cd BE/compassed-api
./mvnw spring-boot:run &

# 3. Đợi 10-15 giây

# 4. Truy cập admin login
# http://localhost:3000/admin-login
```

## 🔍 VERIFY

Test role sau khi restart:

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sysadmin@ed.com","password":"admin123"}' \
  | jq '.user.role'
```

Kết quả mong đợi: `"ADMIN"`

## 💡 TROUBLESHOOTING

### 1. Nếu vẫn hiển thị role = USER

**Nguyên nhân**: Backend cache user cũ

**Giải pháp**:
- Restart backend (xem bên trên)
- Clear localStorage trong browser: `localStorage.clear(); location.reload();`
- Logout và login lại

### 2. Nếu không login được

**Kiểm tra**:
```bash
# Backend chạy chưa?
lsof -ti:8080

# Frontend chạy chưa?
lsof -ti:3000

# MySQL chạy chưa?
mysql -u root -proot -e "SELECT 1;"
```

**Start các service**:
```bash
# MySQL
brew services start mysql

# Backend
cd BE/compassed-api && ./mvnw spring-boot:run &

# Frontend
cd FE && python3 Extensions.py &
```

## 📊 QUICK CHECK

```bash
bash check-admin.sh
```

Script này sẽ hiển thị:
- ✅/❌ Backend status
- ✅/❌ Frontend status
- ✅/❌ MySQL status
- ✅/❌ Admin user status

## 🎯 TẠO TÀI KHOẢN ADMIN MỚI (Nếu cần)

Nếu muốn tạo tài khoản admin khác:

```bash
# 1. Đăng ký user mới
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "New Admin",
    "email": "newemail@domain.com",
    "password": "password123"
  }'

# Lưu lại user ID từ response

# 2. Set role ADMIN
mysql -u root -proot compassed_db -e \
  "UPDATE users SET role='ADMIN' WHERE id=<USER_ID>;"

# 3. RESTART backend
pkill -9 -f "spring-boot:run"
cd BE/compassed-api && ./mvnw spring-boot:run &
```

## 🔐 BẢO MẬT

- Đổi password mặc định `admin123` trong production
- Không commit credentials vào git
- Sử dụng biến môi trường cho sensitive data

---

**🎉 Admin portal sẵn sàng tại: http://localhost:3000/admin-login**

**📝 LƯU Ý**: Nhớ RESTART backend sau khi update role!
