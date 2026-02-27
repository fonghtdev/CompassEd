#!/bin/bash

echo "🔧 TẠO ADMIN - BƯỚC CUỐI CÙNG"
echo "════════════════════════════════════════════════════════════"

# Tạo admin user
echo "🔑 Tạo admin user..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Admin System","email":"sysadmin@ed.com","password":"123456"}' \
  2>/dev/null)

echo "Response: $RESPONSE"

if echo "$RESPONSE" | grep -q '"id"'; then
  USER_ID=$(echo "$RESPONSE" | jq -r '.user.id' 2>/dev/null)
  
  if [ -n "$USER_ID" ] && [ "$USER_ID" != "null" ]; then
    echo ""
    echo "✅ User đã tạo với ID: $USER_ID"
    
    # Cập nhật role ADMIN
    echo "🔧 Cập nhật role ADMIN..."
    mysql -u root -proot compassed_db << EOF 2>/dev/null
UPDATE users SET role='ADMIN' WHERE id=$USER_ID;
SELECT id, email, role FROM users WHERE id=$USER_ID;
EOF
    
    echo ""
    echo "════════════════════════════════════════════════════════════"
    echo "🚨 BƯỚC CUỐI - RESTART BACKEND:"
    echo ""
    echo "1. Trong terminal backend, nhấn Ctrl+C"
    echo "2. Chạy lại: ./mvnw spring-boot:run"
    echo "3. Chờ thông báo 'Started CompassedApiApplication'"
    echo ""
    echo "🎯 SAU ĐÓ ĐĂNG NHẬP:"
    echo "   URL:      http://localhost:3000/admin-login"
    echo "   Email:    sysadmin@ed.com"
    echo "   Password: 123456"
    echo "════════════════════════════════════════════════════════════"
  else
    echo "❌ Không lấy được user ID từ response"
  fi
  
elif echo "$RESPONSE" | grep -q "already exists"; then
  echo ""
  echo "📧 Email đã tồn tại! Thử đăng nhập ngay:"
  echo "   Email: sysadmin@ed.com"
  echo "   Password: 123456"
  
else
  echo "❌ Lỗi tạo user: $RESPONSE"
fi

echo ""
echo "ℹ️  Nếu vẫn báo lỗi quyền, nghĩa là backend chưa restart!"