# 📐 CompassED - System Architecture & Development Roadmap

**Ngày cập nhật:** 26/02/2026  
**Phiên bản:** 2.0  

---

## 📊 I. ĐÁNH GIÁ TÌNH TRẠNG HIỆN TẠI

### ✅ ĐÃ HOÀN THÀNH (MVP Phase 1)

#### **1. Authentication Module** ✅
- [x] Đăng ký user (POST `/api/auth/register`)
- [x] Đăng nhập (POST `/api/auth/login`)
- [x] Xác thực token (GET `/api/auth/me`)
- [x] OAuth mock (POST `/api/auth/oauth/mock`)
- [x] Role-based authorization (USER/ADMIN)
- **Database:** `users` table với role, email, password_hash
- **Frontend:** Auth page với tab Login/Register

#### **2. Subject Management** ✅
- [x] Danh sách subjects (GET `/api/subjects`)
- [x] Subject detail (GET `/api/subjects/{id}`)
- [x] Subject by code (GET `/api/subjects/code/{code}`)
- **Database:** `subjects` table (MATH, LITERATURE, ENGLISH)
- **Frontend:** Landing page load subjects động từ API

#### **3. Placement Test System** ✅ (Partial)
- [x] Start placement test (POST `/api/subjects/{subjectId}/placement-tests`)
- [x] Submit test (POST `/api/placement-attempts/{attemptId}/submit`)
- [x] Tính điểm + gán level (L1/L2/L3)
- [x] Lưu result vào database
- **Database:** `placement_attempts`, `question_bank`
- **Frontend:** Placement test UI với progress bar
- **Missing:** ❌ Kiểm tra free attempts

#### **4. Subscription System** ✅ (Partial)
- [x] Checkout subscription (POST `/api/subscriptions/checkout`)
- [x] Mở khóa môn học
- **Database:** `subscriptions`, `user_subjects`
- **Frontend:** Unlock roadmap button
- **Missing:** ❌ Payment gateway integration

#### **5. History Tracking** ✅
- [x] Xem lịch sử placement tests (GET `/api/history/placements`)
- **Frontend:** History page với table

#### **6. Admin Portal** ✅
- [x] Admin authentication
- [x] Dashboard với stats
- [x] User management (list, set admin role)
- [x] Create admin user
- **Frontend:** Admin login + dashboard

---

### ⚠️ CẦN BỔ SUNG (Phase 2)

#### **1. Free Placement Logic** ❌
**Hiện tại:** Không có giới hạn số lần test miễn phí  
**Cần làm:**
- Thêm column `free_placement_remaining` vào `user_subjects`
- Logic check trước khi cho làm test:
  ```sql
  SELECT free_placement_remaining FROM user_subjects
  WHERE user_id = ? AND subject_id = ?
  ```
- Nếu hết lượt → redirect to payment

#### **2. Payment Gateway** ❌
**Cần tích hợp:**
- VNPay / Momo / Stripe
- Payment flow:
  1. User click "Mua gói"
  2. Tạo payment request
  3. Redirect to payment gateway
  4. Callback verify payment
  5. Tạo subscription nếu success

#### **3. Roadmap Learning System** ❌
**Database cần thêm:**
- `roadmaps` table (subject_id, level, title)
- `roadmap_modules` table (roadmap_id, module_name, order_index, content)
- `user_module_progress` table (user_id, module_id, status, score)

**API cần tạo:**
- GET `/api/roadmaps/{subjectId}/{level}` - Get roadmap theo subject và level
- GET `/api/modules/{moduleId}` - Get module detail
- POST `/api/modules/{moduleId}/complete` - Đánh dấu module hoàn thành

#### **4. Mini Test System** ❌
**Database:**
- `mini_tests` table (module_id, questions)
- `mini_test_attempts` table (user_id, mini_test_id, score, passed)

**Logic:**
- Sau mỗi module → làm mini test
- Pass (>= 70%) → unlock next module
- Fail → suggest học lại

#### **5. Final Test System** ❌
**Database:**
- `final_tests` table (roadmap_id, questions)
- `final_test_attempts` table (user_id, final_test_id, score, passed)

**Logic:**
- Sau khi học hết roadmap → final test
- Pass → promote to next level
- Fail → replan (suggest học lại hoặc review)

#### **6. Profile Management** ❌
**API cần thêm:**
- GET `/api/users/profile` - Get user profile
- PUT `/api/users/profile` - Update profile (name, avatar, etc.)
- PUT `/api/users/change-password` - Change password

---

## 🗄️ II. DATABASE SCHEMA CHI TIẾT

### **Current Schema (đã có):**

```sql
-- Users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  full_name VARCHAR(255),
  password_hash VARCHAR(255),
  provider VARCHAR(255),
  provider_user_id VARCHAR(255),
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subjects
CREATE TABLE subjects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL
);

-- Question Bank
CREATE TABLE question_bank (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  level VARCHAR(10),
  skill_type VARCHAR(100),
  question TEXT NOT NULL,
  options JSON,
  correct_answer VARCHAR(10),
  explanation TEXT,
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- Placement Attempts
CREATE TABLE placement_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  paper_json TEXT,
  answers_json TEXT,
  total_score INT,
  score_percent DECIMAL(5,2),
  level VARCHAR(10),
  skill_analysis_json TEXT,
  completed BOOLEAN DEFAULT FALSE,
  submitted_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- Subscriptions
CREATE TABLE subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  payment_status VARCHAR(50),
  start_date TIMESTAMP,
  end_date TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- User Subjects (Tracking)
CREATE TABLE user_subjects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  current_level VARCHAR(10),
  is_unlocked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (subject_id) REFERENCES subjects(id)
);
```

### **Need to Add (Phase 2):**

```sql
-- 1. Thêm free_placement_remaining vào user_subjects
ALTER TABLE user_subjects
ADD COLUMN free_placement_remaining INT DEFAULT 1;

-- 2. Roadmaps
CREATE TABLE roadmaps (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  level VARCHAR(10) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (subject_id) REFERENCES subjects(id),
  UNIQUE KEY unique_subject_level (subject_id, level)
);

-- 3. Roadmap Modules
CREATE TABLE roadmap_modules (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  roadmap_id BIGINT NOT NULL,
  module_name VARCHAR(255) NOT NULL,
  order_index INT NOT NULL,
  content TEXT,
  video_url VARCHAR(500),
  duration_minutes INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id),
  UNIQUE KEY unique_roadmap_order (roadmap_id, order_index)
);

-- 4. Mini Tests
CREATE TABLE mini_tests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  module_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  questions_json TEXT NOT NULL,
  pass_threshold INT DEFAULT 70,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (module_id) REFERENCES roadmap_modules(id)
);

-- 5. Mini Test Attempts
CREATE TABLE mini_test_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  mini_test_id BIGINT NOT NULL,
  score INT NOT NULL,
  passed BOOLEAN NOT NULL,
  answers_json TEXT,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id)
);

-- 6. User Module Progress
CREATE TABLE user_module_progress (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  module_id BIGINT NOT NULL,
  status VARCHAR(50) DEFAULT 'NOT_STARTED',
  progress_percent INT DEFAULT 0,
  mini_test_score INT,
  completed_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (module_id) REFERENCES roadmap_modules(id),
  UNIQUE KEY unique_user_module (user_id, module_id)
);

-- 7. Final Tests
CREATE TABLE final_tests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  roadmap_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  questions_json TEXT NOT NULL,
  pass_threshold INT DEFAULT 75,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id)
);

-- 8. Final Test Attempts
CREATE TABLE final_test_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  final_test_id BIGINT NOT NULL,
  score INT NOT NULL,
  passed BOOLEAN NOT NULL,
  promoted BOOLEAN DEFAULT FALSE,
  answers_json TEXT,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (final_test_id) REFERENCES final_tests(id)
);

-- 9. Payments
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency VARCHAR(10) DEFAULT 'VND',
  payment_method VARCHAR(50),
  payment_gateway VARCHAR(50),
  transaction_id VARCHAR(255),
  status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  confirmed_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🔄 III. API ENDPOINTS CHI TIẾT

### **✅ Đã có (Current APIs):**

#### **Authentication:**
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/me` - Get current user
- `POST /api/auth/oauth/mock` - OAuth mock

#### **Subjects:**
- `GET /api/subjects` - List all subjects
- `GET /api/subjects/{id}` - Get subject by ID
- `GET /api/subjects/code/{code}` - Get subject by code

#### **Placement Tests:**
- `POST /api/subjects/{subjectId}/placement-tests` - Start test
- `POST /api/placement-attempts/{attemptId}/submit` - Submit test

#### **Subscriptions:**
- `POST /api/subscriptions/checkout` - Checkout subscription

#### **History:**
- `GET /api/history/placements` - Get placement history

#### **Admin:**
- `GET /api/admin/stats` - Dashboard stats
- `GET /api/admin/users` - List users
- `POST /api/dev/create-admin` - Create admin
- `POST /api/dev/set-admin/{userId}` - Set user as admin

---

### **❌ Cần thêm (Phase 2 APIs):**

#### **Profile Management:**
```
GET    /api/users/profile
PUT    /api/users/profile
PUT    /api/users/change-password
GET    /api/users/{userId}/subjects
```

#### **Free Placement Check:**
```
GET    /api/users/{userId}/subjects/{subjectId}/free-attempts
POST   /api/users/{userId}/subjects/{subjectId}/use-free-attempt
```

#### **Payment:**
```
POST   /api/payments/create
POST   /api/payments/verify
GET    /api/payments/{paymentId}/status
POST   /api/payments/callback/vnpay
POST   /api/payments/callback/momo
```

#### **Roadmaps:**
```
GET    /api/roadmaps/{subjectId}/{level}
GET    /api/roadmaps/{roadmapId}/modules
POST   /api/roadmaps (Admin only)
PUT    /api/roadmaps/{roadmapId} (Admin only)
```

#### **Modules:**
```
GET    /api/modules/{moduleId}
GET    /api/modules/{moduleId}/content
POST   /api/modules/{moduleId}/start
POST   /api/modules/{moduleId}/complete
GET    /api/users/me/modules/progress
```

#### **Mini Tests:**
```
GET    /api/mini-tests/{moduleId}
POST   /api/mini-tests/{testId}/start
POST   /api/mini-tests/{testId}/submit
GET    /api/mini-tests/{testId}/attempts
```

#### **Final Tests:**
```
GET    /api/final-tests/{roadmapId}
POST   /api/final-tests/{testId}/start
POST   /api/final-tests/{testId}/submit
POST   /api/final-tests/{testId}/promote
```

---

## 📋 IV. DEVELOPMENT ROADMAP

### **Phase 1: MVP (COMPLETED)** ✅
- [x] Authentication & Authorization
- [x] Subject listing
- [x] Placement test basic flow
- [x] Simple subscription
- [x] History tracking
- [x] Admin portal

### **Phase 2: Core Features (NEXT - 2 weeks)**

#### **Week 1: Payment & Free Attempts**
- [ ] Day 1-2: Implement free_placement_remaining logic
- [ ] Day 3-4: Payment gateway integration (VNPay)
- [ ] Day 5-7: Testing payment flow end-to-end

#### **Week 2: Roadmap Foundation**
- [ ] Day 8-9: Create Roadmap database tables
- [ ] Day 10-11: Roadmap CRUD APIs
- [ ] Day 12-14: Roadmap UI (modules list)

### **Phase 3: Learning System (3 weeks)**

#### **Week 3-4: Module Learning**
- [ ] Module content management
- [ ] Module progress tracking
- [ ] Video integration (if needed)

#### **Week 5: Mini Tests**
- [ ] Mini test generation
- [ ] Mini test submission & grading
- [ ] Unlock next module logic

### **Phase 4: Advanced Features (2 weeks)**

#### **Week 6: Final Tests**
- [ ] Final test system
- [ ] Promote/Replan logic
- [ ] Level up flow

#### **Week 7: Polish**
- [ ] Profile management
- [ ] Dashboard analytics
- [ ] Performance optimization

---

## 🏗️ V. TECHNICAL IMPLEMENTATION

### **Backend Services Needed:**

#### **1. PlacementService** (Update existing)
```java
- checkFreeAttempts(userId, subjectId)
- decrementFreeAttempts(userId, subjectId)
- generateTest(subjectId, level)
- calculateScore(attemptId, answers)
- assignLevel(score)
```

#### **2. PaymentService** (New)
```java
- createPayment(userId, amount, type)
- verifyPayment(transactionId)
- handleCallback(gatewayResponse)
- createSubscriptionAfterPayment(userId, subjectId)
```

#### **3. RoadmapService** (New)
```java
- assignRoadmap(userId, subjectId, level)
- getModulesByRoadmap(roadmapId)
- unlockNextModule(userId, moduleId)
- trackProgress(userId, moduleId, percent)
```

#### **4. MiniTestService** (New)
```java
- getMiniTest(moduleId)
- submitMiniTest(userId, testId, answers)
- evaluatePass(score, threshold)
- unlockNextIfPassed(userId, moduleId)
```

#### **5. FinalTestService** (New)
```java
- getFinalTest(roadmapId)
- submitFinalTest(userId, testId, answers)
- evaluatePromotion(score)
- promoteLevel(userId, subjectId)
- replanRoadmap(userId, subjectId)
```

---

## 📊 VI. BUSINESS LOGIC FLOWS

### **Flow 1: User Journey (Full)**
```
1. Landing Page
   ↓
2. Chọn môn (MATH/LITERATURE/ENGLISH)
   ↓
3. Check free_placement_remaining
   ├─ IF > 0 → Cho làm test (FREE)
   └─ IF = 0 → Redirect to Payment
   ↓
4. Làm Placement Test
   ↓
5. Chấm điểm + Gán Level (L1/L2/L3)
   ↓
6. Hiển thị Result + CTA "Mua gói"
   ↓
7. Payment Flow
   ├─ Payment Success → Create Subscription
   └─ Payment Failed → Retry
   ↓
8. Unlock Roadmap theo Level
   ↓
9. Học từng Module
   ├─ Module Content
   ├─ Mini Test
   └─ IF Pass → Next Module
   ↓
10. Final Test (sau khi học hết)
    ├─ IF Pass → Promote to next level
    └─ IF Fail → Replan
```

### **Flow 2: Free Placement Check**
```sql
-- Check before allowing test
SELECT free_placement_remaining 
FROM user_subjects
WHERE user_id = ? AND subject_id = ?

IF free_placement_remaining > 0:
  -- Allow test
  UPDATE user_subjects
  SET free_placement_remaining = free_placement_remaining - 1
  WHERE user_id = ? AND subject_id = ?
ELSE:
  -- Redirect to payment
  RETURN "Bạn đã hết lượt test miễn phí. Vui lòng mua gói."
```

### **Flow 3: Payment Integration**
```
1. User click "Mua gói"
2. Frontend call: POST /api/payments/create
   {
     "userId": 1,
     "amount": 299000,
     "type": "PLACEMENT_PACK",
     "subjectId": 1
   }
3. Backend create payment record
4. Backend generate payment URL (VNPay/Momo)
5. Frontend redirect to payment gateway
6. User complete payment
7. Gateway callback: POST /api/payments/callback/vnpay
8. Backend verify signature
9. IF valid:
   - Update payment status = "SUCCESS"
   - Create subscription
   - Set is_unlocked = true
   - Add free_placement_remaining += 3
10. Frontend redirect to success page
```

### **Flow 4: Module Learning**
```
1. User vào Roadmap page
2. Load modules by level:
   GET /api/roadmaps/{subjectId}/{level}
3. Display modules:
   - Module 1 (Unlocked)
   - Module 2 (Locked)
   - Module 3 (Locked)
4. User click Module 1
5. Show content (video, text, examples)
6. User click "Hoàn thành"
7. Show Mini Test
8. User làm test
9. Submit: POST /api/mini-tests/{testId}/submit
10. IF score >= 70%:
    - Mark module completed
    - Unlock Module 2
11. ELSE:
    - Suggest review
```

### **Flow 5: Promote/Replan**
```
1. User hoàn thành Final Test
2. Calculate score
3. IF score >= 75%:
   - Promote:
     UPDATE user_subjects
     SET current_level = 'L2'  -- from L1
     WHERE user_id = ? AND subject_id = ?
   - Assign new roadmap (L2)
4. ELSE:
   - Replan:
     - Suggest review weak modules
     - Allow retake final test
     - OR suggest continue at current level
```

---

## 🎯 VII. PRIORITY CHECKLIST

### **HIGH PRIORITY (Must Have - Phase 2)**
- [ ] Free placement attempts logic
- [ ] Payment gateway integration
- [ ] Roadmap database schema
- [ ] Module learning basic flow

### **MEDIUM PRIORITY (Should Have - Phase 3)**
- [ ] Mini test system
- [ ] Final test system
- [ ] Promote/Replan logic
- [ ] Profile management

### **LOW PRIORITY (Nice to Have - Phase 4)**
- [ ] Analytics dashboard
- [ ] Recommendation system
- [ ] Social features
- [ ] Mobile app

---

## 📞 VIII. NEXT STEPS

### **Ngay bây giờ:**
1. ✅ Review document này
2. ✅ Confirm business logic
3. ⏳ Bắt đầu Phase 2 development

### **Tuần này:**
1. Implement free_placement_remaining
2. Test payment gateway (sandbox)
3. Create roadmap tables

### **Tuần sau:**
1. Roadmap CRUD APIs
2. Module management
3. Frontend roadmap UI

---

**Tài liệu này là blueprint hoàn chỉnh cho CompassED v2.0**  
Mọi thay đổi sẽ được cập nhật vào file này.
