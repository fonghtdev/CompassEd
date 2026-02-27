#!/bin/bash

clear

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 KHỞI ĐỘNG ADMIN DASHBOARD"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Kiểm tra backend
echo ""
echo "🔍 Kiểm tra Backend..."
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "✅ Backend đang chạy trên port 8080"
else
    echo "❌ Backend chưa chạy!"
    echo "📌 Mở terminal mới và chạy:"
    echo "   cd BE/compassed-api && ./mvnw spring-boot:run"
    exit 1
fi

# Kiểm tra MySQL
echo ""
echo "🔍 Kiểm tra MySQL..."
if mysql -u root -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ MySQL đang chạy"
else
    echo "❌ MySQL chưa chạy!"
    echo "📌 Chạy: brew services start mysql"
    exit 1
fi

# Kiểm tra user ADMIN
echo ""
echo "🔍 Kiểm tra tài khoản ADMIN..."
ADMIN_COUNT=$(mysql -u root -proot compassed_db -se "SELECT COUNT(*) FROM users WHERE email='admin@compassed.com' AND role='ADMIN';" 2>/dev/null)

if [ "$ADMIN_COUNT" = "1" ]; then
    echo "✅ Tài khoản ADMIN đã tồn tại"
else
    echo "⚠️  Tạo tài khoản ADMIN..."
    mysql -u root -proot compassed_db << 'EOF' 2>/dev/null
UPDATE users SET role='ADMIN' WHERE email='admin@compassed.com';
EOF
    echo "✅ Đã set role ADMIN"
fi

# Kill frontend cũ nếu có
echo ""
echo "🧹 Dọn dẹp frontend cũ..."
pkill -f "Extensions.py" 2>/dev/null && echo "✅ Đã stop frontend cũ" || echo "✅ Không có frontend cũ"

sleep 1

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ SẴN SÀNG KHỞI ĐỘNG FRONTEND"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 THÔNG TIN ĐĂNG NHẬP:"
echo "   Email:    admin@compassed.com"
echo "   Password: admin123"
echo "   Role:     ADMIN"
echo ""
echo "🌐 SAU KHI FRONTEND START, TRUY CẬP:"
echo "   1. Đăng nhập:      http://localhost:3000/auth"
echo "   2. Admin Dashboard: http://localhost:3000/admin-dashboard"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🚀 Đang khởi động Frontend..."
echo ""

# Start frontend
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py
