# CompassEd

Nền tảng học tập có bài kiểm tra xếp lớp (placement test), lộ trình học cá nhân hóa (có AI), mini test, final test và thanh toán gói học.

| Phần | Công nghệ | Thư mục |
|---|---|---|
| Backend | Spring Boot 4, Java 17, JWT, Flyway | `BE/compassed-api` |
| Frontend | HTML/JS tĩnh, phục vụ bằng Flask | `FE` |
| Database | MySQL 8 | `docker-compose.yml` |
| Tích hợp | Google OAuth, OpenAI, PayOS / VNPay, SMTP (Gmail) | biến môi trường |

## Chạy nhanh với Docker

```bash
cp .env.example .env     # điền MYSQL_ROOT_PASSWORD, JWT_SECRET (>= 32 ký tự), các key còn lại
docker compose up -d --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MySQL: `localhost:3307`

Tạo `JWT_SECRET`: `openssl rand -hex 32`. Backend không khởi động nếu thiếu biến này.

## Chạy cục bộ không Docker

Cần Java 17, Python 3 và MySQL 8.

```bash
# Backend
cd BE/compassed-api
export MYSQL_URL="jdbc:mysql://localhost:3306/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER=root MYSQL_PASSWORD=<mật khẩu> JWT_SECRET=<chuỗi >= 32 ký tự>
./mvnw spring-boot:run

# Frontend (terminal khác)
cd FE
python3 -m pip install -r requirements.txt
python3 Extensions.py      # mặc định cổng 3000 (đổi bằng FE_PORT), API_BASE trong FE/config.js
```

Trên Windows dùng `scripts/*.ps1`: sao chép `scripts/dev-env.example.ps1` thành `scripts/dev-env.ps1` (đã bị gitignore), điền giá trị rồi chạy `scripts/run-all.ps1`.

## Biến môi trường

Xem `.env.example`. Mọi secret đặt trong `.env` hoặc biến môi trường, **không commit**.

| Biến | Bắt buộc | Ghi chú |
|---|---|---|
| `JWT_SECRET` | Có | >= 32 ký tự, không có giá trị mặc định |
| `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD` | Có | |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Không | Nếu đặt, `DataSeeder` tạo tài khoản admin lúc khởi động |
| `OPENAI_API_KEY` | Không | Tạo lộ trình bằng AI |
| `GOOGLE_CLIENT_ID` | Không | Đăng nhập Google |
| `MAIL_*` | Không | Gửi email xác thực đăng ký |
| `PAYOS_*` | Không | Thanh toán PayOS (`PAYOS_ENABLED=true` để bật) |

## Cấu trúc

```
BE/compassed-api/   API Spring Boot (controller, service, repository, security)
  src/main/resources/db/migration/   Flyway migrations
FE/                 Trang tĩnh (template/, js/, css/) và Extensions.py (Flask)
scripts/            Script chạy trên Windows (PowerShell)
docker-compose.yml  fe + be + mysql
```

## Test

```bash
cd BE/compassed-api
JWT_SECRET=<chuỗi >= 32 ký tự> ./mvnw test
```

Trên JDK 25, Lombok cũ không biên dịch được: thêm `-Dlombok.version=1.18.42`.
Một số test hiện có (`contextLoads`, `LocalFlowBusinessRulesTests`, `QuestionBankServiceImportTests`) đang fail từ trước và cần được sửa riêng.

## Bảo mật

- Tạo tài khoản admin qua `ADMIN_EMAIL`/`ADMIN_PASSWORD` hoặc SQL, không có endpoint công khai nào cấp quyền admin.
- Nếu một key bị lộ vào git, hãy thu hồi và cấp lại ngay; xóa file là không đủ.
