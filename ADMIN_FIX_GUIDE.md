# 🔐 ADMIN LOGIN - HƯỚNG DẪN HOÀN CHỈNH

## ✅ ĐÃ GIẢI QUYẾT VẤN ĐỀ

**Vấn đề**: Tài khoản `admin@compassed.com` không có quyền ADMIN

**Nguyên nhân**: 
- Profile `local` trong `application-local.yml` **DISABLE MySQL**
- Data không được lưu vào database thật
- Mỗi lần restart backend, data bị mất

**Giải pháp**:
- ✅ Sửa `application-local.yml` để **ENABLE MySQL**
- ✅ Xóa user cũ và tạo lại
- ✅ Set role ADMIN trong database
- ✅ Restart backend để apply

---

## 📋 THÔNG TIN TÀI KHOẢN ADMIN

```
Email:    admin@compassed.com
Password: admin123
Role:     ADMIN
```

---

## 🚀 CÁCH CHẠY ADMIN

### Bước 1: Chạy script tự động (KHUYẾN NGHỊ)

```bash
bash fix-admin-complete.sh
```

Script này sẽ:
1. Stop backend + frontend cũ
2. Xóa user admin cũ
3. Start backend với MySQL
4. Tạo user admin mới
5. Set role ADMIN
6. Start frontend
7. Verify kết quả

### Bước 2: Truy cập trang admin

```
http://localhost:3000/admin-login
```

### Bước 3: Đăng nhập

- Email: `admin@compassed.com`
- Password: `admin123`

---

## 🔧 CÁCH FIX THỦ CÔNG (Nếu script không chạy)

### 1. Stop các process

```bash
pkill -9 -f "spring-boot:run"
pkill -9 -f "Extensions.py"
```

### 2. Xóa user cũ

```bash
mysql -u root -proot compassed_db << 'EOF'
DELETE FROM users WHERE email='admin@compassed.com';
EOF
```

### 3. Start Backend

```bash
cd BE/compassed-api
./mvnw spring-boot:run &
```

Đợi 10-15 giây để backend khởi động xong.

### 4. Đăng ký user admin

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin User",
    "email": "admin@compassed.com",
    "password": "admin123"
  }'
```

Lưu lại `user.id` trong response (ví dụ: `"id": 1`).

### 5. Set role ADMIN

```bash
mysql -u root -proot compassed_db << 'EOF'
UPDATE users SET role='ADMIN' WHERE email='admin@compassed.com';
SELECT id, email, full_name, role FROM users WHERE email='admin@compassed.com';
EOF
```

### 6. RESTART Backend (QUAN TRỌNG!)

```bash
pkill -9 -f "spring-boot:run"
sleep 2
cd BE/compassed-api
./mvnw spring-boot:run &
```

**LƯU Ý**: Phải restart backend sau khi update role, nếu không role mới sẽ không có hiệu lực!

### 7. Start Frontend

```bash
cd FE
python3 Extensions.py
```

---

## ✅ VERIFY KẾT QUẢ

### 1. Kiểm tra trong database

```bash
mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users WHERE email='admin@compassed.com';"
```

Kết quả mong đợi:
```
id  | email                  | full_name  | role
----|------------------------|------------|------
1   | admin@compassed.com    | Admin User | ADMIN
```

### 2. Kiểm tra bằng API

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@compassed.com","password":"admin123"}' \
  | jq '.user.role'
```

Kết quả mong đợi: `"ADMIN"`

### 3. Test quyền admin

```bash
# Login và lấy token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@compassed.com","password":"admin123"}' \
  | jq -r '.token')

# Test admin endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/users
```

Nếu thành công → trả về danh sách users
Nếu thất bại → trả về `403 Forbidden`

---

## 🐛 TROUBLESHOOTING

### Vấn đề 1: Login API trả về role = USER (không phải ADMIN)

**Nguyên nhân**: Backend đang cache user cũ

**Giải pháp**:
```bash
# Restart backend
pkill -9 -f "spring-boot:run"
cd BE/compassed-api
./mvnw spring-boot:run &
```

Sau đó **Logout** và **Login lại** trong browser.

---

### Vấn đề 2: Frontend hiển thị "Bạn không có quyền truy cập Admin Portal"

**Nguyên nhân**: Token cũ trong localStorage vẫn có role USER

**Giải pháp**:
1. Mở DevTools (F12)
2. Application → Local Storage → `http://localhost:3000`
3. Xóa `token` và `user`
4. Reload page và login lại

Hoặc:
```javascript
// Chạy trong Console
localStorage.clear();
location.reload();
```

---

### Vấn đề 3: Backend không kết nối được MySQL

**Kiểm tra**:
```bash
mysql -u root -proot -e "SELECT 1;"
```

Nếu lỗi → Start MySQL:
```bash
brew services start mysql
```

---

### Vấn đề 4: Port 8080 bị chiếm

**Giải pháp**:
```bash
lsof -ti:8080 | xargs kill -9
```

---

### Vấn đề 5: User đã tồn tại nhưng role vẫn là USER

**Giải pháp**:
```bash
# Option 1: Update role
mysql -u root -proot compassed_db -e "UPDATE users SET role='ADMIN' WHERE email='admin@compassed.com';"

# Option 2: Xóa và tạo lại
mysql -u root -proot compassed_db -e "DELETE FROM users WHERE email='admin@compassed.com';"
# Sau đó đăng ký lại bằng API
```

**LƯU Ý**: Sau khi update, PHẢI restart backend!

---

## 📁 FILES QUAN TRỌNG

1. **Backend Config**:
   ```
   BE/compassed-api/src/main/resources/application-local.yml
   ```
   - Chứa cấu hình MySQL
   - Profile: `local`

2. **Trang Admin Login**:
   ```
   FE/template/admin/admin-login.html
   ```
   - Kiểm tra role ADMIN trước khi cho phép truy cập

3. **Flask Routes**:
   ```
   FE/Extensions.py
   ```
   - Route: `/admin-login`

4. **Scripts**:
   - `fix-admin-complete.sh` - Fix toàn bộ tự động
   - `setup-admin.sh` - Setup ban đầu
   - `create-admin-fresh.sh` - Tạo admin mới

---

## 🎯 CHECKLIST

- [ ] MySQL đang chạy
- [ ] Backend đang chạy trên port 8080
- [ ] Frontend đang chạy trên port 3000
- [ ] User `admin@compassed.com` có role = `ADMIN` trong database
- [ ] API login trả về role = `ADMIN`
- [ ] Có thể đăng nhập vào `/admin-login`
- [ ] Có thể truy cập `/admin-dashboard`
- [ ] Admin APIs trả về data (không phải 403)

---

## 🌐 URLS

| URL | Mục đích |
|-----|----------|
| `http://localhost:3000/admin-login` | Đăng nhập admin |
| `http://localhost:3000/admin-dashboard` | Trang quản trị |
| `http://localhost:3000/auth` | Đăng nhập user thường |
| `http://localhost:8080/api/admin/users` | API quản lý users |
| `http://localhost:8080/api/admin/question-bank` | API quản lý câu hỏi |

---

## 📞 HỖ TRỢ

Nếu vẫn gặp vấn đề:

1. **Kiểm tra log backend**:
   ```bash
   tail -50 /tmp/backend-final.log
   ```

2. **Kiểm tra log frontend**:
   ```bash
   tail -50 /tmp/frontend-final.log
   ```

3. **Kiểm tra database**:
   ```bash
   mysql -u root -proot compassed_db -e "SHOW TABLES; SELECT * FROM users;"
   ```

4. **Test connectivity**:
   ```bash
   curl http://localhost:8080/api/subjects
   curl http://localhost:3000/
   ```

---

**🎉 Chúc mừng! Bạn đã có tài khoản ADMIN hoàn chỉnh!**
