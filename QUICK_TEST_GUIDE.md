# 🚀 Quick Start - Test Phase 2

## Bắt đầu test ngay!

### 1️⃣ Servers đang chạy
- ✅ Backend: http://localhost:8080
- ✅ Frontend: http://localhost:3000

### 2️⃣ Test Helper Page
Mở trang helper để check status và debug:
**http://localhost:3000/test-helper**

Trang này có:
- Backend status check
- Current user info
- Quick navigation buttons
- API testing tools
- LocalStorage inspector

### 3️⃣ Bắt đầu Test Flow

#### Option A: Test từ đầu (Fresh start)
```bash
# Clear test data
./reset-test-data.sh

# Sau đó test theo flow
```

#### Option B: Test ngay với data hiện tại
1. **Home Page**: http://localhost:3000
2. Click "Join Program" trên Mathematics
3. Nếu chưa login → Register tại `/auth`
4. Follow complete flow trong `TEST_FLOW_COMPLETE.md`

---

## 📋 Test Flow Summary

```
Home → Register → Placement Test (Free) → Result → Roadmap
  ↓
Module 1 → Mini Test 1 (≥70%) → Module 2 unlocks
  ↓
Module 2 → Mini Test 2 (≥70%) → Module 3 unlocks
  ↓
Module 3 → Mini Test 3 (≥70%) → Final Test unlocks
  ↓
Final Test (≥75%) → Promotion to L2
  ↓
Try Placement again → Blocked → Payment page
  ↓
VNPay payment → Success → Subscription created
  ↓
Placement test works again!
```

---

## 🎯 Quick Test Checklist

Register phase:
- [ ] Register new user: `testphase2@compassed.com` / `Test123!`
- [ ] Login successful

Placement phase:
- [ ] First placement test works (FREE)
- [ ] Result shows score and level
- [ ] Roadmap accessible

Learning phase:
- [ ] Module 1 starts successfully
- [ ] Mini Test 1 passable (≥70%)
- [ ] Module 2 unlocks automatically
- [ ] Module 3 unlocks after Module 2
- [ ] Final Test appears after all modules

Final phase:
- [ ] Final Test passable (≥75%)
- [ ] Promotion button appears
- [ ] User promoted to L2 in database

Payment phase:
- [ ] Second placement blocked
- [ ] Redirect to payment page
- [ ] VNPay payment completes
- [ ] Subscription created
- [ ] Placement accessible after payment

---

## 🔧 Debug Tools

### Test Helper Page
```
http://localhost:3000/test-helper
```

### Check Backend
```bash
curl http://localhost:8080/api/subjects
```

### Check Database
```bash
mysql -u root -proot compassed_db -e "
SELECT 
    u.email, 
    u.level,
    COUNT(DISTINCT ump.module_id) as modules_completed,
    COUNT(DISTINCT mta.id) as mini_tests_taken,
    COUNT(DISTINCT fta.id) as final_tests_taken,
    COUNT(DISTINCT s.id) as subscriptions
FROM users u
LEFT JOIN user_module_progress ump ON u.id = ump.user_id AND ump.status = 'COMPLETED'
LEFT JOIN mini_test_attempts mta ON u.id = mta.user_id
LEFT JOIN final_test_attempts fta ON u.id = fta.user_id
LEFT JOIN subscriptions s ON u.id = s.user_id
WHERE u.email LIKE '%test%'
GROUP BY u.id;
"
```

### Clear Test Data
```bash
./reset-test-data.sh
```

---

## 📚 Documentation

- **Complete Test Flow**: `TEST_FLOW_COMPLETE.md`
- **Phase 2 Testing Guide**: `PHASE2_TESTING_GUIDE.md`
- **Implementation Summary**: `PHASE2_IMPLEMENTATION_SUMMARY.md`
- **Phase 2 Complete**: `PHASE2_COMPLETE.md`

---

## 🎥 VNPay Test Card

Khi test payment, dùng card này:
```
Card Number: 9704198526191432198
Name: NGUYEN VAN A
Expiry: 07/15
OTP: 123456 (or any 6 digits)
```

---

## ⚠️ Common Issues

### "Backend not responding"
```bash
# Check if running
lsof -ti:8080

# Restart if needed
cd BE/compassed-api
bash mvnw spring-boot:run
```

### "Frontend not responding"
```bash
# Check if running
lsof -ti:3000

# Restart if needed
cd FE
python3 Extensions.py
```

### "Free attempts not working"
Database missing data. Run:
```bash
./reset-test-data.sh
```

### "Module not unlocking"
Check mini test score:
```sql
SELECT * FROM mini_test_attempts WHERE user_id = <USER_ID>;
```
Score must be ≥ 70%

---

## ✅ Success!

Khi test xong, bạn sẽ thấy:
1. ✅ User đã promote từ L1 → L2
2. ✅ Database có payment record
3. ✅ Database có subscription
4. ✅ User có thể access roadmap L2

**Congratulations! Phase 2 works perfectly! 🎉**
