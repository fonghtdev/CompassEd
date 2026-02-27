#!/bin/bash

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 SETUP ADMIN ACCOUNT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Kiểm tra backend
if ! lsof -ti:8080 > /dev/null 2>&1; then
    echo "❌ Backend chưa chạy!"
    echo "Chạy: cd BE/compassed-api && ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Backend đang chạy"

# Đợi backend sẵn sàng
sleep 2

# 1. Đăng ký tài khoản admin
echo ""
echo "📝 Đăng ký tài khoản admin..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin User",
    "email": "admin@compassed.com",
    "password": "admin123"
  }')

echo "$REGISTER_RESPONSE" | jq '.' 2>/dev/null || echo "$REGISTER_RESPONSE"

USER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.user.id' 2>/dev/null)

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
    echo "⚠️  Tài khoản có thể đã tồn tại. Đang thử login..."
    
    # Login để lấy token
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "email": "admin@compassed.com",
        "password": "admin123"
      }')
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token' 2>/dev/null)
    USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.id' 2>/dev/null)
else
    TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.token' 2>/dev/null)
fi

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "❌ Không thể đăng ký/đăng nhập!"
    exit 1
fi

echo "✅ Token: ${TOKEN:0:30}..."
echo "✅ User ID: $USER_ID"

# 2. Cập nhật role thành ADMIN
echo ""
echo "🔐 Đang set role ADMIN..."
UPDATE_RESPONSE=$(curl -s -X PUT "http://localhost:8080/api/admin/users/$USER_ID/role?role=ADMIN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json")

echo "$UPDATE_RESPONSE" | jq '.' 2>/dev/null || echo "$UPDATE_RESPONSE"

# 3. Verify trong database
echo ""
echo "🔍 Kiểm tra trong database..."
mysql -u root -proot compassed_db -e "UPDATE users SET role='ADMIN' WHERE id=$USER_ID;" 2>/dev/null
mysql -u root -proot compassed_db -e "SELECT id, email, fullName, role FROM users WHERE id=$USER_ID;" 2>/dev/null

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ SETUP HOÀN TẤT!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 THÔNG TIN ĐĂNG NHẬP ADMIN:"
echo "   Email:    admin@compassed.com"
echo "   Password: admin123"
echo "   Role:     ADMIN"
echo ""
echo "🌐 TRUY CẬP TRANG ADMIN:"
echo "   1. Mở terminal mới và chạy frontend:"
echo "      cd /Users/hoangngoctinh/compassED/ED/FE"
echo "      python3 Extensions.py"
echo ""
echo "   2. Mở browser và đăng nhập tại:"
echo "      http://localhost:3000/auth"
echo ""
echo "   3. Sau khi đăng nhập, truy cập:"
echo "      http://localhost:3000/admin-dashboard"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
