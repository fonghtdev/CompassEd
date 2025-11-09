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
    document.getElementById('totalQuestions').textContent = getTotalQuestions();
    loadQuestion();
}

// Load Current Question
function loadQuestion() {
    const section = quizData[currentSectionIndex];
    const question = section.questions[currentQuestionIndex];

    // Update section badge
    const badge = document.getElementById('sectionBadge');
    badge.className = `section-badge section-${section.section}`;
    badge.textContent = section.sectionTitle;

    // Update question text
    document.getElementById('questionText').textContent = question.question;

    // Update question number
    const globalQuestionNumber = quizData.slice(0, currentSectionIndex)
        .reduce((sum, s) => sum + s.questions.length, 0) + currentQuestionIndex + 1;
    document.getElementById('questionNumber').textContent = globalQuestionNumber;

    // Update progress bar
    const totalQuestions = getTotalQuestions();
    const progressPercent = (globalQuestionNumber / totalQuestions) * 100;
    document.getElementById('progressBar').style.width = progressPercent + '%';

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
        const label = document.createElement('label');
        label.className = 'option-label';

        const input = document.createElement('input');
        input.type = 'radio';
        input.name = 'answer';
        input.value = option.value;

        const text = document.createElement('span');
        text.className = 'option-text';
        text.textContent = option.text;

        label.appendChild(input);
        label.appendChild(text);

        // Check if this option was previously selected
        const answerKey = `${currentSectionIndex}-${currentQuestionIndex}`;
        if (answers[answerKey] && answers[answerKey] === option.value) {
            input.checked = true;
            label.classList.add('selected');
        }

        // Add event listener for selection
        input.addEventListener('change', function() {
            document.querySelectorAll('.option-label').forEach(l => l.classList.remove('selected'));
            label.classList.add('selected');
            answers[answerKey] = option.value;
        });

        container.appendChild(label);
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
    prevBtn.disabled = globalQuestionNumber === 1;

    // Next button and submit button
    if (globalQuestionNumber === totalQuestions) {
        nextBtn.classList.add('d-none');
        submitBtn.classList.remove('d-none');
    } else {
        nextBtn.classList.remove('d-none');
        submitBtn.classList.add('d-none');
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

    // Show results
    showResults();
}

// Show Results
function showResults() {
    document.getElementById('questionContainer').classList.add('d-none');
    document.getElementById('resultsSection').classList.remove('d-none');

    // Hide navigation buttons
    document.querySelector('.button-group').classList.add('d-none');

    const resultsContent = document.getElementById('resultsContent');
    resultsContent.innerHTML = '';

    // Create category results
    const categories = [
        { label: 'Tư Duy Phân Tích', key: 'analytical', careers: 'Lập Trình, Khoa Học, Tài Chính' },
        { label: 'Hợp Tác & Giao Tiếp', key: 'collaborative', careers: 'Quản Lý, Nhân Sự, Giáo Dục' },
        { label: 'Sáng Tạo', key: 'creative', careers: 'Thiết Kế, Nghệ Thuật, Quảng Cáo' },
        { label: 'Thực Hành', key: 'practical', careers: 'Kỹ Thuật, Xây Dựng, Bán Lẻ' },
        { label: 'Truyền Thông', key: 'communicative', careers: 'Nhà Báo, Marketing, PR' },
        { label: 'Lãnh Đạo', key: 'leadership', careers: 'Lãnh Đạo, Khởi Nghiệp, Quản Lý' },
        { label: 'Công Nghệ', key: 'technical', careers: 'IT, Web Dev, Data Science' }
    ];

    const total = Object.values(scores).reduce((a, b) => a + b, 0);

    categories.forEach(category => {
        const score = scores[category.key];
        const percentage = Math.round((score / total) * 100);

        const item = document.createElement('div');
        item.className = 'result-item';
        item.innerHTML = `
            <div class="result-label">${category.label}</div>
            <div class="result-value">${category.careers}
                <span class="result-percentage">${percentage}%</span>
            </div>
        `;
        resultsContent.appendChild(item);
    });

    // Add message for AI analysis
    const aiMessage = document.createElement('div');
    aiMessage.style.marginTop = '20px';
    aiMessage.style.padding = '16px';
    aiMessage.style.backgroundColor = 'rgba(99, 102, 241, 0.1)';
    aiMessage.style.borderRadius = '12px';
    aiMessage.style.borderLeft = '4px solid #6366f1';
    // aiMessage.innerHTML = `
    //     <p style="margin: 0; color: #4f46e5; font-size: 14px; font-weight: 600;">
    //         💡 Gợi Ý: Bạn có thể sử dụng kết quả này để nhập vào AI để nhận được phân tích chi tiết về ngành phù hợp
    //     </p>
    // `;
    resultsContent.appendChild(aiMessage);

    // Add result data for copying
    const resultData = JSON.stringify(scores);
    const dataItem = document.createElement('div');
    dataItem.style.marginTop = '16px';
    dataItem.style.padding = '12px';
    dataItem.style.backgroundColor = '#f8fafc';
    dataItem.style.borderRadius = '8px';
    dataItem.style.fontSize = '12px';
    dataItem.style.color = '#64748b';
    dataItem.style.fontFamily = 'monospace';
    dataItem.style.wordBreak = 'break-all';
    dataItem.innerHTML = `<strong>Dữ liệu phân tích:</strong><br/>${resultData}`;
    resultsContent.appendChild(dataItem);
}

// Initialize on page load
window.addEventListener('DOMContentLoaded', initQuiz);