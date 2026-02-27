#!/bin/bash

echo "🔧 KHỞI ĐỘNG LẠI BACKEND VỚI MYSQL"
echo "════════════════════════════════════════════════════════════"

# Dừng backend hiện tại
echo "🛑 Dừng backend hiện tại..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
sleep 3

# Kiểm tra database compassed có tồn tại không
echo "📊 Kiểm tra database 'compassed'..."
if ! mysql -u root -proot -e "USE compassed;" 2>/dev/null; then
    echo "🔧 Tạo database 'compassed'..."
    mysql -u root -proot -e "CREATE DATABASE compassed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
fi

echo "✅ Database 'compassed' đã sẵn sàng"

# Khởi động với profile mysql
echo ""
echo "🚀 Khởi động backend với profile mysql..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api

# Thiết lập biến môi trường
export SPRING_PROFILES_ACTIVE=mysql
export MYSQL_URL="jdbc:mysql://localhost:3306/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER="root"
export MYSQL_PASSWORD="root"
export JWT_SECRET="this-is-a-very-strong-jwt-secret-min-32"
export ADMIN_EMAIL="admin@compassed.com"
export ADMIN_PASSWORD="123456"

echo "⚙️  Biến môi trường đã thiết lập:"
echo "   Profile: mysql"
echo "   Database: compassed"
echo "   Admin: admin@compassed.com / 123456"

echo ""
echo "🎯 Backend sẽ khởi động với MySQL..."
./mvnw spring-boot:run