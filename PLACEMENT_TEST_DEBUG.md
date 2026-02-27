# Debug Guide: Placement Test Issues

## Vấn đề đã sửa

Đã sửa lỗi khi ấn "Next Question" không chuyển câu hỏi và không lưu được đáp án.

### Nguyên nhân

1. **Event listener không hoạt động**: Do sử dụng `innerHTML` để tạo radio buttons, event listener được gán trước khi element được thêm vào DOM
2. **Thiếu debug log**: Không có log để theo dõi quá trình chọn đáp án

### Giải pháp đã áp dụng

1. **Tạo element bằng JavaScript thay vì innerHTML**:
   - Tạo từng element (input, div, span) bằng `createElement()`
   - Gắn event listener SAU KHI element đã được tạo
   - Đảm bảo cấu trúc DOM đúng với Tailwind CSS peer classes

2. **Thêm debug logging**:
   - Log khi render câu hỏi
   - Log khi chọn đáp án
   - Log khi click Next
   - Log khi submit bài test

## Cách test

### 1. Mở Browser Developer Tools

```
Nhấn F12 hoặc:
- Chrome/Edge: Ctrl+Shift+I (Windows) / Cmd+Option+I (Mac)
- Firefox: Ctrl+Shift+K (Windows) / Cmd+Option+K (Mac)
```

### 2. Mở Console tab

Trong Developer Tools, chọn tab "Console"

### 3. Bắt đầu Placement Test

```
1. Vào http://localhost:3000
2. Click "Join Program" trên một môn học
3. Đăng nhập/Đăng ký nếu chưa
4. Bắt đầu Placement Test
```

### 4. Xem Debug Logs

Khi làm bài test, bạn sẽ thấy các log sau trong Console:

#### Khi render câu hỏi:
```
Rendering question 1: 123 "What is 2+2?"
Answered: 0 / 10
```

#### Khi chọn đáp án:
```
Answer selected: A
Answer saved for question 123: A
Current answers: {123: "A"}
```

#### Khi click Next:
```
Next button clicked. Current question: 123 Answer: A
Moving to question 2
Rendering question 2: 124 "What is 3+3?"
Answered: 1 / 10
```

#### Khi submit:
```
Next button clicked. Current question: 132 Answer: D
Submitting test with answers: {123: "A", 124: "B", ..., 132: "D"}
Submit result: {attemptId: 456, scorePercent: 80, level: "L2", ...}
```

### 5. Kiểm tra LocalStorage

Trong Console, gõ:

```javascript
// Xem đáp án hiện tại
localStorage.getItem('compassed_answers_json_1')

// Xem paper
localStorage.getItem('compassed_paper_json_1')

// Xem attempt ID
localStorage.getItem('compassed_attempt_id_1')
```

## Vấn đề thường gặp

### 1. Không thấy log trong Console

**Nguyên nhân**: Cache browser chưa được refresh

**Giải pháp**:
```
Nhấn Ctrl+Shift+R (Windows) / Cmd+Shift+R (Mac)
để hard refresh và xóa cache
```

### 2. Vẫn không chuyển được câu hỏi

**Kiểm tra**:
1. Mở Console xem có lỗi JavaScript không
2. Xem log "Answer selected" có xuất hiện khi click vào đáp án không
3. Xem log "Next button clicked" có xuất hiện khi click Next không

**Debug**:
```javascript
// Trong Console, xem answers object
let answers = JSON.parse(localStorage.getItem('compassed_answers_json_1') || '{}')
console.log('Current answers:', answers)

// Xem paper
let paper = JSON.parse(localStorage.getItem('compassed_paper_json_1') || '[]')
console.log('Total questions:', paper.length)
console.log('First question:', paper[0])
```

### 3. Submit không hoạt động

**Kiểm tra**:
1. Xem log "Submitting test with answers"
2. Xem có lỗi "Submit error" không
3. Kiểm tra network tab xem API call `/api/placement-attempts/{id}/submit` có được gọi không

**Debug API**:
```javascript
// Test API submit manually
let attemptId = localStorage.getItem('compassed_attempt_id_1')
let answers = localStorage.getItem('compassed_answers_json_1')

fetch(`http://localhost:8080/api/placement-attempts/${attemptId}/submit`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('compassed_auth_token')}`
  },
  body: JSON.stringify({ answersJson: answers })
})
  .then(r => r.json())
  .then(console.log)
  .catch(console.error)
```

## Clear dữ liệu để test lại

```javascript
// Clear placement test data
localStorage.removeItem('compassed_attempt_id_1')
localStorage.removeItem('compassed_paper_json_1')
localStorage.removeItem('compassed_answers_json_1')

// Hoặc clear tất cả
localStorage.clear()

// Sau đó refresh page
location.reload()
```

## Server Status

### Backend
```bash
# Check backend
curl http://localhost:8080/api/subjects

# Xem log backend
tail -f /path/to/backend/logs
```

### Frontend
```bash
# Check frontend
curl http://localhost:3000

# Xem log frontend
tail -f /tmp/frontend.log
```

## Database Verification

```sql
-- Check placement attempts
SELECT * FROM placement_attempts 
WHERE user_id = (SELECT id FROM users WHERE email = 'testphase2@compassed.com')
ORDER BY id DESC LIMIT 5;

-- Check if answer was saved
SELECT id, answers_json, score_percent, level 
FROM placement_attempts 
WHERE id = <attemptId>;
```

## Next Steps

Sau khi đã test xong placement test thành công:

1. ✅ Chọn được đáp án (có log "Answer selected")
2. ✅ Chuyển được câu hỏi tiếp theo (có log "Moving to question X")
3. ✅ Submit được bài test (có log "Submit result")
4. ✅ Xem được kết quả (/placement-result)

Tiếp tục test các tính năng Phase 2 khác theo **TEST_FLOW_COMPLETE.md**
