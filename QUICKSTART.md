# 🚀 CompassED - Quick Start Guide

## Cách chạy ứng dụng (1 lệnh duy nhất!)

### Khởi động:
```bash
cd /Users/hoangngoctinh/compassED/ED
bash start-all.sh
```

### Dừng:
```bash
cd /Users/hoangngoctinh/compassED/ED
bash stop-all.sh
```

---

## 🌐 URLs

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Trang đăng nhập/đăng ký**: http://localhost:3000/auth

---

## 📋 Hướng dẫn sử dụng lần đầu

### 1. Khởi động ứng dụng
```bash
bash start-all.sh
```

### 2. Xóa localStorage cũ (chỉ lần đầu)
- Mở trình duyệt: http://localhost:3000/auth
- Nhấn **F12** (Developer Tools)
- Vào tab **Console**
- Chạy lệnh:
```javascript
localStorage.clear();
sessionStorage.clear();
location.reload();
```

### 3. Đăng ký tài khoản
- Click tab **"Đăng ký"**
- Nhập thông tin: Họ tên, Email, Mật khẩu
- Click **"Đăng ký"**

### 4. Đăng nhập
- Nhập Email và Mật khẩu đã đăng ký
- Click **"Đăng nhập"**

---

## 📝 Xem Logs

### Backend logs:
```bash
tail -f /tmp/compassed-backend.log
```

### Frontend logs:
```bash
tail -f /tmp/compassed-frontend.log
```

---

## 🔧 Troubleshooting

### Nếu port bị chiếm:
```bash
# Kill process trên port 8080 (Backend)
lsof -ti:8080 | xargs kill -9

# Kill process trên port 3000 (Frontend)
lsof -ti:3000 | xargs kill -9
```

### Nếu backend không start:
```bash
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw clean install
bash /Users/hoangngoctinh/compassED/ED/start-all.sh
```

### Nếu MySQL lỗi kết nối:
```bash
# Check MySQL đang chạy
mysql -u root -proot -e "SELECT 1;"

# Nếu lỗi password, reset:
mysql -u root -p'' -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root'; FLUSH PRIVILEGES;"
```

### Xóa tất cả users trong database:
```bash
mysql -u root -proot compassed_db -e "SET FOREIGN_KEY_CHECKS=0; DELETE FROM users; SET FOREIGN_KEY_CHECKS=1;"
```

---

## 🎯 Lưu ý quan trọng

1. **Luôn dùng `start-all.sh`** để khởi động - đảm bảo code mới nhất được load
2. **Đừng quên xóa localStorage** khi có thay đổi về authentication
3. **Backend phải chạy trước Frontend** - script đã tự động xử lý
4. **Check logs** nếu có lỗi: `/tmp/compassed-backend.log` và `/tmp/compassed-frontend.log`

---

## ✅ Đã sửa gì so với code cũ?

### Authentication Token Format:
- ❌ **Trước**: Backend trả `token: "Bearer 1"` → Frontend thêm "Bearer" → `"Bearer Bearer 1"` → LỖI
- ✅ **Sau**: Backend trả `token: "1"` → Frontend thêm "Bearer" → `"Bearer 1"` → ĐÚNG

### File đã sửa:
- `BE/compassed-api/src/main/java/com/compassed/compassed_api/service/impl/AuthServiceJpaImpl.java`
  - Dòng 132: `String token = String.valueOf(user.getId());` (bỏ "Bearer " prefix)

---

## 📞 Liên hệ

Nếu gặp vấn đề, check:
1. Backend log: `/tmp/compassed-backend.log`
2. Frontend log: `/tmp/compassed-frontend.log`
3. MySQL: `mysql -u root -proot -e "SHOW DATABASES;"`
4. Ports: `lsof -ti:8080 && lsof -ti:3000`
