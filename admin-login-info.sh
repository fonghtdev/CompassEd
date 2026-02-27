#!/bin/bash

clear

cat << 'EOF'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                    🔐 ADMIN LOGIN - HƯỚNG DẪN SỬ DỤNG
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✅ ĐÃ TẠO TRANG ĐĂNG NHẬP ADMIN RIÊNG!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📍 URL TRANG ADMIN:

   🔹 Đăng nhập Admin:  http://localhost:3000/admin-login
   🔹 Đăng nhập thường: http://localhost:3000/auth
   🔹 Admin Dashboard:   http://localhost:3000/admin-dashboard

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 THÔNG TIN ĐĂNG NHẬP ADMIN:

   Email:    admin@compassed.com
   Password: admin123
   Role:     ADMIN

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🎨 GIAO DIỆN ADMIN LOGIN:

   ✅ Thiết kế riêng biệt với gradient đẹp mắt
   ✅ Icon shield (khiên) thể hiện bảo mật
   ✅ Form đăng nhập chuyên dụng cho admin
   ✅ Tự động kiểm tra role ADMIN
   ✅ Chặn user thường không có quyền
   ✅ Link quay về trang đăng nhập thường

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🚀 CÁCH SỬ DỤNG:

1️⃣  MỞ BROWSER:
    http://localhost:3000/admin-login

2️⃣  NHẬP THÔNG TIN:
    - Email: admin@compassed.com
    - Password: admin123

3️⃣  NHẤN "ĐĂNG NHẬP ADMIN"
    - Hệ thống sẽ kiểm tra role
    - Nếu có role ADMIN → Chuyển đến Admin Dashboard
    - Nếu không có role ADMIN → Hiển thị lỗi "Bạn không có quyền"

4️⃣  ADMIN DASHBOARD:
    - Quản lý Question Bank
    - Quản lý Users
    - Phân quyền USER/ADMIN

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🔒 BẢO MẬT:

   ✅ Trang admin login RIÊNG, không nhầm lẫn với user thường
   ✅ Kiểm tra role ADMIN trước khi cho phép truy cập
   ✅ JWT token được lưu an toàn trong localStorage
   ✅ Auto-redirect nếu đã đăng nhập

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📁 FILES ĐÃ TẠO:

   ✅ FE/template/admin/admin-login.html   → Trang đăng nhập admin
   ✅ FE/css/adminLogin.css                → CSS cho admin login
   ✅ FE/Extensions.py                      → Thêm route /admin-login

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚙️  KIỂM TRA HỆ THỐNG:

EOF

# Check backend
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "   ✅ Backend: http://localhost:8080"
else
    echo "   ❌ Backend chưa chạy"
fi

# Check frontend
if lsof -ti:3000 > /dev/null 2>&1; then
    echo "   ✅ Frontend: http://localhost:3000"
else
    echo "   ❌ Frontend chưa chạy"
fi

# Check MySQL
if mysql -u root -proot -e "SELECT 1;" > /dev/null 2>&1; then
    echo "   ✅ MySQL: Running"
else
    echo "   ❌ MySQL chưa chạy"
fi

# Check admin user
ADMIN_EXISTS=$(mysql -u root -proot compassed_db -se "SELECT COUNT(*) FROM users WHERE email='admin@compassed.com' AND role='ADMIN';" 2>/dev/null)
if [ "$ADMIN_EXISTS" = "1" ]; then
    echo "   ✅ Admin user: admin@compassed.com (ADMIN)"
else
    echo "   ⚠️  Admin user chưa có role ADMIN"
fi

cat << 'EOF'

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🌐 TRUY CẬP NGAY:

   👉 http://localhost:3000/admin-login

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📚 TIP: 

   • Trang /admin-login chỉ dành cho ADMIN
   • Trang /auth dành cho user thường
   • Nếu user thường cố đăng nhập vào /admin-login sẽ bị chặn
   • Admin có thể đăng nhập từ cả 2 trang

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

EOF
