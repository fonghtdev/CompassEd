# 🎯 CompassED - Full Implementation Roadmap

## 📋 Tổng quan hệ thống

Hệ thống EdTech với:
- **3 môn học**: Toán, Văn, Anh
- **3 Levels**: L1 (Beginner), L2 (Intermediate), L3 (Advanced)
- **Placement Test** → **Roadmap Learning** → **Level Promotion**

---

## 🗄️ PHASE 1: DATABASE SCHEMA (Ưu tiên cao ⚡)

### ✅ Đã có (Hiện tại)
```sql
✓ users
✓ subjects  
✓ placement_attempts
✓ placement_results
✓ roadmaps
✓ subscriptions
✓ user_subject_free_attempts
✓ mini_tests
✓ final_tests
✓ final_test_attempts
✓ user_roadmap_assignments
```

### ❌ Cần thêm (Thiết yếu)

#### 1. **question_bank** (Core - Quan trọng nhất!)
```sql
CREATE TABLE question_bank (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL,
    level ENUM('L1', 'L2', 'L3') NOT NULL,
    skill_type VARCHAR(100) NOT NULL,  -- 'Đại số', 'Hình học', 'Ngữ pháp', 'Reading'...
    question_type ENUM('MULTIPLE_CHOICE', 'ESSAY', 'TRUE_FALSE') DEFAULT 'MULTIPLE_CHOICE',
    question_text TEXT NOT NULL,
    options JSON,  -- ["A. ...", "B. ...", "C. ...", "D. ..."]
    correct_answer VARCHAR(10),  -- "A" hoặc "B,C" nếu nhiều đáp án
    explanation TEXT,
    difficulty INT DEFAULT 1,  -- 1-5
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

-- Index để query nhanh
CREATE INDEX idx_qb_subject_level ON question_bank(subject_id, level);
CREATE INDEX idx_qb_skill ON question_bank(skill_type);
```

#### 2. **user_subjects** (Tracking học viên)
```sql
CREATE TABLE user_subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    current_level ENUM('L1', 'L2', 'L3') DEFAULT 'L1',
    is_unlocked BOOLEAN DEFAULT FALSE,
    free_placement_remaining INT DEFAULT 1,
    placement_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, subject_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);
```

#### 3. **roadmap_modules** (Chi tiết module trong roadmap)
```sql
CREATE TABLE roadmap_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    roadmap_id BIGINT NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    content_type ENUM('VIDEO', 'DOCUMENT', 'QUIZ', 'EXERCISE') DEFAULT 'VIDEO',
    content_url VARCHAR(500),
    duration_minutes INT,  -- Thời lượng học
    is_required BOOLEAN DEFAULT TRUE,
    pass_score INT DEFAULT 60,  -- Điểm tối thiểu để qua
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id)
);
```

#### 4. **user_module_progress** (Tiến độ học)
```sql
CREATE TABLE user_module_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'NOT_STARTED',
    score INT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE KEY (user_id, module_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (module_id) REFERENCES roadmap_modules(id)
);
```

#### 5. **mini_test_questions** (Link mini test với câu hỏi)
```sql
CREATE TABLE mini_test_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mini_test_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    order_index INT NOT NULL,
    FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id),
    FOREIGN KEY (question_id) REFERENCES question_bank(id)
);
```

#### 6. **mini_test_attempts** (Lịch sử làm mini test)
```sql
CREATE TABLE mini_test_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    mini_test_id BIGINT NOT NULL,
    score DOUBLE,
    max_score DOUBLE,
    passed BOOLEAN DEFAULT FALSE,
    answers JSON,  -- {"1": "A", "2": "B", ...}
    started_at TIMESTAMP,
    submitted_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mini_test_id) REFERENCES mini_tests(id)
);
```

#### 7. **payments** (Lịch sử thanh toán)
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    payment_method VARCHAR(50),  -- 'VNPAY', 'MOMO', 'BANK_TRANSFER'
    transaction_id VARCHAR(255),
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);
```

#### 8. **subject_config** (Cấu hình môn học)
```sql
CREATE TABLE subject_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_id BIGINT NOT NULL UNIQUE,
    free_placement_count INT DEFAULT 1,
    placement_price DECIMAL(10,2) DEFAULT 99000,
    subscription_price DECIMAL(10,2) DEFAULT 299000,
    subscription_duration_days INT DEFAULT 365,
    placement_question_count INT DEFAULT 20,
    placement_pass_score INT DEFAULT 60,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
);
```

#### 9. **skill_analysis** (Phân tích kỹ năng chi tiết)
```sql
CREATE TABLE skill_analysis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    placement_result_id BIGINT,
    skill_name VARCHAR(100) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL,
    score_percent DOUBLE,
    level_recommendation ENUM('L1', 'L2', 'L3'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (placement_result_id) REFERENCES placement_results(id)
);
```

#### 10. **admin_logs** (Log hoạt động admin)
```sql
CREATE TABLE admin_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,  -- 'CREATE_QUESTION', 'UPDATE_ROADMAP'
    target_type VARCHAR(50),  -- 'QUESTION', 'USER', 'SUBSCRIPTION'
    target_id BIGINT,
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);
```

---

## 🏗️ PHASE 2: BACKEND ENTITIES & REPOSITORIES

### Cần tạo các Entity mới:

```java
✓ User.java (đã có)
✓ Subject.java (đã có)
✓ Roadmap.java (đã có)
✓ PlacementAttempt.java (đã có)
✓ PlacementResult.java (đã có)
✓ Subscription.java (đã có)

❌ QuestionBank.java (MỚI)
❌ UserSubject.java (MỚI)
❌ RoadmapModule.java (MỚI)
❌ UserModuleProgress.java (MỚI)
❌ MiniTestQuestion.java (MỚI)
❌ MiniTestAttempt.java (MỚI)
❌ Payment.java (MỚI)
❌ SubjectConfig.java (MỚI)
❌ SkillAnalysis.java (MỚI)
❌ AdminLog.java (MỚI)
```

### Repository cần tạo:
```java
QuestionBankRepository.java
UserSubjectRepository.java
RoadmapModuleRepository.java
UserModuleProgressRepository.java
MiniTestQuestionRepository.java
MiniTestAttemptRepository.java
PaymentRepository.java
SubjectConfigRepository.java
SkillAnalysisRepository.java
AdminLogRepository.java
```

---

## 🎯 PHASE 3: CORE BUSINESS LOGIC

### 1. **PlacementTestService** (Ưu tiên 1)
```java
// Chức năng:
- checkFreeAttempt(userId, subjectId) → boolean
- generatePlacementTest(subjectId, level?) → List<Question>
- submitTest(attemptId, answers) → PlacementResult
- calculateScore(answers, correctAnswers) → Score
- analyzeSkills(answers) → Map<Skill, Score>
- assignLevel(scorePercent) → Level
- saveResult(userId, subjectId, result) → PlacementResult
```

**Logic sinh đề:**
```sql
SELECT * FROM question_bank
WHERE subject_id = ?
  AND is_active = TRUE
ORDER BY RAND()
LIMIT 20
```

**Logic phân tích skill:**
```java
Map<String, SkillScore> analyzeSkills(answers) {
    // Group câu hỏi theo skill
    // Tính % đúng mỗi skill
    // Return: {"Đại số": 80%, "Hình học": 60%}
}
```

**Mapping level:**
```java
Level assignLevel(double scorePercent) {
    if (scorePercent >= 76) return Level.L3;
    if (scorePercent >= 41) return Level.L2;
    return Level.L1;
}
```

---

### 2. **SubscriptionService** (Ưu tiên 2)
```java
- createSubscription(userId, subjectId, packageType)
- unlockSubject(userId, subjectId)
- checkSubscriptionStatus(userId, subjectId) → boolean
- renewSubscription(subscriptionId)
- cancelSubscription(subscriptionId)
```

---

### 3. **PaymentService** (Ưu tiên 2)
```java
- createPayment(userId, amount, purpose)
- processPaymentCallback(transactionData)
- verifyPaymentSignature(data)
- updatePaymentStatus(paymentId, status)
- createSubscriptionAfterPayment(paymentId)
```

**Integration cần có:**
- VNPay API
- Momo API
- Callback handler

---

### 4. **RoadmapService** (Ưu tiên 3)
```java
- assignRoadmap(userId, subjectId, level)
- getRoadmapWithProgress(userId, roadmapId)
- unlockNextModule(userId, moduleId)
- checkModuleCompletion(userId, moduleId) → boolean
```

---

### 5. **MiniTestService** (Ưu tiên 4)
```java
- generateMiniTest(moduleId) → MiniTest
- submitMiniTest(attemptId, answers)
- evaluateMiniTest(attemptId) → Result
- checkPassStatus(score, passScore) → boolean
```

---

### 6. **FinalTestService** (Ưu tiên 5)
```java
- generateFinalTest(roadmapId)
- submitFinalTest(attemptId, answers)
- evaluateFinalTest(attemptId) → Result
- promoteLevel(userId, subjectId) → newLevel
- replan(userId, subjectId, reason)
```

**Logic Promote:**
```java
if (finalTestScore >= 70) {
    currentLevel++;
    assignRoadmap(userId, subjectId, currentLevel);
} else {
    // Replan: học lại hoặc điều chỉnh roadmap
}
```

---

## 🎨 PHASE 4: ADMIN FEATURES

### Admin Controllers cần tạo:

```java
1. AdminUserController
   - GET /api/admin/users
   - GET /api/admin/users/{id}
   - PUT /api/admin/users/{id}/reset-level
   - PUT /api/admin/users/{id}/reset-free-attempts
   - DELETE /api/admin/users/{id}/block

2. AdminSubjectController
   - POST /api/admin/subjects
   - PUT /api/admin/subjects/{id}
   - GET /api/admin/subjects/{id}/config
   - PUT /api/admin/subjects/{id}/config

3. AdminQuestionBankController ⚡ (Quan trọng nhất!)
   - GET /api/admin/questions
   - POST /api/admin/questions
   - PUT /api/admin/questions/{id}
   - DELETE /api/admin/questions/{id}
   - POST /api/admin/questions/import-excel
   - GET /api/admin/questions/stats

4. AdminRoadmapController
   - GET /api/admin/roadmaps
   - POST /api/admin/roadmaps
   - PUT /api/admin/roadmaps/{id}
   - POST /api/admin/roadmaps/{id}/modules
   - PUT /api/admin/modules/{id}
   - DELETE /api/admin/modules/{id}

5. AdminPaymentController
   - GET /api/admin/payments
   - GET /api/admin/payments/{id}
   - PUT /api/admin/payments/{id}/refund
   - GET /api/admin/payments/stats

6. AdminDashboardController
   - GET /api/admin/dashboard/stats
   - GET /api/admin/dashboard/conversion-rate
   - GET /api/admin/dashboard/revenue
```

---

## 📊 PHASE 5: DASHBOARD & ANALYTICS

### Metrics cần track:

```java
1. User Metrics
   - Total users
   - Active users (7 days)
   - Users by level (L1/L2/L3)
   - Users by subject

2. Conversion Metrics
   - Placement → Subscription rate
   - Free → Paid conversion
   - Trial → Paid conversion

3. Learning Metrics
   - Module completion rate
   - Mini test pass rate
   - Final test pass rate
   - Average study time

4. Financial Metrics
   - Total revenue
   - Revenue by subject
   - Revenue by month
   - Average order value

5. Content Metrics
   - Questions per subject/level/skill
   - Most difficult questions
   - Question usage statistics
```

---

## 🚀 IMPLEMENTATION ORDER (Bắt đầu từ đây!)

### ✅ Sprint 1 (Week 1-2): Core Foundation
```
1. ✓ Fix placement test bug (DONE)
2. ✓ Add hot reload (DONE)
3. ❌ Create all database tables
4. ❌ Create all Entity classes
5. ❌ Create all Repository classes
6. ❌ Seed initial data (subjects, config)
```

### ⏳ Sprint 2 (Week 3-4): Question Bank System
```
1. Create QuestionBank entity & repository
2. Admin: CRUD questions
3. Admin: Import Excel questions
4. Admin: Question statistics
5. Placement test integration with real questions
```

### ⏳ Sprint 3 (Week 5-6): Payment & Subscription
```
1. Payment entity & repository
2. VNPay integration
3. Payment callback handler
4. Subscription creation after payment
5. Subject unlock logic
```

### ⏳ Sprint 4 (Week 7-8): Roadmap System
```
1. RoadmapModule entity
2. UserModuleProgress tracking
3. Module learning flow
4. Unlock next module logic
```

### ⏳ Sprint 5 (Week 9-10): Mini Test System
```
1. Mini test generation
2. Mini test submission
3. Auto scoring
4. Pass/Fail logic
```

### ⏳ Sprint 6 (Week 11-12): Final Test & Promotion
```
1. Final test generation
2. Final test evaluation
3. Level promotion logic
4. Replan system
```

### ⏳ Sprint 7 (Week 13-14): Admin Dashboard
```
1. Dashboard statistics
2. Analytics charts
3. User management panel
4. Payment tracking
```

---

## 🔧 TECHNICAL STACK

### Backend:
- ✓ Spring Boot 4.0.1
- ✓ MySQL 9.6
- ✓ JPA/Hibernate
- ✓ Spring Security
- ❌ Spring DevTools (added)

### Frontend:
- ✓ HTML/CSS/JavaScript
- ✓ Tailwind CSS
- ✓ Flask (Python)

### Tools needed:
- ❌ Apache POI (Excel import)
- ❌ VNPay SDK
- ❌ Momo SDK (optional)
- ❌ Chart.js (analytics)

---

## 📝 SAMPLE DATA STRUCTURE

### Question Bank Example:
```json
{
  "id": 1,
  "subject_id": 1,
  "level": "L1",
  "skill_type": "Đại số",
  "question_type": "MULTIPLE_CHOICE",
  "question_text": "Tính: 2 + 3 = ?",
  "options": ["A. 4", "B. 5", "C. 6", "D. 7"],
  "correct_answer": "B",
  "explanation": "2 cộng 3 bằng 5",
  "difficulty": 1
}
```

### Placement Test Paper:
```json
{
  "subject": "MATH",
  "level": "L1",
  "questions": [
    {
      "id": 1,
      "question": "...",
      "options": [...],
      "skill": "Đại số"
    },
    // ... 19 questions more
  ]
}
```

---

## 🎯 SUCCESS METRICS

Hệ thống hoàn chỉnh khi:
- ✅ User có thể đăng ký, đăng nhập
- ✅ User làm placement test miễn phí
- ✅ System tự động chấm điểm, phân tích skill
- ✅ System gán level chính xác
- ✅ User thanh toán được qua VNPay
- ✅ Subscription tự động unlock môn học
- ✅ Roadmap hiển thị đúng theo level
- ✅ User học theo module, làm mini test
- ✅ Final test promote level tự động
- ✅ Admin quản lý toàn bộ hệ thống
- ✅ Dashboard hiển thị analytics

---

## 📞 NEXT STEPS

**Bắt đầu ngay:**
1. Tạo file SQL migration cho tất cả tables
2. Generate Entity classes
3. Tạo sample data cho question_bank
4. Implement PlacementTestService với real data

**Bạn muốn tôi bắt đầu với phần nào trước?**
- [ ] Tạo SQL migration scripts
- [ ] Generate Entity classes
- [ ] Implement QuestionBankService
- [ ] Create Admin API for questions
