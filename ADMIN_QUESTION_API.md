# Admin Question Bank API Documentation

Base URL: `http://localhost:8080/api/admin/questions`

## 📚 Endpoints

### 1. Get All Questions (với filter & phân trang)
```http
GET /api/admin/questions
```

**Query Parameters:**
- `subjectId` (optional): Long - Filter theo subject
- `level` (optional): L1 | L2 | L3 - Filter theo level
- `skillType` (optional): String - Filter theo skill type
- `isActive` (optional): Boolean - Filter theo trạng thái
- `page` (default: 0): int - Số trang
- `size` (default: 20): int - Số items/trang
- `sortBy` (default: id): String - Sắp xếp theo field
- `sortDir` (default: DESC): ASC | DESC

**Example:**
```bash
# Lấy tất cả câu hỏi Toán Level L1
curl "http://localhost:8080/api/admin/questions?subjectId=1&level=L1&page=0&size=10"

# Lấy câu hỏi Đại số
curl "http://localhost:8080/api/admin/questions?skillType=Đại%20số"
```

**Response:**
```json
{
  "questions": [
    {
      "id": 1,
      "subjectId": 1,
      "subjectName": "Mathematics",
      "level": "L1",
      "skillType": "Đại số",
      "questionType": "MULTIPLE_CHOICE",
      "questionText": "Tính: 2 + 3 = ?",
      "options": "[\"A. 4\", \"B. 5\", \"C. 6\", \"D. 7\"]",
      "correctAnswer": "B",
      "explanation": "2 cộng 3 bằng 5",
      "difficulty": 1,
      "isActive": true
    }
  ],
  "currentPage": 0,
  "totalItems": 30,
  "totalPages": 3
}
```

---

### 2. Get Question by ID
```http
GET /api/admin/questions/{id}
```

**Example:**
```bash
curl http://localhost:8080/api/admin/questions/1
```

---

### 3. Create New Question
```http
POST /api/admin/questions
Content-Type: application/json
```

**Request Body:**
```json
{
  "subjectId": 1,
  "level": "L1",
  "skillType": "Đại số",
  "questionType": "MULTIPLE_CHOICE",
  "questionText": "Tính: 10 + 5 = ?",
  "options": "[\"A. 10\", \"B. 15\", \"C. 20\", \"D. 25\"]",
  "correctAnswer": "B",
  "explanation": "10 cộng 5 bằng 15",
  "difficulty": 1
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/admin/questions \
  -H "Content-Type: application/json" \
  -d '{
    "subjectId": 1,
    "level": "L1",
    "skillType": "Đại số",
    "questionType": "MULTIPLE_CHOICE",
    "questionText": "Tính: 10 + 5 = ?",
    "options": "[\"A. 10\", \"B. 15\", \"C. 20\", \"D. 25\"]",
    "correctAnswer": "B",
    "explanation": "10 cộng 5 bằng 15",
    "difficulty": 1
  }'
```

---

### 4. Update Question
```http
PUT /api/admin/questions/{id}
Content-Type: application/json
```

**Request Body:** (Giống Create, nhưng chỉ gửi field cần update)
```json
{
  "questionText": "Tính: 10 + 5 = ? (Updated)",
  "difficulty": 2
}
```

**Example:**
```bash
curl -X PUT http://localhost:8080/api/admin/questions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "questionText": "Tính: 2 + 3 = ? (Updated)",
    "difficulty": 2
  }'
```

---

### 5. Delete Question (Soft Delete)
```http
DELETE /api/admin/questions/{id}
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/admin/questions/1
```

**Response:**
```json
{
  "message": "Question deleted successfully"
}
```

---

### 6. Hard Delete Question (Xóa vĩnh viễn)
```http
DELETE /api/admin/questions/{id}/hard
```

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/admin/questions/1/hard
```

---

### 7. Get Question Statistics
```http
GET /api/admin/questions/stats
```

**Example:**
```bash
curl http://localhost:8080/api/admin/questions/stats
```

**Response:**
```json
{
  "totalQuestions": 30,
  "activeQuestions": 28,
  "byLevel": {
    "L1": 18,
    "L2": 6,
    "L3": 4
  },
  "bySubject": [
    {
      "subjectName": "Mathematics",
      "count": 18
    },
    {
      "subjectName": "Literature",
      "count": 6
    },
    {
      "subjectName": "English",
      "count": 6
    }
  ]
}
```

---

### 8. Get Skill Types by Subject & Level
```http
GET /api/admin/questions/skill-types?subjectId={subjectId}&level={level}
```

**Example:**
```bash
# Lấy skill types của Toán Level L1
curl "http://localhost:8080/api/admin/questions/skill-types?subjectId=1&level=L1"
```

**Response:**
```json
[
  "Đại số",
  "Hình học"
]
```

---

## 🧪 Test Commands

### Test GET all questions
```bash
curl -s http://localhost:8080/api/admin/questions | jq '.'
```

### Test GET with filters
```bash
# Filter by subject
curl -s "http://localhost:8080/api/admin/questions?subjectId=1" | jq '.questions[0]'

# Filter by level
curl -s "http://localhost:8080/api/admin/questions?level=L1" | jq '.totalItems'

# Filter by skill type
curl -s "http://localhost:8080/api/admin/questions?skillType=Đại%20số" | jq '.questions[].questionText'
```

### Test CREATE
```bash
curl -s -X POST http://localhost:8080/api/admin/questions \
  -H "Content-Type: application/json" \
  -d '{
    "subjectId": 1,
    "level": "L1",
    "skillType": "Đại số",
    "questionType": "MULTIPLE_CHOICE",
    "questionText": "Tính: 100 + 200 = ?",
    "options": "[\"A. 200\", \"B. 250\", \"C. 300\", \"D. 350\"]",
    "correctAnswer": "C",
    "explanation": "100 cộng 200 bằng 300",
    "difficulty": 1
  }' | jq '.'
```

### Test UPDATE
```bash
curl -s -X PUT http://localhost:8080/api/admin/questions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "difficulty": 2,
    "explanation": "Updated explanation"
  }' | jq '.'
```

### Test STATS
```bash
curl -s http://localhost:8080/api/admin/questions/stats | jq '.'
```

### Test SKILL TYPES
```bash
curl -s "http://localhost:8080/api/admin/questions/skill-types?subjectId=1&level=L1" | jq '.'
```

---

## 🔒 Security Notes

**TODO:** Thêm authentication/authorization:
- Chỉ cho phép user có role ADMIN truy cập
- Sử dụng JWT token trong header: `Authorization: Bearer <token>`

---

## 📊 Response Status Codes

- `200 OK` - Thành công
- `201 Created` - Tạo mới thành công
- `400 Bad Request` - Request không hợp lệ
- `404 Not Found` - Không tìm thấy resource
- `500 Internal Server Error` - Lỗi server
