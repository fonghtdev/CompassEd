#!/bin/bash

echo "🚀 KHỞI ĐỘNG HOÀN CHỈNH - CompassED System"
echo "════════════════════════════════════════════════════════════"

# Dừng tất cả processes cũ
echo "🛑 Dừng processes cũ..."
lsof -ti:3000 | xargs kill -9 2>/dev/null
lsof -ti:8080 | xargs kill -9 2>/dev/null
sleep 3

# 1. Khởi động Frontend
echo "1️⃣ Khởi động Frontend (Flask)..."
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend PID: $FRONTEND_PID"
sleep 5

# Kiểm tra frontend
if lsof -ti:3000 > /dev/null; then
    echo "   ✅ Frontend đã chạy trên http://localhost:3000"
else
    echo "   ❌ Frontend không khởi động được"
    exit 1
fi

# 2. Khởi động Backend với MySQL
echo ""
echo "2️⃣ Khởi động Backend với MySQL..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api

# Thiết lập biến môi trường
export SPRING_PROFILES_ACTIVE=mysql
export MYSQL_URL="jdbc:mysql://localhost:3306/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER="root"
export MYSQL_PASSWORD="root"
export JWT_SECRET="this-is-a-very-strong-jwt-secret-min-32"
export ADMIN_EMAIL="admin@compassed.com"
export ADMIN_PASSWORD="123456"

echo "   ⚙️  Profile: mysql"
echo "   ⚙️  Database: compassed"
echo "   ⚙️  Admin: admin@compassed.com / 123456"

./mvnw spring-boot:run > /tmp/backend-mysql.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"

# Đợi backend khởi động
echo "   ⏰ Chờ backend khởi động (45 giây)..."
for i in {1..45}; do
    if lsof -ti:8080 > /dev/null 2>&1; then
        break
    fi
    echo -n "."
    sleep 1
done
echo ""

# 3. Kiểm tra kết quả
echo ""
echo "📊 KIỂM TRA KẾT QUẢ:"
echo "════════════════════════════════════════════════════════════"

if lsof -ti:8080 > /dev/null; then
    echo "✅ Backend: http://localhost:8080"
    
    # Test API
    if curl -s http://localhost:8080/api/subjects > /dev/null; then
        echo "✅ API hoạt động"
    else
        echo "❌ API không hoạt động"
    fi
else
    echo "❌ Backend không chạy"
    echo "   Log backend:"
    tail -10 /tmp/backend-mysql.log
    exit 1
fi

if lsof -ti:3000 > /dev/null; then
    echo "✅ Frontend: http://localhost:3000"
else
    echo "❌ Frontend không chạy"
fi

echo ""
echo "🎯 TẠO ADMIN ACCOUNT..."
echo "════════════════════════════════════════════════════════════"

# Tạo admin account
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"fullName":"Admin","email":"admin@compassed.com","password":"123456"}' \
    2>/dev/null)

if echo "$RESPONSE" | grep -q '"id"'; then
    USER_ID=$(echo "$RESPONSE" | jq -r '.user.id' 2>/dev/null)
    echo "✅ Admin user tạo thành công (ID: $USER_ID)"
    
    # Cập nhật role ADMIN
    mysql -u root -proot compassed -e "UPDATE users SET role='ADMIN' WHERE id=$USER_ID;" 2>/dev/null
    echo "✅ Role đã cập nhật thành ADMIN"
    
elif echo "$RESPONSE" | grep -q "already exists"; then
    echo "ℹ️  Admin account đã tồn tại"
else
    echo "⚠️  Phản hồi: $RESPONSE"
fi

echo ""
echo "🎉 HOÀN TẤT KHỞI ĐỘNG!"
echo "════════════════════════════════════════════════════════════"
echo "🌐 Truy cập:"
echo "   • Trang chủ:     http://localhost:3000"
echo "   • Admin Login:   http://localhost:3000/admin-login"
echo "   • Backend API:   http://localhost:8080/api"
echo ""
echo "🔐 Đăng nhập Admin:"
echo "   Email:    admin@compassed.com"
echo "   Password: 123456"
echo ""
echo "⚠️  LƯU Ý: Admin role có hiệu lực NGAY LẬP TỨC với profile mysql!"