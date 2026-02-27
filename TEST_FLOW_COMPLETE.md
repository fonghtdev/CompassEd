# 🧪 Complete Test Flow - CompassED Phase 2

## ✅ Prerequisites Check

1. **Backend running**: http://localhost:8080
2. **Frontend running**: http://localhost:3000
3. **Database**: MySQL với `compassed_db`

---

## 📋 Test Flow Từ Home Page

### ✨ Step 1: Trang Home (Landing Page)
1. Mở browser: **http://localhost:3000**
2. Bạn sẽ thấy landing page với:
   - Hero section: "Your AI Learning Compass"
   - 3 subject cards: Math, Literature, English
   - Mỗi card có button "Join Program"

### 🔐 Step 2: Register (Tạo tài khoản mới)
1. Click button "Join Program" trên card **Mathematics**
2. Redirect đến `/auth` (nếu chưa login)
3. Click tab **"Register"**
4. Điền form:
   ```
   Full Name: Test User Phase2
   Email: testphase2@compassed.com
   Password: Test123!
   ```
5. Click **"Register"**
6. Sau khi success → redirect về landing page, user đã login

**Expected:**
- Toast hiển thị "Register successful"
- Top-right navbar hiển thị tên user

### 📝 Step 3: Placement Test (Lần 1 - FREE)
1. Click lại button **"Join Program"** trên Mathematics card
2. Redirect đến `/placement-test?subjectId=1`
3. Backend check free attempts → **Có 1 lần free**
4. Placement test hiển thị với 10 câu hỏi
5. Trả lời tất cả câu hỏi (chọn bất kỳ)
6. Click **"Submit Test"**
7. Redirect đến `/placement-result`

**Expected:**
- Xem score (ví dụ: 70%)
- Xem level assigned (L1/L2/L3)
- Button "Unlock Roadmap" xuất hiện

### 🗺️ Step 4: View Roadmap
1. Từ placement result page, click **"Unlock Roadmap"**
2. Redirect đến `/roadmap?subjectId=1&level=L1`
3. Roadmap page hiển thị:
   - Header: Mathematics - Level 1
   - Progress: 0/3 modules completed (0%)
   - 3 module cards:
     - **Module 1**: ✅ Unlocked (màu xanh/xám)
     - **Module 2**: 🔒 Locked
     - **Module 3**: 🔒 Locked

**Expected:**
- Module 1 có thể click
- Module 2, 3 có icon 🔒 và text "Locked"

### 📚 Step 5: Module 1 Learning
1. Click vào **Module 1** card
2. Modal popup hiển thị:
   - Title: Module 1 name
   - Content: Markdown content (description)
   - Button: **"Bắt đầu học"**
3. Click **"Bắt đầu học"**
4. Backend tạo `user_module_progress` với status = IN_PROGRESS
5. Modal đóng lại
6. Module 1 card status thay đổi → 📖 "Đang học"
7. Click lại Module 1 → Modal hiển thị button **"Làm Mini Test"**

**Expected:**
- Module 1 status: IN_PROGRESS
- Button "Làm Mini Test" xuất hiện

### 📝 Step 6: Mini Test 1
1. Click **"Làm Mini Test"** trong Module 1 modal
2. Redirect đến `/mini-test?moduleId=1`
3. Mini test hiển thị 5 câu hỏi
4. Timer bắt đầu đếm (00:00, 00:01, ...)
5. Trả lời tất cả 5 câu
6. Click **"Nộp bài"**
7. Backend tính điểm:
   - Nếu **score ≥ 70%** → Passed ✅
   - Nếu **score < 70%** → Failed ❌

**Expected (if Passed):**
- Result modal: "Chúc mừng! Bạn đã vượt qua"
- Score hiển thị (ví dụ: 80%)
- Backend tự động:
  - Set Module 1 status = COMPLETED
  - Unlock Module 2 (tạo progress với status = NOT_STARTED)
- Button "Tiếp tục học" → quay về roadmap

**Expected (if Failed):**
- Result modal: "Chưa đạt. Cần ≥70%"
- Button "Thử lại"

### 🔄 Step 7: Module 2 Learning (sau khi pass Mini Test 1)
1. Quay về roadmap page
2. Thấy progress: **1/3 modules completed (33%)**
3. Module 1: ✅ Completed (màu xanh lá)
4. Module 2: 📚 Unlocked (có thể click)
5. Module 3: 🔒 Locked
6. Repeat Step 5-6 cho Module 2

### 🔄 Step 8: Module 3 Learning
1. Sau khi pass Mini Test 2
2. Module 3 tự động unlock
3. Repeat Step 5-6 cho Module 3

**Expected sau Module 3:**
- Progress: **3/3 modules completed (100%)**
- Final Test section xuất hiện (màu vàng/cam)

### 🏆 Step 9: Final Test
1. Scroll xuống roadmap page
2. Thấy section **"🏆 Final Test"** (màu vàng/cam)
3. Description: "Hoàn thành tất cả modules để mở khóa"
4. Button: **"Làm Final Test"** (enabled vì đã hoàn thành 3 modules)
5. Click **"Làm Final Test"**
6. Redirect đến `/final-test?roadmapId=1`
7. Final test hiển thị 10 câu hỏi
8. Trả lời tất cả 10 câu
9. Click **"Nộp bài"**
10. Backend tính điểm:
    - Nếu **score ≥ 75%** → Passed ✅
    - Nếu **score < 75%** → Failed ❌

**Expected (if Passed):**
- Result modal: "Xuất sắc! Bạn đã vượt qua final test"
- Score hiển thị (ví dụ: 85%)
- Section promotion: "🎖️ Bạn đủ điều kiện thăng cấp!"
- Button **"🎖️ Thăng cấp lên Level tiếp theo"**

### 🎖️ Step 10: Promotion
1. Click **"Thăng cấp lên Level tiếp theo"**
2. Backend:
   - Update user level: L1 → L2
   - Mark attempt.promoted = true
3. Alert: "Chúc mừng! Bạn đã được thăng cấp"
4. Redirect về home page

**Expected:**
- User level trong database = L2
- Có thể access `/roadmap?subjectId=1&level=L2` (nếu có data)

---

## 🔒 Step 11: Test Free Placement Limit

### Scenario: User đã dùng hết 1 lần free
1. Từ home page, click **"Join Program"** trên Math again
2. Backend check free attempts:
   - `user_subject_free_attempts.used = true`
   - No active subscription
3. Alert popup: "Bạn không còn lượt placement miễn phí. Cần mua gói để tiếp tục. Chuyển đến trang thanh toán?"
4. Click **OK**
5. Redirect đến `/payment?subjectId=1`

**Expected:**
- Placement test bị block
- Redirect đến payment page

---

## 💳 Step 12: Payment Flow

### A. Payment Page
1. Tại `/payment?subjectId=1`
2. Hiển thị 4 gói:
   - **PLACEMENT_PACK**: 299,000 VND (1 placement test)
   - **SUBSCRIPTION_MONTHLY**: 499,000 VND
   - **SUBSCRIPTION_3MONTHS**: 1,299,000 VND
   - **SUBSCRIPTION_6MONTHS**: 2,499,000 VND
3. Select một gói (ví dụ: MONTHLY)
4. Click **"Thanh toán với VNPay"**

### B. VNPay Redirect
1. Backend tạo payment record với status = PENDING
2. Generate VNPay URL với HMAC signature
3. Redirect đến VNPay sandbox: `https://sandbox.vnpayment.vn/...`

### C. VNPay Test Payment
1. Trên VNPay sandbox page, nhập test card:
   ```
   Card Number: 9704198526191432198
   Cardholder: NGUYEN VAN A
   Expiry Date: 07/15
   OTP: 123456 (hoặc bất kỳ)
   ```
2. Click **"Thanh toán"**
3. VNPay redirect về: `http://localhost:3000/payment/callback?vnp_ResponseCode=00&...`

### D. Payment Callback
1. Frontend nhận callback params
2. Call backend `/api/payments/callback/vnpay`
3. Backend:
   - Verify HMAC signature
   - Update payment status = SUCCESS
   - Create subscription record
4. Frontend hiển thị:
   - Success screen: "Thanh toán thành công! 🎉"
   - Payment details
   - Button "Xem Roadmap" hoặc "Trang chủ"

**Expected:**
- Database `payments` table: 1 record với status = SUCCESS
- Database `subscriptions` table: 1 record cho user này
- User giờ có thể làm placement test lại

---

## 🔁 Step 13: Verify Subscription Works

1. Click **"Trang chủ"** từ payment success page
2. Click **"Join Program"** trên Math
3. Backend check:
   - Free attempts used = true
   - Has active subscription = true ✅
4. Allow vào placement test
5. Làm placement test thành công → Không bị block

**Expected:**
- Placement test accessible
- No payment redirect

---

## 📊 Database Verification

### Check User Progress:
```sql
-- Check user's level
SELECT id, email, level FROM users WHERE email = 'testphase2@compassed.com';

-- Check module progress
SELECT 
    m.module_name,
    ump.status,
    ump.mini_test_score,
    ump.completed_at
FROM user_module_progress ump
JOIN roadmap_modules m ON ump.module_id = m.id
WHERE ump.user_id = <USER_ID>
ORDER BY m.order_index;

-- Check payment
SELECT id, amount, status, package_type, created_at 
FROM payments 
WHERE user_id = <USER_ID>;

-- Check subscription
SELECT id, subject_id, start_date, end_date, is_active
FROM subscriptions
WHERE user_id = <USER_ID>;

-- Check mini test attempts
SELECT mt.test_name, mta.score, mta.passed, mta.attempted_at
FROM mini_test_attempts mta
JOIN mini_tests mt ON mta.mini_test_id = mt.id
WHERE mta.user_id = <USER_ID>;

-- Check final test attempts
SELECT fta.score, fta.passed, fta.promoted, fta.attempted_at
FROM final_test_attempts fta
WHERE fta.user_id = <USER_ID>;
```

---

## ✅ Test Checklist

- [ ] Home page loads successfully
- [ ] Register new user works
- [ ] First placement test (free) works
- [ ] Placement result shows correct score/level
- [ ] Roadmap displays with locked/unlocked modules
- [ ] Module 1 can be started
- [ ] Mini Test 1 submission works
- [ ] Module 2 unlocks after passing Mini Test 1
- [ ] Module 3 unlocks after passing Mini Test 2
- [ ] Final Test appears after completing all modules
- [ ] Final Test submission works
- [ ] Promotion button appears if score ≥ 75%
- [ ] User promoted to L2 in database
- [ ] Second placement attempt blocked (free used)
- [ ] Payment page shows 4 packages
- [ ] VNPay redirect works
- [ ] Payment callback updates database
- [ ] Subscription created successfully
- [ ] Placement test accessible after payment

---

## 🐛 Common Issues

### Issue 1: "Free attempts check not working"
**Cause**: Table `user_subject_free_attempts` empty
**Fix**:
```sql
INSERT INTO user_subject_free_attempts (user_id, subject_id, is_used) 
VALUES (<USER_ID>, 1, false);
```

### Issue 2: "Module 2 doesn't unlock"
**Cause**: Mini Test 1 score < 70%
**Fix**: Retake Mini Test 1 and score ≥ 70%

### Issue 3: "Payment callback returns error"
**Cause**: HMAC signature mismatch
**Fix**: Check VNPay hash secret in `application.yml`

### Issue 4: "CORS error on API calls"
**Cause**: Backend not allowing frontend origin
**Fix**: Check `@CrossOrigin` in controllers

---

## 📹 Quick Test Commands

```bash
# Check backend status
curl http://localhost:8080/api/subjects

# Check frontend
curl -I http://localhost:3000

# View backend logs
tail -f /tmp/backend.log

# View frontend logs
tail -f /tmp/frontend.log

# Clear database (reset test)
mysql -u root -proot compassed_db -e "
SET FOREIGN_KEY_CHECKS=0;
DELETE FROM final_test_attempts;
DELETE FROM mini_test_attempts;
DELETE FROM user_module_progress;
DELETE FROM payments;
DELETE FROM subscriptions;
DELETE FROM placement_attempts;
DELETE FROM user_subject_free_attempts;
DELETE FROM users WHERE email='testphase2@compassed.com';
SET FOREIGN_KEY_CHECKS=1;
"
```

---

## 🎯 Success Criteria

✅ User có thể:
1. Register và login
2. Làm placement test (free lần đầu)
3. View roadmap với modules
4. Complete modules theo thứ tự
5. Pass mini tests để unlock modules tiếp theo
6. Pass final test để được promote
7. Bị block khi hết free attempts
8. Thanh toán qua VNPay
9. Access placement test sau khi có subscription

---

## 📞 Need Help?

- Backend logs: `/tmp/backend.log`
- Frontend logs: `/tmp/frontend.log`
- API docs: Check `API_INTEGRATION_GUIDE.md`
- Testing guide: `PHASE2_TESTING_GUIDE.md`
