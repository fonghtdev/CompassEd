# 🚀 HƯỚNG DẪN CHẠY DỰ ÁN COMPASSED

## 📋 Yêu cầu hệ thống
- ✅ Java 17+ (đã có)
- ✅ Maven (đã có trong ./mvnw)
- ✅ MySQL đang chạy
- ✅ Python 3 (đã có)

---

## 🎯 CÁCH CHẠY NHANH (3 TERMINAL)

### **Terminal 1: Setup MySQL** (chỉ chạy 1 lần đầu)
```bash
cd /Users/hoangngoctinh/compassED/ED
bash setup-mysql.sh
```

### **Terminal 2: Backend Server**
```bash
cd /Users/hoangngoctinh/compassED/ED
bash start-backend.sh
```
⏳ Đợi đến khi thấy: `Started CompassedApiApplication in X.XX seconds`

### **Terminal 3: Frontend Server** (sau khi Backend đã chạy)
```bash
cd /Users/hoangngoctinh/compassED/ED
bash start-frontend.sh
```

---

## 🌐 MỞ TRÌNH DUYỆT

Sau khi cả 2 server đã chạy:

| Trang | URL | Mô tả |
|-------|-----|-------|
| 🏠 Landing Page | http://localhost:3000 | Trang chủ |
| 📝 Placement Test | http://localhost:3000/placementTest.html | Bài test đầu vào |
| 🗺️ Roadmap | http://localhost:3000/roadmap.html | Lộ trình học |
| 👤 Login | http://localhost:3000/auth.html | Đăng nhập user |
| 🔐 Admin Login | http://localhost:3000/admin-login | Đăng nhập admin |
| 📊 Admin Dashboard | http://localhost:3000/admin | Trang quản trị |
| 📚 Question Bank | http://localhost:3000/admin/question-bank | **Quản lý ngân hàng câu hỏi** |

---

## 🔑 TẠO ADMIN ACCOUNT

### Cách 1: Qua API
```bash
curl -X POST http://localhost:8080/api/dev/create-admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@compassed.com","fullName":"Admin User","password":"admin123"}'
```

### Cách 2: Qua MySQL
```bash
mysql -u root -proot compassed_db

# Trong MySQL console:
UPDATE users SET role = 'ADMIN' WHERE email = 'your-email@gmail.com';
```

---

## 🛑 DỪNG SERVERS

### Dừng Backend:
```bash
lsof -ti:8080 | xargs kill -9
```

### Dừng Frontend:
```bash
lsof -ti:3000 | xargs kill -9
```

### Hoặc dùng script:
```bash
bash stop-all.sh
```

---

## 🐛 XỬ LÝ LỖI THƯỜNG GẶP

### ❌ Backend không chạy
```bash
# Kiểm tra MySQL
mysql -u root -proot -e "SELECT 1"

# Kiểm tra port 8080
lsof -ti:8080

# Xem log backend
tail -f /tmp/backend.log
```

### ❌ Frontend không chạy
```bash
# Kiểm tra Python
python3 --version

# Install dependencies
python3 -m pip install flask requests

# Kiểm tra port 3000
lsof -ti:3000
```

### ❌ Cannot connect to MySQL
```bash
# Start MySQL service (macOS)
brew services start mysql

# Or
mysql.server start
```

---

## 📂 CẤU TRÚC PROJECT

```
ED/
├── BE/compassed-api/          # Backend Spring Boot
│   ├── src/main/java/...
│   └── mvnw                   # Maven wrapper
├── FE/                        # Frontend Flask
│   ├── template/              # HTML templates
│   ├── js/                    # JavaScript files
│   ├── css/                   # Stylesheets
│   └── Extensions.py          # Flask server
├── start-backend.sh           # Script chạy backend
├── start-frontend.sh          # Script chạy frontend
├── setup-mysql.sh             # Script setup database
└── stop-all.sh                # Script dừng tất cả
```

---

## ✨ QUESTION BANK FEATURES (Của bạn!)

Các tính năng đã merge:
- ✅ **Backend Controller**: `AdminQuestionBankController.java`
- ✅ **Service Layer**: `QuestionBankService.java`
- ✅ **Repository**: `QuestionBankRepository.java`
- ✅ **Frontend UI**: `adminQuestionBank.html`
- ✅ **JavaScript Logic**: `adminQuestionBank.js`

Truy cập tại: **http://localhost:3000/admin/question-bank**

---

## 🎉 HOÀN TẤT MERGE

✅ Code đã được merge thành công:
- Phong's codebase (origin/main)
- Your QuestionBank features
- No conflicts!

Git history:
```
main (HEAD)
├── f3483be - Merge origin/main: Accept Phong's full codebase + Keep QuestionBank features
└── c8253bc - feat: Add Admin Features - User Management & Question Bank
```

---

## 📞 HỖ TRỢ

Nếu gặp vấn đề:
1. Kiểm tra log trong terminal
2. Test API: `curl http://localhost:8080/api/subjects`
3. Kiểm tra MySQL: `mysql -u root -proot compassed_db -e "SHOW TABLES;"`

---

**Tạo bởi:** GitHub Copilot Agent  
**Ngày:** 27/02/2026  
**Version:** 1.0 - Post Merge Success 🎊
