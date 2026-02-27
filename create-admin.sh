#!/bin/bash

clear

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "        🔐 TẠO TÀI KHOẢN ADMIN ĐỠN GIẢN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 1. Kiểm tra MySQL
echo "1️⃣  Kiểm tra MySQL..."
if ! mysql -u root -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ MySQL không chạy!"
    echo "Chạy: brew services start mysql"
    exit 1
fi
echo "✅ MySQL OK"

# 2. Kiểm tra Backend
echo ""
echo "2️⃣  Kiểm tra Backend..."
if ! lsof -ti:8080 > /dev/null 2>&1; then
    echo "❌ Backend chưa chạy!"
    echo "Chạy: cd BE/compassed-api && ./mvnw spring-boot:run &"
    exit 1
fi
echo "✅ Backend OK"

# 3. Đăng ký user mới
echo ""
echo "3️⃣  Đăng ký tài khoản admin..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Administrator",
    "email": "admin@ed.vn",
    "password": "Admin@123"
  }')

USER_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

if [ -z "$USER_ID" ]; then
    echo "❌ Không thể đăng ký user!"
    echo "Response: $RESPONSE"
    exit 1
fi

echo "✅ Đã tạo user ID: $USER_ID"

# 4. Set role ADMIN
echo ""
echo "4️⃣  Set role ADMIN..."
mysql -u root -proot compassed_db << EOF 2>/dev/null
UPDATE users SET role='ADMIN' WHERE id=$USER_ID;
EOF

echo "✅ Đã set role ADMIN"

# 5. Verify
echo ""
echo "5️⃣  Kiểm tra kết quả..."
mysql -u root -proot compassed_db << EOF 2>/dev/null
SELECT id, email, full_name, role FROM users WHERE id=$USER_ID;
EOF

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ HOÀN TẤT!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 THÔNG TIN ĐĂNG NHẬP:"
echo "   Email:    admin@ed.vn"
echo "   Password: Admin@123"
echo "   Role:     ADMIN"
echo ""
echo "⚠️  QUAN TRỌNG: Cần RESTART backend để role có hiệu lực!"
echo ""
echo "📝 Chạy lệnh sau:"
echo "   pkill -9 -f 'spring-boot:run'"
echo "   sleep 2"
echo "   cd BE/compassed-api && ./mvnw spring-boot:run &"
echo ""
echo "🌐 Sau đó đăng nhập tại:"
echo "   http://localhost:3000/admin-login"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
