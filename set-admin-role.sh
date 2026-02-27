#!/bin/bash

# Script để set role ADMIN cho user
# Sử dụng: bash set-admin-role.sh <userId>

USER_ID=${1:-1}  # Default là user ID 1

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 SET ADMIN ROLE CHO USER"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📌 User ID: $USER_ID"
echo ""

# Kiểm tra backend có đang chạy không
if ! lsof -i:8080 > /dev/null 2>&1; then
    echo "❌ Backend chưa chạy! Hãy start backend trước:"
    echo "   cd BE/compassed-api && ./mvnw spring-boot:run"
    exit 1
fi

# Cách 1: Thông qua MySQL (không cần auth)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 CÁCH 1: Thông qua MySQL Database"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Kiểm tra xem có MySQL client không
if command -v mysql > /dev/null 2>&1; then
    echo "🔍 Kiểm tra user hiện tại..."
    mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users WHERE id = $USER_ID;" 2>/dev/null
    
    echo ""
    echo "🔧 Cập nhật role thành ADMIN..."
    mysql -u root -proot compassed_db -e "UPDATE users SET role = 'ADMIN' WHERE id = $USER_ID;" 2>/dev/null
    
    echo "✅ Kiểm tra lại sau khi update..."
    mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users WHERE id = $USER_ID;" 2>/dev/null
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ HOÀN TẤT!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "🌐 Bây giờ bạn có thể:"
    echo "   • Login với user ID $USER_ID"
    echo "   • Truy cập admin dashboard: http://localhost:3000/admin-dashboard"
    echo "   • Sử dụng các API admin như:"
    echo "     - GET  /api/admin/question-bank"
    echo "     - GET  /api/admin/users"
    echo "     - POST /api/admin/question-bank"
    echo ""
else
    echo "⚠️  MySQL client không có sẵn!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📊 CÁCH 2: Thông qua API (cần ADMIN token)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "⚠️  Cần có một ADMIN user khác để set role!"
    echo ""
    echo "Nếu chưa có ADMIN user nào, bạn cần:"
    echo "1. Cài MySQL client: brew install mysql-client"
    echo "2. Hoặc truy cập MySQL thủ công và chạy:"
    echo ""
    echo "   mysql -u root -proot compassed_db"
    echo "   UPDATE users SET role = 'ADMIN' WHERE id = $USER_ID;"
    echo ""
fi
