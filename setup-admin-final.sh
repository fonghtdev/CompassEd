#!/bin/bash

echo "🔧 THIẾT LẬP ADMIN - BACKEND ĐÃ KẾT NỐI MYSQL"
echo "════════════════════════════════════════════════════════════"

# Kiểm tra backend
if ! lsof -ti:8080 > /dev/null; then
    echo "❌ Backend không chạy! Khởi động trước:"
    echo "   cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api"
    echo "   ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Backend đang chạy trên port 8080"
echo ""

# Test MySQL connection
echo "🔍 Test kết nối MySQL..."
if ! curl -s http://localhost:8080/api/subjects > /dev/null; then
    echo "❌ Backend chưa kết nối API! Chờ thêm..."
    exit 1
fi

echo "✅ Backend API hoạt động"
echo ""

# Tạo admin user
echo "🔑 Tạo admin user..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Admin","email":"admin2026@compassed.com","password":"123456"}')

if echo "$RESPONSE" | grep -q '"id"'; then
    USER_ID=$(echo "$RESPONSE" | jq -r '.user.id')
    echo "✅ User tạo thành công với ID: $USER_ID"
    
    # Cập nhật role trong database
    echo "🔧 Cập nhật role ADMIN trong database..."
    mysql -u root -proot compassed_db -e "UPDATE users SET role='ADMIN' WHERE id=$USER_ID;" 2>/dev/null
    
    echo "📊 Kiểm tra database:"
    mysql -u root -proot compassed_db -e "SELECT id, email, role FROM users WHERE id=$USER_ID;" 2>/dev/null | tail -1
    
    echo ""
    echo "⚠️  QUAN TRỌNG: Phải RESTART backend để role có hiệu lực!"
    echo ""
    echo "🔄 Restart backend:"
    echo "   1. Ctrl+C trong terminal backend"
    echo "   2. ./mvnw spring-boot:run"
    echo ""
    echo "🎯 Sau khi restart, đăng nhập tại:"
    echo "   URL: http://localhost:3000/admin-login"
    echo "   Email: admin2026@compassed.com"
    echo "   Password: 123456"
    
elif echo "$RESPONSE" | grep -q "already exists"; then
    echo "📧 Email đã tồn tại, thử đăng nhập:"
    echo "   Email: admin2026@compassed.com"  
    echo "   Password: 123456"
    
    # Test login
    echo ""
    echo "🧪 Test đăng nhập..."
    LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"email":"admin2026@compassed.com","password":"123456"}')
    
    if echo "$LOGIN_RESP" | grep -q '"role":"ADMIN"'; then
        echo "✅ ĐÃ HOÀN TẤT! Có thể đăng nhập admin ngay!"
        echo "   URL: http://localhost:3000/admin-login"
    else
        echo "⚠️  Role chưa có hiệu lực, cần restart backend"
    fi
    
else
    echo "❌ Lỗi tạo user:"
    echo "$RESPONSE"
fi

echo ""
echo "════════════════════════════════════════════════════════════"