#!/bin/bash

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 TẠO TÀI KHOẢN ADMIN MỚI"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Xóa user cũ nếu có
echo "🗑️  Xóa user admin cũ (nếu có)..."
mysql -u root -proot compassed_db << 'EOF' 2>/dev/null
DELETE FROM users WHERE email='admin@compassed.com';
EOF

echo "✅ Đã xóa"

# Đợi 2 giây
sleep 2

# Đăng ký user mới
echo ""
echo "📝 Đăng ký user admin mới..."
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin User",
    "email": "admin@compassed.com",
    "password": "admin123"
  }')

echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

# Lấy user ID
USER_ID=$(echo "$RESPONSE" | jq -r '.user.id' 2>/dev/null)

if [ -z "$USER_ID" ] || [ "$USER_ID" = "null" ]; then
    echo "❌ Không thể tạo user!"
    exit 1
fi

echo "✅ User ID: $USER_ID"

# Set role ADMIN trong database
echo ""
echo "🔐 Set role ADMIN trong database..."
mysql -u root -proot compassed_db << EOF 2>/dev/null
UPDATE users SET role='ADMIN' WHERE id=$USER_ID;
SELECT id, email, full_name, role FROM users WHERE id=$USER_ID;
EOF

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ HOÀN TẤT!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 THÔNG TIN ĐĂNG NHẬP:"
echo "   Email:    admin@compassed.com"
echo "   Password: admin123"
echo "   Role:     ADMIN"
echo ""
echo "⚠️  LƯU Ý: Bạn cần LOGOUT và LOGIN LẠI để role ADMIN có hiệu lực!"
echo ""
echo "🌐 TRUY CẬP:"
echo "   http://localhost:3000/admin-login"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
