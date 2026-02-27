# 🧪 Quick Test - Admin Question Bank API

## ✅ API đã tạo xong!

### 📦 Files Created:
1. `QuestionBank.java` - Entity class
2. `QuestionBankRepository.java` - Repository interface
3. `QuestionBankDTO.java` - Data Transfer Object
4. `CreateQuestionRequest.java` - Request DTO
5. `QuestionBankService.java` - Service interface
6. `QuestionBankServiceImpl.java` - Service implementation
7. `AdminQuestionBankController.java` - REST Controller

---

## 🚀 Test Commands

### 1. Get All Questions (trang đầu, 5 items)
```bash
curl -s "http://localhost:8080/api/admin/questions?page=0&size=5" | jq '.'
```

### 2. Get Question Stats
```bash
curl -s "http://localhost:8080/api/admin/questions/stats" | jq '.'
```

**Expected:**
```json
{
  "totalQuestions": 30,
  "activeQuestions": 30,
  "byLevel": {
    "L1": 18,
    "L2": 3,
    "L3": 3
  },
  "bySubject": [
    { "subjectName": "Mathematics", "count": 18 },
    { "subjectName": "Literature", "count": 6 },
    { "subjectName": "English", "count": 6 }
  ]
}
```

### 3. Filter by Subject (Toán)
```bash
curl -s "http://localhost:8080/api/admin/questions?subjectId=1&page=0&size=10" | jq '.totalItems'
```

**Expected:** `18`

### 4. Filter by Level (L1)
```bash
curl -s "http://localhost:8080/api/admin/questions?level=L1" | jq '.totalItems'
```

**Expected:** `24` (12 Toán + 6 Văn + 6 Anh)

### 5. Get Skill Types cho Toán L1
```bash
curl -s "http://localhost:8080/api/admin/questions/skill-types?subjectId=1&level=L1" | jq '.'
```

**Expected:**
```json
[
  "Đại số",
  "Hình học"
]
```

### 6. Create New Question
```bash
curl -s -X POST "http://localhost:8080/api/admin/questions" \
  -H "Content-Type: application/json" \
  -d '{
    "subjectId": 1,
    "level": "L1",
    "skillType": "Đại số",
    "questionType": "MULTIPLE_CHOICE",
    "questionText": "Tính: 10 × 10 = ?",
    "options": "[\"A. 10\", \"B. 50\", \"C. 100\", \"D. 200\"]",
    "correctAnswer": "C",
    "explanation": "10 nhân 10 bằng 100",
    "difficulty": 1
  }' | jq '.id'
```

**Expected:** ID của câu hỏi mới (ví dụ: `31`)

### 7. Update Question (thay đổi difficulty)
```bash
curl -s -X PUT "http://localhost:8080/api/admin/questions/1" \
  -H "Content-Type: application/json" \
  -d '{
    "difficulty": 2
  }' | jq '.difficulty'
```

**Expected:** `2`

### 8. Delete Question (Soft Delete)
```bash
curl -s -X DELETE "http://localhost:8080/api/admin/questions/31" | jq '.message'
```

**Expected:** `"Question deleted successfully"`

---

## 🎯 Frontend Integration (Sắp tới)

### Admin Panel UI cần:
1. **Question List Table** với filters:
   - Subject dropdown
   - Level dropdown
   - Skill Type dropdown
   - Search box (question text)
   - Pagination

2. **Create/Edit Modal**:
   - Form với all fields
   - JSON editor cho options
   - Preview question

3. **Stats Dashboard**:
   - Total questions chart
   - By subject pie chart
   - By level bar chart
   - By skill type breakdown

4. **Bulk Import**:
   - Excel upload
   - CSV upload
   - Preview before import

---

## 📊 Current Database State

```sql
-- Check current data
SELECT 
    s.name as subject,
    qb.level,
    qb.skill_type,
    COUNT(*) as count
FROM question_bank qb
JOIN subjects s ON qb.subject_id = s.id
WHERE qb.is_active = 1
GROUP BY s.name, qb.level, qb.skill_type
ORDER BY s.name, qb.level, qb.skill_type;
```

**Result:**
- Mathematics L1 Đại số: 8 questions
- Mathematics L1 Hình học: 4 questions
- Mathematics L2 Đại số: 2 questions
- Mathematics L2 Hình học: 1 question
- Mathematics L3 Đại số: 2 questions
- Mathematics L3 Hình học: 1 question
- Literature L1 Ngữ pháp: 4 questions
- Literature L1 Đọc hiểu: 2 questions
- English L1 Grammar: 3 questions
- English L1 Vocabulary: 2 questions
- English L1 Reading: 1 question

**TOTAL: 30 questions**

---

## ⚡ Next Steps

1. ✅ Test all 8 API endpoints
2. ⏭️ Add Authorization (only ADMIN can access)
3. ⏭️ Create Frontend Admin Panel
4. ⏭️ Implement Excel Import/Export
5. ⏭️ Add Question Preview/Test feature
6. ⏭️ Integrate with Placement Test Service
