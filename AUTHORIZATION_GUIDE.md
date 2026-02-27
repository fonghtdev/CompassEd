# 🔐 Hướng Dẫn Test Phân Quyền

## ✅ Đã triển khai:

### 1. **Roles**:
- `USER` - Người dùng thường (mặc định khi đăng ký)
- `ADMIN` - Quản trị viên (quyền cao nhất)

### 2. **Annotation `@RequireRole`**:
```java
@GetMapping("/admin/stats")
@RequireRole(UserRole.ADMIN)  // CHỈ ADMIN mới truy cập được
public ResponseEntity<?> getStats() { ... }
```

### 3. **Interceptor tự động check quyền** trên mọi endpoint có `@RequireRole`

---

## 🧪 CÁCH TEST

### **Bước 1: Tạo Admin User**

```bash
curl -X POST http://localhost:8080/api/dev/create-admin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@compassed.com",
    "password": "admin123",
    "fullName": "Admin User"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "✅ Đã tạo admin user thành công!",
  "user": {
    "id": 1,
    "email": "admin@compassed.com",
    "fullName": "Admin User",
    "role": "ADMIN"
  }
}
```

---

### **Bước 2: Tạo User Thường (qua đăng ký bình thường)**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@compassed.com",
    "password": "user123",
    "fullName": "Normal User"
  }'
```

**Response:**
```json
{
  "token": "1",
  "user": {
    "id": 1,
    "email": "user@compassed.com",
    "fullName": "Normal User",
    "role": "USER"  ← Role mặc định
  }
}
```

---

### **Bước 3: Test với USER thường (KHÔNG có quyền ADMIN)**

#### a) Login USER:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@compassed.com",
    "password": "user123"
  }'
```

**Lưu token** (giả sử: `"token": "2"`)

#### b) Truy cập endpoint CHỈ ADMIN:
```bash
curl -X GET http://localhost:8080/api/admin/test \
  -H "Authorization: Bearer 2"
```

**❌ Kết quả (403 Forbidden):**
```json
{
  "error": "Forbidden - You don't have permission to access this resource"
}
```

---

### **Bước 4: Test với ADMIN (CÓ quyền)**

#### a) Login ADMIN:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@compassed.com",
    "password": "admin123"
  }'
```

**Lưu token** (giả sử: `"token": "1"`)

#### b) Truy cập endpoint CHỈ ADMIN:
```bash
curl -X GET http://localhost:8080/api/admin/test \
  -H "Authorization: Bearer 1"
```

**✅ Kết quả (200 OK):**
```json
{
  "message": "✅ Bạn có quyền ADMIN!",
  "access": "granted"
}
```

---

### **Bước 5: Set user thường thành ADMIN**

```bash
curl -X POST http://localhost:8080/api/dev/set-admin/2 \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "success": true,
  "message": "✅ Đã set user user@compassed.com làm ADMIN!",
  "user": {
    "id": 2,
    "email": "user@compassed.com",
    "role": "ADMIN"
  }
}
```

Giờ user này **CÓ thể truy cập** `/api/admin/*` endpoints!

---

## 📋 Danh Sách Endpoints

| Endpoint | Method | Role Required | Description |
|----------|--------|---------------|-------------|
| `/api/auth/register` | POST | None | Đăng ký (auto role=USER) |
| `/api/auth/login` | POST | None | Đăng nhập |
| `/api/auth/me` | GET | None | Lấy thông tin user |
| `/api/admin/stats` | GET | **ADMIN** | Xem thống kê |
| `/api/admin/test` | GET | **ADMIN** | Test quyền admin |
| `/api/dev/create-admin` | POST | None (dev only) | Tạo admin user |
| `/api/dev/set-admin/{userId}` | POST | None (dev only) | Set user thành admin |

---

## 🎯 Cách Dùng Trong Frontend

### JavaScript Example:

```javascript
// Lấy user info sau khi login
const response = await api("/api/auth/me", "GET", null, true);
console.log("Role:", response.role); // "USER" hoặc "ADMIN"

// Hiển thị menu admin chỉ khi role = ADMIN
if (response.role === "ADMIN") {
  document.getElementById("admin-menu").style.display = "block";
}

// Truy cập endpoint admin
try {
  const stats = await api("/api/admin/stats", "GET", null, true);
  console.log("Stats:", stats);
} catch (error) {
  // Nếu không có quyền: 403 Forbidden
  console.error("Không có quyền truy cập:", error);
}
```

---

## 🔒 Bảo Mật

1. **Endpoint `/api/dev/*` chỉ dùng cho DEV/TEST** - trong production nên disable hoặc yêu cầu auth
2. **Token hiện tại đơn giản (userId)** - trong production nên dùng JWT
3. **RoleInterceptor** tự động check mọi endpoint có `@RequireRole`
4. **Endpoint `/api/auth/*` KHÔNG check role** (cho phép đăng ký/đăng nhập)

---

## ✨ Next Steps

1. Thêm role **TEACHER**, **STUDENT** nếu cần
2. Thêm permission chi tiết hơn (e.g., `CAN_EDIT_COURSE`, `CAN_VIEW_REPORTS`)
3. Implement JWT thay vì token đơn giản
4. Tạo UI admin panel trên frontend
