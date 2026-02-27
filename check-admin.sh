#!/bin/bash

clear

cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        🚀 QUICK START ADMIN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 THÔNG TIN ĐĂNG NHẬP ADMIN:

   Email:    superadmin@compassed.com
   Password: admin123
   Role:     ADMIN

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🌐 TRUY CẬP:

   Admin Login:  http://localhost:3000/admin-login
   Dashboard:    http://localhost:3000/admin-dashboard

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚙️  STATUS CHECK:

EOF

# Check Backend
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "   ✅ Backend: Running on port 8080"
else
    echo "   ❌ Backend: Not running"
    echo "   📝 Start: cd BE/compassed-api && ./mvnw spring-boot:run &"
fi

# Check Frontend
if lsof -ti:3000 > /dev/null 2>&1; then
    echo "   ✅ Frontend: Running on port 3000"
else
    echo "   ❌ Frontend: Not running"
    echo "   📝 Start: cd FE && python3 Extensions.py &"
fi

# Check MySQL
if mysql -u root -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "   ✅ MySQL: Running"
else
    echo "   ❌ MySQL: Not running"
    echo "   📝 Start: brew services start mysql"
fi

# Check Admin User
echo ""
ADMIN_COUNT=$(mysql -u root -proot compassed_db -se "SELECT COUNT(*) FROM users WHERE email='superadmin@compassed.com' AND role='ADMIN';" 2>/dev/null)

if [ "$ADMIN_COUNT" = "1" ]; then
    echo "   ✅ Admin User: superadmin@compassed.com (ADMIN role)"
    
    # Show user details
    mysql -u root -proot compassed_db -e "SELECT id, email, full_name, role FROM users WHERE email='superadmin@compassed.com';" 2>/dev/null | grep -v "Warning"
else
    echo "   ⚠️  Admin User: Not found or wrong role"
    echo "   📝 Fix: bash fix-admin-complete.sh"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 TIP: Nếu không login được, clear localStorage trong browser:"
echo "   F12 > Console > localStorage.clear(); location.reload();"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
