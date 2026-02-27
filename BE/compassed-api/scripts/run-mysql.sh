#!/bin/bash

# Script tương đương với run-mysql.ps1 cho macOS
# Sử dụng: bash run-mysql.sh

# Thiết lập mặc định (giống PowerShell script)
MYSQL_HOST="${1:-localhost}"
MYSQL_PORT="${2:-3307}"
MYSQL_USER="${3:-root}"
MYSQL_PASSWORD="${4:-1234}"
DATABASE="${5:-compassed}"
JWT_SECRET="${6:-this-is-a-very-strong-jwt-secret-min-32}"
ADMIN_EMAIL="${7:-}"
ADMIN_PASSWORD="${8:-}"
SERVER_PORT="${9:-8080}"

echo "🔧 THIẾT LẬP DATABASE THEO CONFIG CỦA PHONG"
echo "════════════════════════════════════════════════════════════"
echo "MySQL Host: $MYSQL_HOST"
echo "MySQL Port: $MYSQL_PORT"
echo "MySQL User: $MYSQL_USER"
echo "Database: $DATABASE"
echo "Server Port: $SERVER_PORT"
echo ""

# Tạo database nếu chưa có
echo "📊 Tạo database '$DATABASE' nếu chưa có..."
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD -e "CREATE DATABASE IF NOT EXISTS $DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Database '$DATABASE' đã sẵn sàng"
else
    echo "❌ Lỗi kết nối MySQL. Kiểm tra:"
    echo "   - MySQL có đang chạy?"
    echo "   - Port $MYSQL_PORT có đúng?"
    echo "   - Password '$MYSQL_PASSWORD' có đúng?"
    exit 1
fi

# Thiết lập biến môi trường
echo ""
echo "⚙️  Thiết lập biến môi trường..."
export SPRING_PROFILES_ACTIVE="mysql"
export MYSQL_URL="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER="$MYSQL_USER"
export MYSQL_PASSWORD="$MYSQL_PASSWORD"
export JWT_SECRET="$JWT_SECRET"
export ADMIN_EMAIL="$ADMIN_EMAIL"
export ADMIN_PASSWORD="$ADMIN_PASSWORD"
export SERVER_PORT="$SERVER_PORT"

echo "✅ Biến môi trường đã thiết lập"

# Khởi động backend
echo ""
echo "🚀 Khởi động backend trên http://localhost:$SERVER_PORT ..."
echo "════════════════════════════════════════════════════════════"

# Sử dụng mvnw cho macOS thay vì mvnw.cmd
./mvnw spring-boot:run