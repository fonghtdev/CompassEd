#!/bin/bash

clear

cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        🔧 FIX VÀ TẠO TÀI KHOẢN ADMIN - HOÀN CHỈNH
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📌 VẤN ĐỀ: admin@compassed.com không có quyền ADMIN
📌 NGUYÊN NHÂN: Profile 'local' không dùng MySQL, data không lưu
📌 GIẢI PHÁP: Enable MySQL trong application-local.yml

EOF

echo "1️⃣  Stop các process đang chạy..."
pkill -9 -f "spring-boot:run" 2>/dev/null
pkill -9 -f "Extensions.py" 2>/dev/null
sleep 2
echo "✅ Đã stop"

echo ""
echo "2️⃣  Xóa user admin cũ trong database..."
mysql -u root -proot compassed_db << 'MYSQL' 2>/dev/null
DELETE FROM users WHERE email='superadmin@compassed.com';
MYSQL
echo "✅ Đã xóa"

echo ""
echo "3️⃣  Start Backend với MySQL..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run > /tmp/backend-final.log 2>&1 &
BACKEND_PID=$!

echo "⏳ Đang khởi động backend (PID: $BACKEND_PID)..."
sleep 15

# Kiểm tra backend
if curl -s http://localhost:8080/api/subjects > /dev/null 2>&1; then
    echo "✅ Backend sẵn sàng"
else
    echo "❌ Backend chưa sẵn sàng, đợi thêm..."
    sleep 10
fi

echo ""
echo "4️⃣  Đăng ký tài khoản admin mới..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Super Admin",
    "email": "superadmin@compassed.com",
    "password": "admin123"
  }')

echo "$REGISTER_RESPONSE" | jq '.' 2>/dev/null || echo "$REGISTER_RESPONSE"

USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.user.id' 2>/dev/null)

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
    echo "❌ Không thể tạo user!"
    echo "Kiểm tra log: tail -50 /tmp/backend-final.log"
    exit 1
fi

echo "✅ User ID: $USER_ID"

echo ""
echo "5️⃣  Set role ADMIN trong database..."
mysql -u root -proot compassed_db << MYSQL 2>/dev/null
UPDATE users SET role='ADMIN' WHERE id=$USER_ID;
SELECT id, email, full_name, role FROM users WHERE id=$USER_ID;
MYSQL

echo ""
echo "6️⃣  Verify bằng login API..."
sleep 2
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "superadmin@compassed.com",
    "password": "admin123"
  }')

ROLE=$(echo "$LOGIN_RESPONSE" | jq -r '.user.role' 2>/dev/null)
echo "Role trả về từ API: $ROLE"

if [ "$ROLE" = "ADMIN" ]; then
    echo "✅ THÀNH CÔNG! Role = ADMIN"
else
    echo "⚠️  Role vẫn là: $ROLE"
    echo "⚠️  QUAN TRỌNG: Cần RESTART backend để role có hiệu lực!"
    echo ""
    echo "Chạy lệnh:"
    echo "  pkill -9 -f 'spring-boot:run'"
    echo "  cd BE/compassed-api && ./mvnw spring-boot:run &"
fi

echo ""
echo "7️⃣  Start Frontend..."
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py > /tmp/frontend-final.log 2>&1 &
FRONTEND_PID=$!
sleep 3
echo "✅ Frontend started (PID: $FRONTEND_PID)"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ HOÀN TẤT!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 THÔNG TIN ĐĂNG NHẬP ADMIN:"
echo "   Email:    superadmin@compassed.com"
echo "   Password: admin123"
echo "   Role:     ADMIN"
echo ""
echo "🌐 TRUY CẬP TRANG ADMIN:"
echo "   http://localhost:3000/admin-login"
echo ""
echo "⚠️  NẾU VẪN KHÔNG CÓ QUYỀN ADMIN:"
echo "   1. Xóa localStorage trong browser (F12 > Application > Local Storage > Clear)"
echo "   2. Logout và login lại"
echo "   3. Hoặc restart backend:"
echo "      pkill -9 -f 'spring-boot:run'"
echo "      cd BE/compassed-api && ./mvnw spring-boot:run &"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
