// Quiz Data
const quizData = [
    {
        section: 1,
        sectionTitle: "Phần 1: Kỹ Năng Tư Duy & Giải Quyết Vấn Đề",
        questions: [
            {
                question: "Khi gặp một vấn đề khó, bạn thường:",
                options: [
                    { text: "Phân tích nguyên nhân, tìm giải pháp logic", value: "analytical" },
                    { text: "Hỏi ý kiến người khác", value: "collaborative" },
                    { text: "Thử nhiều cách sáng tạo khác nhau", value: "creative" },
                    { text: "Để thời gian rồi quay lại giải sau", value: "reflective" }
                ]
            },
            {
                question: "Bạn giỏi nhất trong việc:",
                options: [
                    { text: "Giải toán, lập luận logic", value: "analytical" },
                    { text: "Viết, kể chuyện, diễn đạt ý tưởng", value: "communicative" },
                    { text: "Vẽ, tưởng tượng, sáng tạo", value: "creative" },
                    { text: "Làm việc thực tế, sửa chữa, thử nghiệm", value: "practical" }
                ]
            },
            {
                question: "Bạn cảm thấy hứng thú nhất khi:",
                options: [
                    { text: "Tìm ra cách làm mới để giải bài tập", value: "analytical" },
                    { text: "Thuyết trình, trình bày ý tưởng", value: "communicative" },
                    { text: "Thiết kế, sáng tạo nội dung", value: "creative" },
                    { text: "Làm dự án thực tế cùng nhóm", value: "practical" }
                ]
            },
            {
                question: "Khi được giao nhiệm vụ, bạn thường:",
                options: [
                    { text: "Lập kế hoạch chi tiết trước khi làm", value: "analytical" },
                    { text: "Hành động ngay để thử nghiệm", value: "practical" },
                    { text: "Thảo luận với người khác để cùng làm", value: "collaborative" },
                    { text: "Nghĩ cách mới, độc đáo hơn", value: "creative" }
                ]
            },
            {
                question: "Bạn thường đạt kết quả tốt nhất khi:",
                options: [
                    { text: "Có thời gian chuẩn bị kỹ", value: "analytical" },
                    { text: "Làm việc nhóm và chia sẻ ý kiến", value: "collaborative" },
                    { text: "Làm việc độc lập, sáng tạo", value: "creative" },
                    { text: "Làm thực hành, thí nghiệm", value: "practical" }
                ]
            }
        ]
    },
    {
        section: 2,
        sectionTitle: "Phần 2: Kỹ Năng Giao Tiếp & Ứng Xử",
        questions: [
            {
                question: "Khi thuyết trình trước lớp, bạn cảm thấy:",
                options: [
                    { text: "Tự tin, thích nói trước đám đông", value: "leadership" },
                    { text: "Hồi hộp nhưng vẫn muốn thử", value: "communicative" },
                    { text: "Lo lắng, thích làm hậu trường hơn", value: "reflective" },
                    { text: "Không thích nói trước người khác", value: "introverted" }
                ]
            },
            {
                question: "Khi người khác không đồng ý với bạn, bạn thường:",
                options: [
                    { text: "Giải thích lại bằng lập luận rõ ràng", value: "analytical" },
                    { text: "Cố gắng lắng nghe và thỏa hiệp", value: "collaborative" },
                    { text: "Bỏ qua, không tranh luận", value: "reflective" },
                    { text: "Giữ vững ý kiến đến cùng", value: "leadership" }
                ]
            },
            {
                question: "Bạn thích công việc yêu cầu:",
                options: [
                    { text: "Gặp gỡ, nói chuyện với nhiều người", value: "communicative" },
                    { text: "Viết – soạn thảo – truyền đạt thông tin", value: "communicative" },
                    { text: "Quan sát, phân tích dữ liệu", value: "analytical" },
                    { text: "Làm việc với máy móc hoặc phần mềm", value: "technical" }
                ]
            },
            {
                question: "Trong nhóm bạn bè, bạn thường là:",
                options: [
                    { text: "Người lãnh đạo, điều phối", value: "leadership" },
                    { text: "Người kết nối, giao tiếp", value: "communicative" },
                    { text: "Người sáng tạo, đưa ý tưởng", value: "creative" },
                    { text: "Người hoàn thành công việc tỉ mỉ", value: "practical" }
                ]
            },
            {
                question: "Mọi người thường nhận xét bạn là người:",
                options: [
                    { text: "Tự tin, năng động", value: "leadership" },
                    { text: "Thân thiện, dễ gần", value: "communicative" },
                    { text: "Sáng tạo, khác biệt", value: "creative" },
                    { text: "Trầm tính, chu đáo", value: "analytical" }
                ]
            }
        ]
    },
    {
        section: 3,
        sectionTitle: "Phần 3: Kỹ Năng Học Tập & Tư Duy Học Thuật",
        questions: [
            {
                question: "Bạn học tốt nhất khi:",
                options: [
                    { text: "Có ví dụ thực tế, minh họa cụ thể", value: "practical" },
                    { text: "Thầy cô giảng chi tiết, tuần tự", value: "analytical" },
                    { text: "Được tự nghiên cứu, khám phá", value: "creative" },
                    { text: "Học nhóm, trao đổi với bạn bè", value: "collaborative" }
                ]
            },
            {
                question: "Khi ôn thi, bạn thường:",
                options: [
                    { text: "Lập kế hoạch chi tiết từng ngày", value: "analytical" },
                    { text: "Làm thử đề liên tục", value: "practical" },
                    { text: "Học bằng hình ảnh, sơ đồ", value: "creative" },
                    { text: "Học theo cảm hứng", value: "reflective" }
                ]
            },
            {
                question: "Bạn có khả năng:",
                options: [
                    { text: "Ghi nhớ tốt các công thức, sự kiện", value: "analytical" },
                    { text: "Hiểu nhanh bản chất vấn đề", value: "analytical" },
                    { text: "Phát hiện lỗi, chi tiết sai", value: "reflective" },
                    { text: "Tạo ra cách làm mới mẻ", value: "creative" }
                ]
            },
            {
                question: "Bạn thường hứng thú với loại câu hỏi:",
                options: [
                    { text: "Trắc nghiệm logic", value: "analytical" },
                    { text: "Câu hỏi mở – viết tự luận", value: "communicative" },
                    { text: "Câu hỏi sáng tạo, gợi mở", value: "creative" },
                    { text: "Thực hành / dự án", value: "practical" }
                ]
            },
            {
                question: "Bạn thường dùng thời gian rảnh để:",
                options: [
                    { text: "Tìm hiểu công nghệ / game / app mới", value: "technical" },
                    { text: "Viết blog, đọc sách, xem tin tức", value: "communicative" },
                    { text: "Vẽ, làm video, thiết kế", value: "creative" },
                    { text: "Thử nghiệm, lắp ráp đồ vật", value: "practical" }
                ]
            }
        ]
    },
    {
        section: 4,
        sectionTitle: "Phần 4: Kỹ Năng Làm Việc & Tổ Chức",
        questions: [
            {
                question: "Khi làm việc nhóm, bạn ưu tiên:",
                options: [
                    { text: "Hiệu quả và tiến độ công việc", value: "leadership" },
                    { text: "Sự hài hòa, tránh mâu thuẫn", value: "collaborative" },
                    { text: "Sự sáng tạo và linh hoạt", value: "creative" },
                    { text: "Kết quả cuối cùng, dù cách nào", value: "practical" }
                ]
            },
            {
                question: "Bạn thích vai trò nào trong dự án nhóm?",
                options: [
                    { text: "Trưởng nhóm / lãnh đạo", value: "leadership" },
                    { text: "Người giao tiếp, điều phối", value: "communicative" },
                    { text: "Người thiết kế, sáng tạo", value: "creative" },
                    { text: "Người phân tích, xử lý dữ liệu", value: "analytical" }
                ]
            },
            {
                question: "Khi có xung đột trong nhóm, bạn sẽ:",
                options: [
                    { text: "Giải quyết thẳng thắn", value: "leadership" },
                    { text: "Lắng nghe, làm trung gian", value: "collaborative" },
                    { text: "Né tránh, im lặng", value: "introverted" },
                    { text: "Tập trung vào công việc để tránh căng thẳng", value: "practical" }
                ]
            },
            {
                question: "Bạn thường lập kế hoạch cho công việc như thế nào?",
                options: [
                    { text: "Ghi rõ từng bước, mục tiêu cụ thể", value: "analytical" },
                    { text: "Có định hướng chung nhưng linh hoạt", value: "creative" },
                    { text: "Làm theo cảm hứng, không ràng buộc", value: "reflective" },
                    { text: "Chờ người khác phân công", value: "collaborative" }
                ]
            },
            {
                question: "Bạn thường hoàn thành công việc ở mức độ:",
                options: [
                    { text: "Trước thời hạn", value: "analytical" },
                    { text: "Đúng hạn", value: "practical" },
                    { text: "Sát giờ nộp", value: "reflective" },
                    { text: "Thường bị trễ hạn", value: "creative" }
                ]
            }
        ]
    },
    {
        section: 5,
        sectionTitle: "Phần 5: Kỹ Năng Sáng Tạo & Công Nghệ",
        questions: [
            {
                question: "Bạn thấy mình phù hợp nhất với kiểu công việc nào?",
                options: [
                    { text: "Quản lý, điều phối, lãnh đạo", value: "leadership" },
                    { text: "Viết, biên tập, truyền thông", value: "communicative" },
                    { text: "Lập trình, phân tích dữ liệu", value: "analytical" },
                    { text: "Thiết kế, sáng tạo nghệ thuật", value: "creative" }
                ]
            },
            {
                question: "Khi có thiết bị / phần mềm mới, bạn thường:",
                options: [
                    { text: "Tò mò, tìm hiểu ngay cách dùng", value: "technical" },
                    { text: "Cần người hướng dẫn", value: "collaborative" },
                    { text: "Không quan tâm lắm", value: "introverted" },
                    { text: "Thích xem người khác thử trước", value: "reflective" }
                ]
            },
            {
                question: "Bạn thường nảy ra ý tưởng mới khi:",
                options: [
                    { text: "Quan sát vấn đề thực tế", value: "practical" },
                    { text: "Thảo luận với người khác", value: "collaborative" },
                    { text: "Xem video, phim ảnh, nghệ thuật", value: "creative" },
                    { text: "Một mình suy nghĩ, tưởng tượng", value: "reflective" }
                ]
            },
            {
                question: "Bạn giỏi nhất trong việc tạo ra:",
                options: [
                    { text: "Giải pháp logic, tối ưu", value: "analytical" },
                    { text: "Câu chuyện, nội dung hấp dẫn", value: "communicative" },
                    { text: "Hình ảnh, sản phẩm trực quan", value: "creative" },
                    { text: "Ứng dụng thực tế, mô hình hoạt động", value: "practical" }
                ]
            },
            {
                question: "Bạn thích tham gia cuộc thi nào hơn?",
                options: [
                    { text: "Kinh doanh / khởi nghiệp", value: "leadership" },
                    { text: "Hùng biện / viết luận", value: "communicative" },
                    { text: "Sáng tạo nghệ thuật / thiết kế", value: "creative" },
                    { text: "Khoa học kỹ thuật / công nghệ", value: "technical" }
                ]
            }
        ]
    },
    {
        section: 6,
        sectionTitle: "Phần 6: Kỹ Năng Lãnh Đạo & Thích Ứng",
        questions: [
            {
                question: "Khi được giao việc khó, bạn sẽ:",
                options: [
                    { text: "Nhận thử thách và tìm cách làm", value: "leadership" },
                    { text: "Hỏi ý kiến người có kinh nghiệm", value: "collaborative" },
                    { text: "Cảm thấy lo lắng, cần hỗ trợ", value: "reflective" },
                    { text: "Trì hoãn hoặc né tránh", value: "introverted" }
                ]
            },
            {
                question: "Bạn thích:",
                options: [
                    { text: "Là người ra quyết định", value: "leadership" },
                    { text: "Là người hỗ trợ trong đội", value: "collaborative" },
                    { text: "Là người sáng tạo giải pháp", value: "creative" },
                    { text: "Là người thực hiện tỉ mỉ", value: "practical" }
                ]
            },
            {
                question: "Khi môi trường học thay đổi, bạn thường:",
                options: [
                    { text: "Thích nghi nhanh, chủ động", value: "leadership" },
                    { text: "Mất vài ngày để quen", value: "reflective" },
                    { text: "Khó thích nghi, cần thời gian dài", value: "introverted" },
                    { text: "Cảm thấy không thoải mái", value: "introverted" }
                ]
            },
            {
                question: "Trong vai trò lãnh đạo nhóm, bạn chú trọng:",
                options: [
                    { text: "Hiệu quả, kết quả", value: "leadership" },
                    { text: "Tinh thần, sự đoàn kết", value: "collaborative" },
                    { text: "Cách làm sáng tạo", value: "creative" },
                    { text: "Quy trình rõ ràng, khoa học", value: "analytical" }
                ]
            },
            {
                question: "Khi làm việc với người có ý kiến khác, bạn:",
                options: [
                    { text: "Tôn trọng và học hỏi", value: "collaborative" },
                    { text: "Cố gắng thuyết phục họ", value: "analytical" },
                    { text: "Giữ vững quan điểm cá nhân", value: "leadership" },
                    { text: "Tránh tranh luận", value: "introverted" }
                ]
            }
        ]
    }
];

// Quiz State
let currentQuestionIndex = 0;
let currentSectionIndex = 0;
let answers = {};
let scores = {
    analytical: 0,
    collaborative: 0,
    creative: 0,
    practical: 0,
    communicative: 0,
    leadership: 0,
    technical: 0,
    reflective: 0,
    introverted: 0
};

// Get total questions count
function getTotalQuestions() {
    return quizData.reduce((sum, section) => sum + section.questions.length, 0);
}

// Initialize Quiz
function initQuiz() {
    const totalQuestionsEl = document.getElementById('totalQuestions');
    if (totalQuestionsEl) {
        totalQuestionsEl.textContent = getTotalQuestions();
    }
    loadQuestion();
}

// Load Current Question
function loadQuestion() {
    const section = quizData[currentSectionIndex];
    const question = section.questions[currentQuestionIndex];

    // Update section badge
    const badge = document.getElementById('sectionBadge');
    badge.className = 'section-badge';
    badge.textContent = section.sectionTitle;

    // Update question text
    document.getElementById('questionText').textContent = question.question;

    // Update question number
    const globalQuestionNumber = quizData.slice(0, currentSectionIndex)
        .reduce((sum, s) => sum + s.questions.length, 0) + currentQuestionIndex + 1;
    
    const questionNumberEl = document.getElementById('questionNumber');
    if (questionNumberEl) {
        questionNumberEl.textContent = globalQuestionNumber;
    }

    // Update progress bar
    const totalQuestions = getTotalQuestions();
    const progressPercent = Math.round((globalQuestionNumber / totalQuestions) * 100);
    
    const progressBar = document.getElementById('progressBar');
    const progressPercentage = document.getElementById('progressPercentage');
    
    if (progressBar) {
        progressBar.style.width = progressPercent + '%';
    }
    if (progressPercentage) {
        progressPercentage.textContent = progressPercent + '%';
    }

    // Load options
    loadOptions(question.options);

    // Update buttons
    updateButtons();
}

// Load Options
function loadOptions(options) {
    const container = document.getElementById('optionsContainer');
    container.innerHTML = '';

    options.forEach((option, index) => {
        const card = document.createElement('div');
        card.className = 'option-card';

        const content = document.createElement('div');
        content.className = 'option-content';

        const radio = document.createElement('div');
        radio.className = 'option-radio';

        const text = document.createElement('div');
        text.className = 'option-text';
        text.textContent = option.text;

        content.appendChild(radio);
        content.appendChild(text);
        card.appendChild(content);

        // Check if this option was previously selected
        const answerKey = `${currentSectionIndex}-${currentQuestionIndex}`;
        if (answers[answerKey] && answers[answerKey] === option.value) {
            card.classList.add('selected');
        }

        // Add click event
        card.addEventListener('click', function() {
            document.querySelectorAll('.option-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            answers[answerKey] = option.value;
        });

        container.appendChild(card);
    });
}

// Update Button States
function updateButtons() {
    const totalQuestions = getTotalQuestions();
    const globalQuestionNumber = quizData.slice(0, currentSectionIndex)
        .reduce((sum, s) => sum + s.questions.length, 0) + currentQuestionIndex + 1;

    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');

    // Previous button
    if (prevBtn) {
        prevBtn.disabled = globalQuestionNumber === 1;
    }

    // Next button and submit button
    if (globalQuestionNumber === totalQuestions) {
        if (nextBtn) nextBtn.classList.add('d-none');
        if (submitBtn) submitBtn.classList.remove('d-none');
    } else {
        if (nextBtn) nextBtn.classList.remove('d-none');
        if (submitBtn) submitBtn.classList.add('d-none');
    }
}

// Next Question
function nextQuestion() {
    const answerKey = `${currentSectionIndex}-${currentQuestionIndex}`;
    if (!answers[answerKey]) {
        alert('Vui lòng chọn một câu trả lời!');
        return;
    }

    if (currentQuestionIndex < quizData[currentSectionIndex].questions.length - 1) {
        currentQuestionIndex++;
    } else if (currentSectionIndex < quizData.length - 1) {
        currentSectionIndex++;
        currentQuestionIndex = 0;
    }

    loadQuestion();
}

// Previous Question
function previousQuestion() {
    if (currentQuestionIndex > 0) {
        currentQuestionIndex--;
    } else if (currentSectionIndex > 0) {
        currentSectionIndex--;
        currentQuestionIndex = quizData[currentSectionIndex].questions.length - 1;
    }

    loadQuestion();
}

// Submit Quiz
function submitQuiz() {
    // Calculate scores
    scores = {
        analytical: 0,
        collaborative: 0,
        creative: 0,
        practical: 0,
        communicative: 0,
        leadership: 0,
        technical: 0,
        reflective: 0,
        introverted: 0
    };

    Object.values(answers).forEach(answer => {
        if (scores.hasOwnProperty(answer)) {
            scores[answer]++;
        }
    });

    // Calculate group scores
    const groupScores = calculateGroupScores();
    const total = Object.values(groupScores).reduce((a, b) => a + b, 0);
    const sortedGroups = Object.keys(groupScores).sort((a, b) => groupScores[b] - groupScores[a]);

    // Save to localStorage
    const resultsData = {
        groupScores: groupScores,
        sortedGroups: sortedGroups,
        total: total,
        timestamp: new Date().toISOString()
    };
    
    localStorage.setItem('surveyResults', JSON.stringify(resultsData));

    // Redirect to results page
    window.location.href = 'result.html';
}

// Career Groups
const careerGroups = {
    A: {
        name: 'Nhóm A: Kinh Tế - Kinh Doanh',
        traits: ['analytical', 'leadership', 'practical'],
        description: 'Bạn có khả năng phân tích, lãnh đạo và định hướng kinh doanh tốt. Phù hợp với các ngành quản lý, tài chính, marketing.',
        careers: ['Quản Trị Kinh Doanh', 'Tài Chính - Ngân Hàng', 'Marketing', 'Kế Toán', 'Kinh Tế Học'],
        universities: ['ĐH Kinh Tế Quốc Dân', 'ĐH Ngoại Thương', 'ĐH Kinh Tế TP.HCM'],
        color: '#22c55e',
        icon: 'fa-chart-line',
        strengths: ['Tư duy phân tích chiến lược', 'Kỹ năng lãnh đạo và quản lý', 'Định hướng mục tiêu rõ ràng'],
        suggestions: ['Tham gia câu lạc bộ khởi nghiệp', 'Học các khóa Marketing & Business', 'Thực tập tại các công ty kinh doanh']
    },
    B: {
        name: 'Nhóm B: Kỹ Thuật - Công Nghệ',
        traits: ['analytical', 'technical', 'practical'],
        description: 'Bạn có tư duy logic, giỏi công nghệ và kỹ thuật. Phù hợp với các ngành lập trình, kỹ sư, khoa học máy tính.',
        careers: ['Công Nghệ Thông Tin', 'Khoa Học Máy Tính', 'Kỹ Thuật Điện - Điện Tử', 'Cơ Khí', 'Xây Dựng'],
        universities: ['ĐH Bách Khoa Hà Nội', 'ĐH Bách Khoa TP.HCM', 'ĐH Công Nghệ - ĐHQGHN'],
        color: '#3b82f6',
        icon: 'fa-laptop-code',
        strengths: ['Tư duy logic và kỹ thuật', 'Ham học hỏi công nghệ mới', 'Giải quyết vấn đề sáng tạo'],
        suggestions: ['Tham gia CLB STEM / Robotics', 'Tự học lập trình (FreeCodeCamp, W3Schools)', 'Tìm hiểu về AI, Machine Learning']
    },
    C: {
        name: 'Nhóm C: Ngôn Ngữ - Truyền Thông',
        traits: ['communicative', 'creative', 'collaborative'],
        description: 'Bạn có khả năng giao tiếp tốt, sáng tạo trong ngôn từ. Phù hợp với các ngành báo chí, ngoại ngữ, quan hệ công chúng.',
        careers: ['Báo Chí - Truyền Thông', 'Ngôn Ngữ Anh', 'Quan Hệ Công Chúng', 'Biên - Phiên Dịch', 'Ngôn Ngữ Học'],
        universities: ['ĐH Khoa Học Xã Hội và Nhân Văn', 'ĐH Ngoại Ngữ - ĐHQGHN', 'Học Viện Báo Chí'],
        color: '#eab308',
        icon: 'fa-comments',
        strengths: ['Giao tiếp và diễn đạt xuất sắc', 'Sáng tạo nội dung hấp dẫn', 'Kỹ năng làm việc nhóm tốt'],
        suggestions: ['Viết blog hoặc chia sẻ nội dung sáng tạo', 'Tham gia CLB diễn thuyết', 'Luyện kỹ năng ngoại ngữ']
    },
    D: {
        name: 'Nhóm D: Nghệ Thuật - Thiết Kế',
        traits: ['creative', 'reflective', 'introverted'],
        description: 'Bạn có tư duy sáng tạo, nhạy cảm nghệ thuật. Phù hợp với các ngành thiết kế, nghệ thuật, kiến trúc.',
        careers: ['Thiết Kế Đồ Họa', 'Kiến Trúc', 'Mỹ Thuật', 'Thiết Kế Thời Trang', 'Nhiếp Ảnh - Quay Phim'],
        universities: ['ĐH Kiến Trúc Hà Nội', 'ĐH Mỹ Thuật Công Nghiệp', 'ĐH Sân Khấu - Điện Ảnh'],
        color: '#f97316',
        icon: 'fa-palette',
        strengths: ['Tư duy sáng tạo độc đáo', 'Cảm nhận thẩm mỹ tinh tế', 'Khả năng biểu đạt nghệ thuật'],
        suggestions: ['Thực hành vẽ và thiết kế hàng ngày', 'Học các công cụ Adobe (Photoshop, Illustrator)', 'Tham gia triển lãm nghệ thuật']
    },
    E: {
        name: 'Nhóm E: Giáo Dục - Tâm Lý',
        traits: ['collaborative', 'communicative', 'reflective'],
        description: 'Bạn có khả năng thấu hiểu con người, hỗ trợ và giáo dục. Phù hợp với các ngành giáo dục, tâm lý, xã hội học.',
        careers: ['Sư Phạm', 'Tâm Lý Học', 'Công Tác Xã Hội', 'Giáo Dục Mầm Non', 'Tư Vấn Học Đường'],
        universities: ['ĐH Sư Phạm Hà Nội', 'ĐH Sư Phạm TP.HCM', 'ĐH Giáo Dục - ĐHQGHN'],
        color: '#a855f7',
        icon: 'fa-user-graduate',
        strengths: ['Thấu hiểu và đồng cảm cao', 'Kỹ năng hỗ trợ và giảng dạy', 'Kiên nhẫn và tận tâm'],
        suggestions: ['Tham gia hoạt động tình nguyện', 'Học về tâm lý học và kỹ năng mềm', 'Thử làm gia sư hoặc dạy kèm']
    },
    F: {
        name: 'Nhóm F: Nông - Lâm - Ngư - Y',
        traits: ['practical', 'analytical', 'reflective'],
        description: 'Bạn quan tâm đến môi trường, sức khỏe và thực hành. Phù hợp với các ngành y dược, nông nghiệp, môi trường.',
        careers: ['Y Khoa', 'Dược Học', 'Nông Nghiệp', 'Môi Trường', 'Thú Y'],
        universities: ['ĐH Y Hà Nội', 'ĐH Y Dược TP.HCM', 'ĐH Nông Lâm TP.HCM'],
        color: '#ef4444',
        icon: 'fa-seedling',
        strengths: ['Quan tâm đến sức khỏe cộng đồng', 'Tư duy khoa học thực nghiệm', 'Tinh thần trách nhiệm cao'],
        suggestions: ['Tham gia các hoạt động bảo vệ môi trường', 'Tìm hiểu về y học và khoa học sức khỏe', 'Tham quan các cơ sở y tế, nông trại']
    }
};

// Calculate Group Scores
function calculateGroupScores() {
    const groupScores = {};

    Object.keys(careerGroups).forEach(groupKey => {
        const group = careerGroups[groupKey];
        let score = 0;

        group.traits.forEach(trait => {
            score += scores[trait] || 0;
        });

        groupScores[groupKey] = score;
    });

    return groupScores;
}

// Restart Quiz (clears survey and starts over)
function restartQuiz() {
    // Clear localStorage
    localStorage.removeItem('surveyResults');
    
    // Reset survey state
    currentQuestionIndex = 0;
    currentSectionIndex = 0;
    answers = {};
    scores = {
        analytical: 0,
        collaborative: 0,
        creative: 0,
        practical: 0,
        communicative: 0,
        leadership: 0,
        technical: 0,
        reflective: 0,
        introverted: 0
    };
    
    // Reload page to restart
    window.location.href = 'survey.html';
}

// Initialize on page load
window.addEventListener('DOMContentLoaded', initQuiz);