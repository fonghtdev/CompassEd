#!/bin/bash

echo "🛑 Dừng backend..."
pkill -9 -f "spring-boot:run"
pkill -9 java
sleep 3

echo "🚀 Khởi động backend lần cuối..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run > /tmp/backend-final.log 2>&1 &

echo "⏳ Chờ 40 giây..."
for i in {1..40}; do
  echo -n "."
  sleep 1
done
echo ""

if lsof -ti:8080 > /dev/null 2>&1; then
  echo "✅ Backend đã chạy!"
  echo ""
  echo "═══════════════════════════════════════"
  echo "🎯 BÂY GIỜ ĐĂNG NHẬP:"
  echo "   http://localhost:3000/admin-login"
  echo "   Email: admin@test.com"
  echo "   Password: 123456"
  echo "═══════════════════════════════════════"
else
  echo "❌ Lỗi! Xem log:"
  tail -50 /tmp/backend-final.log
fi
