# Phase 2 E2E Testing Guide

## Prerequisites

### 1. Start Backend
```bash
cd BE/compassed-api
./mvnw spring-boot:run
```
Backend runs on: http://localhost:8080

### 2. Start Frontend
```bash
cd FE
python3 Extensions.py
```
Frontend runs on: http://localhost:3000

### 3. Database Check
Ensure MySQL is running and database `compassed_db` exists with Phase 2 tables:
- payments
- roadmap_modules
- user_module_progress
- mini_tests
- mini_test_attempts
- final_tests
- final_test_attempts

## E2E Test Flow

### Step 1: User Registration
1. Go to http://localhost:3000/auth
2. Click "Register" tab
3. Fill in:
   - Full Name: Test User
   - Email: testuser@test.com
   - Password: Test123!
4. Click "Register"
5. Should redirect to landing page with user logged in

### Step 2: First Placement Test (Free)
1. From landing page, click "Join Program" on Math subject
2. Should redirect to `/placement-test?subjectId=1`
3. Answer all questions (demo data has 10 questions)
4. Click "Submit Test"
5. Should see results page with score and level (L1/L2/L3)
6. Click "Unlock Roadmap" button
7. Should redirect to `/roadmap?subjectId=1&level=L1` (assuming L1)

### Step 3: Second Placement Test (Should Block)
1. Go back to landing page
2. Click "Join Program" on Math again
3. Should see alert: "Ban khong con luot placement mien phi. Ban can mua goi de tiep tuc. Chuyen den trang thanh toan?"
4. Click OK
5. Should redirect to `/payment?subjectId=1`

### Step 4: Payment Flow
1. On payment page, select package:
   - PLACEMENT_PACK: 299,000 VND (1 placement only)
   - SUBSCRIPTION_MONTHLY: 499,000 VND
   - SUBSCRIPTION_3MONTHS: 1,299,000 VND
   - SUBSCRIPTION_6MONTHS: 2,499,000 VND
2. Click "Thanh toán với VNPay"
3. Should redirect to VNPay sandbox URL
4. On VNPay page, use test card:
   - Card Number: 9704198526191432198
   - Cardholder: NGUYEN VAN A
   - Expiry: 07/15
   - OTP: 123456 (or any)
5. Complete payment
6. Should redirect back to `/payment/callback?vnp_ResponseCode=00...`
7. Should see success screen with "Thanh toán thành công!"
8. Payment record created in database with status=SUCCESS
9. Subscription created in database

### Step 5: Access Roadmap
1. From payment success page, click "Xem Roadmap"
2. Or navigate to `/roadmap?subjectId=1&level=L1`
3. Should see:
   - Roadmap header with subject name "Mathematics", level "L1"
   - Progress indicator (0/3 modules)
   - 3 modules: Module 1, Module 2, Module 3
   - Module 1: unlocked (click to view)
   - Module 2 & 3: locked 🔒

### Step 6: Module 1 Learning
1. Click on Module 1 card
2. Modal opens showing module content (markdown)
3. Click "Bắt đầu học"
4. Module status changes to "IN_PROGRESS" 📖
5. Modal shows "Làm Mini Test" button

### Step 7: Mini Test 1
1. Click "Làm Mini Test"
2. Redirect to `/mini-test?moduleId=1`
3. Answer all 5 questions
4. Click "Nộp bài"
5. If score ≥ 70%:
   - Shows "Chúc mừng!" with pass message
   - Module 1 status -> COMPLETED ✓
   - Module 2 automatically unlocked
   - Click "Tiếp tục học" to go back to roadmap
6. If score < 70%:
   - Shows "Chưa đạt" with retry message
   - Must retake mini test

### Step 8: Module 2 & 3
1. Repeat steps 6-7 for Module 2
2. After passing Mini Test 2, Module 3 unlocks
3. Repeat for Module 3
4. After passing Mini Test 3, Final Test section appears

### Step 9: Final Test
1. On roadmap page, scroll to "Final Test" section (golden yellow banner)
2. Click "Làm Final Test"
3. Redirect to `/final-test?roadmapId=1`
4. Answer all 10 questions
5. Click "Nộp bài"
6. If score ≥ 75%:
   - Shows "Xuất sắc!" with promotion section
   - Click "🎖️ Thăng cấp lên Level tiếp theo"
   - User promoted to L2
   - Redirect to home page
7. If score < 75%:
   - Shows "Chưa đạt" with retry message
   - Click "Ôn tập lại" to review content

### Step 10: Level 2 Roadmap
1. After promotion, check database: user's level should be L2
2. Go to `/roadmap?subjectId=1&level=L2`
3. Should see new L2 roadmap (if data exists)
4. Repeat flow for L2 -> L3

## Expected Results

### Database Checks

**After Payment:**
```sql
SELECT * FROM payments WHERE userId = <userId> ORDER BY createdAt DESC LIMIT 1;
-- status should be 'SUCCESS', transactionId populated

SELECT * FROM subscriptions WHERE userId = <userId>;
-- subscription record created
```

**After Module Progress:**
```sql
SELECT * FROM user_module_progress WHERE userId = <userId> ORDER BY moduleId;
-- Module 1: status = 'COMPLETED', miniTestScore = 80 (example)
-- Module 2: status = 'NOT_STARTED' or 'IN_PROGRESS'
```

**After Mini Test:**
```sql
SELECT * FROM mini_test_attempts WHERE userId = <userId> ORDER BY attemptedAt DESC LIMIT 1;
-- score, passed, timeTaken populated
```

**After Final Test:**
```sql
SELECT * FROM final_test_attempts WHERE userId = <userId> ORDER BY attemptedAt DESC LIMIT 1;
-- score, passed, promoted populated

SELECT level FROM users WHERE id = <userId>;
-- level should be 'L2' if promoted
```

## Known Issues & Troubleshooting

### Issue: VNPay Redirect Fails
- **Cause**: VNPay sandbox may be down or URL changed
- **Solution**: Check VNPay docs for latest sandbox URL, update in `application.yml`

### Issue: Module 2 Doesn't Unlock
- **Cause**: Mini Test 1 score < 70%
- **Solution**: Retake Mini Test 1, ensure score ≥ 70%

### Issue: Free Placement Check Not Working
- **Cause**: `user_subject_free_attempts` table missing data
- **Solution**: Insert default free attempt:
```sql
INSERT INTO user_subject_free_attempts (user_id, subject_id, is_used) VALUES (<userId>, 1, false);
```

### Issue: Roadmap Shows No Modules
- **Cause**: No roadmap data for subject/level
- **Solution**: Check `roadmaps` and `roadmap_modules` tables, insert data if missing

### Issue: CORS Errors
- **Cause**: Backend not allowing frontend origin
- **Solution**: Check `@CrossOrigin` annotations in controllers, ensure `origins = "*"` or `origins = "http://localhost:3000"`

## Testing Checklist

- [ ] Register new user
- [ ] First placement test (free) works
- [ ] Second placement test blocks and redirects to payment
- [ ] Payment with VNPay completes successfully
- [ ] Payment record created in database
- [ ] Subscription created in database
- [ ] Roadmap displays with 3 modules
- [ ] Module 1 starts successfully
- [ ] Mini Test 1 submits and scores correctly
- [ ] Module 2 unlocks after passing Mini Test 1
- [ ] Module 3 unlocks after passing Mini Test 2
- [ ] Final Test appears after completing all modules
- [ ] Final Test submits and scores correctly
- [ ] Promotion works when score ≥ 75%
- [ ] User's level updated in database
- [ ] L2 roadmap accessible after promotion

## Performance Notes

- Placement test loading: < 2s
- Module content loading: < 1s
- Mini test submission: < 2s
- Final test submission: < 3s
- VNPay redirect: < 3s (external service)

## Security Notes

- All API calls require `Authorization: Bearer <token>` except auth endpoints
- Payment callback verifies HMAC signature from VNPay
- Free attempt check prevents unauthorized placement tests
- Module unlock logic prevents skipping modules

## Next Steps

1. Replace VNPay sandbox credentials with production credentials
2. Add real roadmap content (markdown, videos) for L2 and L3
3. Add more mini/final test questions
4. Implement quiz difficulty adaptation based on user performance
5. Add analytics dashboard for admin to track user progress
6. Deploy to production environment
