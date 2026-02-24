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

### Chay frontend

```powershell
cd FE
python -m pip install flask requests
python .\Extensions.py
```

Frontend chay tai `http://127.0.0.1:5000`.
