# 🔥 Hot Reload Guide - CompassED

## ✅ Đã cấu hình Hot Reload thành công!

Bây giờ bạn chỉ cần **reload lại trang web** sau khi sửa code, không cần restart server!

---

## 📋 Hướng dẫn sử dụng

### 🎯 Backend (Spring Boot)

**✓ Đã enable Spring DevTools**

Khi bạn sửa code Java:
1. **Save file** (Cmd/Ctrl + S)
2. DevTools sẽ **tự động restart** trong vài giây
3. **Reload trang web** để thấy thay đổi

**Lưu ý:**
- Chỉ áp dụng cho code changes trong `src/main/java/`
- Không cần restart server thủ công
- Terminal sẽ hiển thị `restartedMain` khi reload thành công

---

### 🎨 Frontend (Flask)

**✓ Đã có Debug Mode**

Flask đã chạy với `debug=True`, nên:
1. **Save file** HTML/CSS/JS
2. Flask sẽ **tự động reload**
3. **Refresh browser** (F5 hoặc Cmd+R)

**Các file được auto-reload:**
- `template/*.html`
- `css/*.css`
- `js/*.js`
- `Extensions.py`

---

## 🚀 Khởi động hệ thống

### Lần đầu tiên:

```bash
# Terminal 1 - Backend
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py
```

### Hoặc dùng script:

```bash
cd /Users/hoangngoctinh/compassED/ED
./start-all.sh
```

---

## 🔧 Đã fix lỗi

### ✅ Lỗi Placement Test đã được fix

**Trước:**
```
❌ Error 500: Please create user via /api/dev/users first (local mode)
```

**Sau khi fix:**
```
✓ User sẽ tự động được tạo khi bắt đầu placement test
✓ Không cần tạo user thủ công nữa
```

**Code đã thay đổi:**
```java
// Auto-create user nếu chưa tồn tại
private void ensureUserExists(Long userId) {
    if (!localDataStore.userExists(userId)) {
        localDataStore.getOrCreateUser(userId, 
            "user" + userId + "@test.com", 
            "Test User " + userId);
    }
}
```

---

## 📊 Trạng thái hệ thống

✅ **Backend:** http://localhost:8080
- Spring DevTools: **ENABLED**
- Hot Reload: **ACTIVE**

✅ **Frontend:** http://localhost:3000
- Flask Debug Mode: **ENABLED**
- Auto Reload: **ACTIVE**

---

## 🎮 Test ngay

1. Mở trình duyệt: http://localhost:3000/placement-test
2. Chọn môn học bất kỳ
3. Bắt đầu làm bài test
4. ✅ **Không còn lỗi 500 nữa!**

---

## 💡 Tips

### Khi sửa Java code:
- Save file → Đợi 2-3s → Reload browser

### Khi sửa HTML/CSS/JS:
- Save file → Reload browser ngay

### Nếu thay đổi không có hiệu lực:
1. Hard refresh: **Cmd+Shift+R** (Mac) hoặc **Ctrl+Shift+R** (Windows)
2. Clear cache trong DevTools (F12 → Network → Disable cache)
3. Nếu vẫn không được, restart server

---

## 🎉 Hoàn thành!

Bây giờ bạn có thể code và test nhanh hơn rất nhiều! 🚀
