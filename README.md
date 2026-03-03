## Run Project

### macOS / Linux:
1. Run both Backend & Frontend:
```bash
./scripts/run-all.sh
```

Or run separately in 2 terminals:
```bash
# Terminal 1 - Backend
./scripts/run-be.sh

# Terminal 2 - Frontend
./scripts/run-fe.sh
```

### Windows:
1. Run Backend:
```powershell
.\scripts\run-be.ps1
```

2. Run Frontend:
```powershell
.\scripts\run-fe.ps1
```

3. Run both in 2 terminals:
```powershell
.\scripts\run-all.ps1
```

### Access:
- **Frontend**: http://127.0.0.1:3000/landing (or http://127.0.0.1:3000)
- **Backend API**: http://localhost:8080

**Important:**
- ✅ Flask runs on port **3000** (macOS uses port 5000 for AirPlay)
- ✅ Use `/landing` route, NOT `/template/landingPage.html` 
- ✅ MySQL password is `root` (not `1234`)

<!-- # CompassEd

## Run Project (MySQL + FE)

This project currently runs with:
- Backend: Spring Boot (`BE/compassed-api`)
- Frontend: Flask static host (`FE/Extensions.py`)
- Database: MySQL 8 (Docker Compose or local MySQL)

## 1) Start MySQL

### Option A: Docker Compose (recommended)

```powershell
cd D:\FPT\CompassEd
docker compose up -d mysql
```

Current compose defaults:
- database: `compassed`
- root user: `root`
- root password: `1234`
- host port: `3307` (container `3306`)

Important:
- Do not set `MYSQL_USER=root` in compose.
- Root login is handled by `MYSQL_ROOT_PASSWORD`.

### Option B: Local MySQL service

If you already run MySQL locally (for example port `3306`), use that directly.

## 2) Start Backend (profile mysql)

### Fast way (script)

```powershell
cd D:\FPT\CompassEd\BE\compassed-api
.\scripts\run-mysql.ps1 -MySqlHost localhost -MySqlPort 3307 -MySqlUser root -MySqlPassword 1234 -Database compassed
```

If your local MySQL is on port `3306`, run:

```powershell
.\scripts\run-mysql.ps1 -MySqlHost localhost -MySqlPort 3306 -MySqlUser root -MySqlPassword 1234 -Database compassed
```

Backend URL:
- `http://localhost:8080`

### Manual env way (alternative)

```powershell
cd D:\FPT\CompassEd\BE\compassed-api
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:MYSQL_URL="jdbc:mysql://localhost:3307/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="1234"
$env:JWT_SECRET="this-is-a-very-strong-jwt-secret-min-32"
$env:SERVER_PORT="8080"
.\mvnw.cmd spring-boot:run
```

## 3) Start Frontend

```powershell
cd D:\FPT\CompassEd\FE
python -m pip install flask requests
python .\Extensions.py
```

Frontend URL:
- `http://127.0.0.1:5000`
- Landing page: `http://127.0.0.1:5000/template/landingPage.html`

## Notes

- Backend MySQL config is read from:
  - `BE/compassed-api/src/main/resources/application-mysql.yml`
- DB schema is managed by Flyway:
  - `BE/compassed-api/src/main/resources/db/migration`
- If FE cannot call BE, check `FE/config.js` (`API_BASE` should be `http://localhost:8080`). -->
