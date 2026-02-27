#!/bin/bash

# Script để test login và lấy token ADMIN

BACKEND_URL="http://localhost:8080"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 TEST ADMIN LOGIN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Kiểm tra backend
if ! lsof -i:8080 > /dev/null 2>&1; then
    echo "❌ Backend chưa chạy!"
    echo "   Hãy chạy: cd BE/compassed-api && ./mvnw spring-boot:run"
    exit 1
fi

echo "✅ Backend đang chạy"
echo ""

# Lấy email và password từ arguments hoặc dùng default
EMAIL=${1:-"admin@compassed.com"}
PASSWORD=${2:-"admin123"}

echo "📧 Email: $EMAIL"
echo "🔑 Password: $PASSWORD"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔄 Đang login..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Login và lưu response
RESPONSE=$(curl -s -X POST $BACKEND_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

# Kiểm tra response có error không
if echo "$RESPONSE" | grep -q "error"; then
    echo "❌ LOGIN THẤT BẠI!"
    echo ""
    echo "Response:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "💡 GỢI Ý:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "1. Kiểm tra email/password đúng chưa"
    echo "2. Kiểm tra user có trong database:"
    echo "   mysql -u root -proot compassed_db -e \"SELECT id, email, role FROM users WHERE email = '$EMAIL';\""
    echo ""
    echo "3. Reset password nếu quên:"
    echo "   # Cần hash password bằng BCrypt trước"
    echo ""
    exit 1
fi

# Parse token và user info
TOKEN=$(echo "$RESPONSE" | jq -r '.token' 2>/dev/null)
USER_ID=$(echo "$RESPONSE" | jq -r '.user.id' 2>/dev/null)
USER_EMAIL=$(echo "$RESPONSE" | jq -r '.user.email' 2>/dev/null)
USER_NAME=$(echo "$RESPONSE" | jq -r '.user.fullName' 2>/dev/null)
USER_ROLE=$(echo "$RESPONSE" | jq -r '.user.role' 2>/dev/null)

echo "✅ LOGIN THÀNH CÔNG!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "👤 USER INFO:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "ID:        $USER_ID"
echo "Email:     $USER_EMAIL"
echo "Name:      $USER_NAME"
echo "Role:      $USER_ROLE"
echo ""

# Kiểm tra role
if [ "$USER_ROLE" != "ADMIN" ]; then
    echo "⚠️  WARNING: User không phải ADMIN!"
    echo ""
    echo "User này không thể truy cập admin endpoints."
    echo "Để set role ADMIN, chạy:"
    echo "  bash set-admin-role.sh $USER_ID"
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔑 TOKEN:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "$TOKEN"
echo ""

# Lưu token vào file để dùng sau
echo "$TOKEN" > /tmp/admin-token.txt
echo "💾 Token đã lưu vào: /tmp/admin-token.txt"
echo ""

# Export token để dùng trong terminal
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📦 SỬ DỤNG TOKEN:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Trong terminal này, chạy:"
echo "  export TOKEN=\"$TOKEN\""
echo ""
echo "Hoặc:"
echo "  TOKEN=\$(cat /tmp/admin-token.txt)"
echo ""

# Test một vài endpoints
if [ "$USER_ROLE" = "ADMIN" ]; then
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🧪 TEST ADMIN ENDPOINTS:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    # Test users endpoint
    echo "1️⃣  Testing GET /api/admin/users..."
    USERS_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" $BACKEND_URL/api/admin/users)
    
    if echo "$USERS_RESPONSE" | grep -q "error"; then
        echo "   ❌ Failed"
    else
        USERS_COUNT=$(echo "$USERS_RESPONSE" | jq '. | length' 2>/dev/null)
        echo "   ✅ Success - Found $USERS_COUNT users"
    fi
    echo ""
    
    # Test question bank endpoint
    echo "2️⃣  Testing GET /api/admin/question-bank..."
    QB_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" $BACKEND_URL/api/admin/question-bank)
    
    if echo "$QB_RESPONSE" | grep -q "error"; then
        echo "   ❌ Failed"
    else
        QB_COUNT=$(echo "$QB_RESPONSE" | jq '. | length' 2>/dev/null)
        echo "   ✅ Success - Found $QB_COUNT questions"
    fi
    echo ""
    
    # Test subjects endpoint
    echo "3️⃣  Testing GET /api/admin/subjects..."
    SUBJECTS_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" $BACKEND_URL/api/admin/subjects)
    
    if echo "$SUBJECTS_RESPONSE" | grep -q "error"; then
        echo "   ❌ Failed"
    else
        SUBJECTS_COUNT=$(echo "$SUBJECTS_RESPONSE" | jq '. | length' 2>/dev/null)
        echo "   ✅ Success - Found $SUBJECTS_COUNT subjects"
    fi
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 ADMIN DASHBOARD:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Truy cập: http://localhost:3000/admin-dashboard"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
