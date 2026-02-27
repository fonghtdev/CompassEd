#!/bin/bash

echo "🛑 Dừng backend cũ..."
pkill -9 -f "spring-boot:run"
pkill -9 java
sleep 3

echo "🚀 Khởi động backend mới..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run > /tmp/backend.log 2>&1 &

echo "⏳ Chờ backend khởi động (40 giây)..."
for i in {1..40}; do
  echo -n "."
  sleep 1
done
echo ""

if lsof -ti:8080 > /dev/null 2>&1; then
  echo "✅ Backend đã chạy trên port 8080"
  echo ""
  echo "📝 Giờ tạo admin:"
  echo "   Email: admin@test.com"
  echo "   Password: 123456"
  echo ""
  
  # Đăng ký admin
  RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"fullName":"Admin","email":"admin@test.com","password":"123456"}')
  
  echo "Response: $RESPONSE"
  
  # Lấy user ID
  USER_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | cut -d: -f2)
  
  if [ -n "$USER_ID" ]; then
    echo ""
    echo "👤 User ID: $USER_ID"
    echo "🔧 Cập nhật role thành ADMIN..."
    
    mysql -u root -proot -D compassed_db -e \
      "UPDATE users SET role='ADMIN' WHERE id=$USER_ID;"
    
    echo "✅ HOÀN TẤT!"
    echo ""
    echo "═══════════════════════════════════════"
    echo "📱 ĐĂNG NHẬP ADMIN:"
    echo "   URL: http://localhost:3000/admin-login"
    echo "   Email: admin@test.com"
    echo "   Password: 123456"
    echo "═══════════════════════════════════════"
    echo ""
    echo "⚠️  LƯU Ý: Backend cần RESTART lần nữa để role có hiệu lực!"
    echo "   Chạy: bash restart-backend-final.sh"
  fi
else
  echo "❌ Backend không khởi động được!"
  echo "Xem log:"
  tail -50 /tmp/backend.log
fi
