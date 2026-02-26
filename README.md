# CompassEd

## Local Code-First

Project hien tai chay local theo huong code-first:
- Khong can Docker
- Khong ket noi DB
- Du lieu chay in-memory trong backend

### Chay backend

```powershell
cd BE\compassed-api
.\mvnw.cmd spring-boot:run
```

Backend chay tai `http://localhost:8080`.

## MySQL Mode (bat dau migrate)

Backend da co profile `mysql` de chay voi MySQL thay vi in-memory.
Auth cho API protected da su dung JWT (`Authorization: Bearer <token>`), khong con phu thuoc `X-USER-ID`.
`/api/dev/**` chi duoc bat o profile `local`.

### Yeu cau

- MySQL 8+
- Tao database `compassed` (hoac ten khac qua env `MYSQL_URL`)

### Chay backend voi MySQL

```powershell
cd BE\compassed-api
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:MYSQL_URL="jdbc:mysql://localhost:3307/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="1234"
$env:JWT_SECRET="your-very-strong-jwt-secret-at-least-32-chars"
$env:ADMIN_EMAIL="admin@compassed.local"
$env:ADMIN_PASSWORD="admin123"
.\mvnw.cmd spring-boot:run
```

Khi khoi dong profile `mysql`, he thong se seed 3 mon hoc + roadmap L1/L2/L3 neu chua co.
Neu set `ADMIN_EMAIL` + `ADMIN_PASSWORD`, he thong se tao/upgrade user nay thanh role `ADMIN`.
Schema duoc tao/quan ly boi Flyway migration (`src/main/resources/db/migration`).

### Chay MySQL nhanh bang Docker Compose

```powershell
docker compose up -d mysql
```

Mac dinh compose tao DB:
- database: `compassed`
- user/password: `compassed` / `compassed`
- root password: `1234`
- host port: `3307`

### Script chay backend mysql (tu tao DB neu thieu)

```powershell
cd BE\compassed-api
.\scripts\run-mysql.ps1
```

### Chay frontend

```powershell
cd FE
python -m pip install flask requests
python .\Extensions.py
```

Frontend chay tai `http://127.0.0.1:5000`.

## Admin/User APIs da co (khong gom payment)

- Admin (`/api/admin/**`, can JWT role `ADMIN`):
  - analytics overview
  - quan ly users (list + update role)
  - quan ly subjects/roadmaps
  - quan ly lessons + mini-tests (question bank)
  - quan ly system configs
  - gui notification cho tung user hoac broadcast
  - xem/review AI generation logs
- User notification:
  - `GET /api/notifications`
  - `GET /api/notifications/unread-count`
  - `POST /api/notifications/{id}/read`
