# Huong Dan Setup AI (OpenAI API) cho CompassED

## 1. Yeu cau bat buoc
- Co OpenAI API key hop le (tao tai `https://platform.openai.com/api-keys`).
- Tai khoan co billing/deposit de goi API.
- Backend doc key tu env `OPENAI_API_KEY`.

## 2. Chay local (khong Docker)
Dat env truoc khi chay backend:

```powershell
$env:OPENAI_API_KEY="sk-xxxxx"
$env:OPENAI_MODEL="gpt-5"   # tuy chon, mac dinh la gpt-5
```

Backend mapping:
- `openai.api.key: ${OPENAI_API_KEY:}`
- `openai.model: ${OPENAI_MODEL:gpt-5}`

## 3. Chay bang Docker Compose
`docker-compose.yml` da doi sang dung env vars (khong hardcode secret).

Truoc khi chay:

```powershell
$env:OPENAI_API_KEY="sk-xxxxx"
$env:OPENAI_MODEL="gpt-5"
docker compose up -d --build
```

## 4. Kiem tra AI da hoat dong
1. Dang nhap app.
2. Vao flow roadmap va bam `Khoi tao roadmap`/`Refresh roadmap`.
3. Hoac goi API (can auth): `GET /api/me/subjects/{subjectId}/ai-roadmap?action=initialize`

Neu AI loi, he thong van co fallback roadmap de khong chan user.

## 5. Loi thuong gap
- `401 Unauthorized`:
  - API key sai/het han/khong dung project.
  - Chua co billing tren OpenAI Platform.
- `429`:
  - Vuot rate limit/quota.
- Da doi key nhung van loi:
  - Chua restart backend sau khi cap nhat env.

## 6. Bao mat
- Khong commit API key that vao git.
- Khong de secret trong `docker-compose.yml`.
- Neu da lo key, rotate key ngay lap tuc tren OpenAI Platform.
