// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
    initHeroSlider();
    initFeatures();
    initCourses();
    initStats();
    initEvents();
    initTeam();
    initBlog();
    initProducts();
    initTestimonials();
    initPartners();
    initFlickrFeed();
});

// Hero Slider
function initHeroSlider() {
    const container = document.getElementById('heroSlides');

    data.slides.forEach((slide, index) => {
        const isActive = index === 0 ? 'active' : '';
        const slideHTML = `
            <div class="carousel-item ${isActive}" style="background-image: url('${slide.image}')">
                <div class="carousel-caption">
                    <h1 class="display-3 fw-bold">${slide.title}</h1>
                    <p class="lead">${slide.description1}</p>
                    <p class="lead">${slide.description2}</p>
                    <div class="btn-group-hero">
                        <button class="btn btn-dark btn-lg">READ MORE</button>
                        <button class="btn btn-danger btn-lg">GET STARTED NOW</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += slideHTML;
    });
}

// Features
function initFeatures() {
    const container = document.getElementById('featuresContainer');

    data.features.forEach(feature => {
        const featureHTML = `
            <div class="col-lg-3 col-md-6">
                <div class="feature-card">
                    <div class="icon-circle">
                        <i class="fas ${feature.icon}"></i>
                    </div>
                    <h5 class="fw-bold mb-3">${feature.title}</h5>
                    <p class="text-light mb-0">${feature.description}</p>
                </div>
            </div>
        `;
        container.innerHTML += featureHTML;
    });
}

// Courses
function initCourses() {
    const container = document.getElementById('coursesSlides');

    // Group courses into slides of 3
    for (let i = 0; i < data.courses.length; i += 3) {
        const isActive = i === 0 ? 'active' : '';
        const coursesGroup = data.courses.slice(i, i + 3);

        let slideHTML = `<div class="carousel-item ${isActive}"><div class="row g-4">`;

        coursesGroup.forEach(course => {
            const stars = generateStars(course.rating);
            slideHTML += `
                <div class="col-lg-4">
                    <div class="course-card">
                        <div class="card-img-wrapper">
                            <img src="${course.image}" alt="${course.title}">
                            <div class="price-badge">$${course.price}</div>
                            <div class="category-badge">${course.category}</div>
                        </div>
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-3">${course.title}</h5>
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <div class="stars">${stars}</div>
                                <small class="text-muted">${course.reviews} Reviews</small>
                            </div>
                            <p class="text-muted mb-3">${course.description}</p>
                            <div class="course-meta">
                                <div>
                                    <small>Course Time</small>
                                    <div class="fw-semibold">${course.courseTime}</div>
                                </div>
                                <div>
                                    <small>Students</small>
                                    <div class="fw-semibold">${course.students}</div>
                                </div>
                                <div>
                                    <small>Duration</small>
                                    <div class="fw-semibold">${course.duration}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        });

        slideHTML += `</div></div>`;
        container.innerHTML += slideHTML;
    }
}

// Stats with animation
function initStats() {
    const container = document.getElementById('statsContainer');

    data.stats.forEach(stat => {
        const statHTML = `
            <div class="col-lg-3 col-md-6">
                <div class="stat-item">
                    <div class="stat-number" data-target="${stat.number}">0</div>
                    <h5 class="fw-semibold">${stat.label}</h5>
                </div>
            </div>
        `;
        container.innerHTML += statHTML;
    });

    // Animate numbers
    animateStats();
}

function animateStats() {
    const statNumbers = document.querySelectorAll('.stat-number');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = parseInt(entry.target.getAttribute('data-target'));
                animateNumber(entry.target, target);
                observer.unobserve(entry.target);
            }
        });
    });

    statNumbers.forEach(stat => observer.observe(stat));
}

function animateNumber(element, target) {
    let current = 0;
    const increment = target / 60;
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            element.textContent = target;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 30);
}

// Events
function initEvents() {
    const container = document.getElementById('eventsContainer');

    data.events.forEach(event => {
        const eventHTML = `
            <div class="col-lg-4 col-md-6">
                <div class="event-card">
                    <div class="card-img-wrapper">
                        <img src="${event.image}" alt="${event.title}">
                        <div class="date-badge">
                            <i class="fas fa-calendar-alt me-1"></i> ${event.date}
                        </div>
                    </div>
                    <div class="card-body p-4">
                        <h5 class="fw-bold mb-4">${event.title}</h5>
                        <div class="event-meta mb-3">
                            <div class="mb-2">
                                <i class="fas fa-clock"></i>
                                <span class="text-muted">${event.time}</span>
                            </div>
                            <div>
                                <i class="fas fa-map-marker-alt"></i>
                                <span class="text-muted">${event.venue}</span>
                            </div>
                        </div>
                        <button class="btn btn-danger w-100">Join Event</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += eventHTML;
    });
}

// Team
function initTeam() {
    const container = document.getElementById('teamContainer');

    data.team.forEach(member => {
        const teamHTML = `
            <div class="col-lg-4 col-md-6">
                <div class="team-card">
                    <div class="card-img-wrapper">
                        <img src="${member.image}" alt="${member.name}">
                        <div class="overlay">
                            <div class="overlay-content">
                                <h4 class="fw-bold mb-2">${member.name}</h4>
                                <p class="text-danger fw-semibold mb-3">${member.position}</p>
                                <p class="small mb-3">${member.description}</p>
                                <div class="social-links">
                                    <a href="#"><i class="fab fa-twitter"></i></a>
                                    <a href="#"><i class="fab fa-facebook"></i></a>
                                    <a href="#"><i class="fab fa-linkedin"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-4 text-center">
                        <h5 class="fw-bold mb-2">${member.name}</h5>
                        <p class="text-danger fw-semibold mb-0">${member.position}</p>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += teamHTML;
    });
}

// Blog
function initBlog() {
    const container = document.getElementById('blogContainer');

    data.blogs.forEach(blog => {
        const blogHTML = `
            <div class="col-lg-4 col-md-6">
                <div class="blog-card">
                    <div class="card-img-wrapper">
                        <img src="${blog.image}" alt="${blog.title}">
                    </div>
                    <div class="card-body p-4">
                        <small class="text-muted">${blog.date}</small>
                        <h5 class="fw-bold my-3">${blog.title}</h5>
                        <p class="text-muted text-truncate-3">${blog.excerpt}</p>
                        <a href="#" class="read-more">Read More →</a>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += blogHTML;
    });
}

// Products
function initProducts() {
    const container = document.getElementById('productsContainer');

    data.products.forEach(product => {
        const productHTML = `
            <div class="col-lg-3 col-md-6">
                <div class="product-card">
                    <div class="card-img-wrapper">
                        <img src="${product.image}" alt="${product.title}">
                        <div class="overlay">
                            <button class="btn btn-danger">Quick View</button>
                        </div>
                    </div>
                    <div class="card-body p-4">
                        <h6 class="fw-bold mb-3">${product.title}</h6>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="text-danger fw-bold fs-5">From: $${product.price}.00</span>
                        </div>
                        <button class="btn btn-dark w-100">
                            <i class="fas fa-shopping-cart me-2"></i>Add To Cart
                        </button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += productHTML;
    });
}

// Testimonials
function initTestimonials() {
    const container = document.getElementById('testimonialsContainer');

    data.testimonials.forEach(testimonial => {
        const stars = generateStars(5);
        const testimonialHTML = `
            <div class="col-lg-6">
                <div class="testimonial-card">
                    <div class="d-flex align-items-center gap-4 mb-4">
                        <img src="${testimonial.image}" alt="${testimonial.name}">
                        <div>
                            <h5 class="fw-bold mb-2">${testimonial.name}</h5>
                            <div class="stars">${stars}</div>
                        </div>
                    </div>
                    <div class="position-relative">
                        <div class="quote-icon position-absolute" style="top: -10px; left: -10px;">
                            <i class="fas fa-quote-left"></i>
                        </div>
                        <p class="text-muted fst-italic ps-4">${testimonial.text}</p>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += testimonialHTML;
    });
}

// Partners
function initPartners() {
    const container = document.getElementById('partnersContainer');

    data.partners.forEach(partner => {
        const partnerHTML = `
            <div class="col-lg-2 col-md-4 col-6">
                <div class="partner-card">
                    <img src="${partner.image}" alt="Partner">
                </div>
            </div>
        `;
        container.innerHTML += partnerHTML;
    });
}

// Flickr Feed
function initFlickrFeed() {
    const container = document.getElementById('flickrFeed');

    data.flickrImages.forEach(image => {
        const flickrHTML = `
            <div class="col-4">
                <img src="${image}" alt="Flickr" class="flickr-img">
            </div>
        `;
        container.innerHTML += flickrHTML;
    });
}

// Helper Functions
function generateStars(rating) {
    let starsHTML = '';
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
        starsHTML += '<i class="fas fa-star"></i>';
    }

    if (hasHalfStar) {
        starsHTML += '<i class="fas fa-star-half-alt"></i>';
    }

    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '<i class="far fa-star"></i>';
    }

    return starsHTML;
}

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// AI EDUADVISOR MODULE
document.getElementById('aiSearch').addEventListener('click', async () => {
    const keyword = document.getElementById('careerInput').value.trim();
    if (!keyword) return alert('Vui lòng nhập ngành nghề!');

    const res = await fetch(`http://127.0.0.1:5000/api/universities?keyword=${encodeURIComponent(keyword)}`);
    const data = await res.json();
    const container = document.getElementById('universityList');
    container.innerHTML = '';

    data.forEach((school, index) => {
        const mapId = `map_${index}`;
        const reviewsHtml = school.reviews.map(r => `
      <div class="border rounded p-2 mb-2 bg-light">
        <strong>${r.author}</strong>: ${r.text}
        <div class="rating">⭐ ${r.rating}</div>
      </div>`).join('');

        const score2023 = school.admission_scores?.["2023"] || {};
        const score2024 = school.admission_scores?.["2024"] || {};
        const scoreHtml = Object.keys(score2024).map(major => `
      <li>${major}: ${score2023[major] || '-'} → ${score2024[major]}</li>`).join('');

        container.insertAdjacentHTML('beforeend', `
      <div class="col-md-4">
        <div class="card p-3 shadow-sm">
          <h5 class="fw-bold">${school.school}</h5>
          <div>⭐ ${school.rating}</div>
          <ul>${scoreHtml}</ul>
          <div><strong>Đánh giá từ Google Maps:</strong>${reviewsHtml}</div>
          <div id="${mapId}" style="height:200px;border-radius:10px;margin-top:10px;"></div>
        </div>
      </div>`);

        // Hiển thị bản đồ
        setTimeout(() => {
            if (school.location) {
                const map = new google.maps.Map(document.getElementById(mapId), {
                    center: school.location,
                    zoom: 15
                });
                new google.maps.Marker({
                    position: school.location,
                    map,
                    title: school.school
                });
            }
        }, 300);
    });
});

// // TOP UNIVERSITIES
// document.addEventListener('DOMContentLoaded', () => {
//     const container = document.getElementById('universityList');
//     if (!container || !data.universities) return;

//     container.innerHTML = '';

//     data.universities.forEach((school, index) => {
//         const reviewsHtml = school.reviews.map(r => `
//             <div class="border rounded p-2 mb-2 bg-light">
//                 <strong>${r.author}</strong>: ${r.text}
//                 <div class="rating text-warning">⭐ ${r.rating}</div>
//             </div>
//         `).join('');

//         const score2023 = school.admission_scores?.["2023"] || {};
//         const score2024 = school.admission_scores?.["2024"] || {};
//         const scoreHtml = Object.keys(score2024).map(major => `
//             <li>${major}: ${score2023[major] || '-'} → ${score2024[major]}</li>
//         `).join('');

//         const mapId = `map_${index}`;

//         container.insertAdjacentHTML('beforeend', `
//             <div class="col-lg-3 col-md-4 col-sm-6">
//                 <div class="card shadow-sm p-3 h-100">
//                     <h5 class="fw-bold mb-2">${school.school}</h5>
//                     <div class="rating text-warning mb-2">⭐ ${school.rating}</div>
//                     <ul class="small text-muted mb-3">${scoreHtml}</ul>
//                     <div><strong>Đánh giá từ sinh viên:</strong>${reviewsHtml}</div>
//                     <div id="${mapId}" style="height:200px;border-radius:10px;margin-top:10px;"></div>
//                 </div>
//             </div>
//         `);

//         // Hiển thị bản đồ Google Maps
//         setTimeout(() => {
//             if (school.location && school.location.lat && school.location.lng) {
//                 const map = new google.maps.Map(document.getElementById(mapId), {
//                     center: { lat: school.location.lat, lng: school.location.lng },
//                     zoom: 14
//                 });
//                 new google.maps.Marker({
//                     position: { lat: school.location.lat, lng: school.location.lng },
//                     map: map,
//                     title: school.school
//                 });
//             }
//         }, 300);
//     });
// });


// LOAD TOP UNIVERSITIES
function initUniversities() {
    const container = document.getElementById('universitiesSlides');
    if (!container) return;

    const allSchools = data.universities;
    const chunkSize = 3;

    for (let i = 0; i < allSchools.length; i += chunkSize) {
        const slideSchools = allSchools.slice(i, i + chunkSize);

        const isActive = (i === 0) ? 'active' : '';
        let cardsHtml = ''; 
        let mapInfos = []; 

        slideSchools.forEach((school, indexInSlide) => {
            const globalIndex = i + indexInSlide; 

            const reviews = school.reviews || [];
            const reviewsHtml = reviews.length > 0
                ? reviews.map(r => `<div><small>${r.author}: ${r.text} (⭐ ${r.rating})</small></div>`).join('')
                : '<div><small>Chưa có đánh giá.</small></div>';

            const score2023 = school.admission_scores?.["2023"] || {};
            const score2024 = school.admission_scores?.["2024"] || {};
            const majors = Array.from(new Set([...Object.keys(score2023), ...Object.keys(score2024)]));

            const scoreHtml = majors.length > 0
                ? majors.map(major => `
                    <li>${major}: ${score2023[major] || 'N/A'} → ${score2024[major] || 'N/A'}</li>`).join('')
                : '<li>Chưa cập nhật điểm chuẩn.</li>';

            const mapId = `slider_map_${globalIndex}`;

            mapInfos.push({
                mapId: mapId,
                location: school.location
            });

            cardsHtml += `
                <div class="col-md-6 col-lg-4"> <div class="card shadow-sm p-3 h-100">
                        <h5 class="fw-bold mb-2">${school.school}</h5>
                        <div class="rating text-warning mb-2">⭐ ${school.rating}</div>
                        
                        <strong class="small">Điểm chuẩn (2023 → 2024):</strong>
                        <ul class="small text-muted mb-2" style="padding-left: 1.2rem; max-height: 80px; overflow-y: auto;">
                            ${scoreHtml}
                        </ul>
                        
                        <strong class="small">Đánh giá:</strong>
                        <div class="small text-muted mb-3" style="max-height: 70px; overflow-y: auto;">
                            ${reviewsHtml}
                        </div>
                        
                        <div id="${mapId}" style="height:150px; border-radius:10px; background: #f0f0f0;">
                            </div>
                    </div>
                </div>
            `;
        });

        const slideHTML = `
            <div class="carousel-item ${isActive}">
                <div class="row g-4 justify-content-center"> ${cardsHtml}
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', slideHTML);

        // API gg map
        setTimeout(() => {
            mapInfos.forEach(info => {
                try {
                    if (info.location && info.location.lat && info.location.lng) {
                        const mapEl = document.getElementById(info.mapId);
                        if (!mapEl) return;

                        const map = new google.maps.Map(mapEl, {
                            center: { lat: info.location.lat, lng: info.location.lng },
                            zoom: 15,
                            disableDefaultUI: true
                        });
                        new google.maps.Marker({
                            position: { lat: info.location.lat, lng: info.location.lng },
                            map: map,
                        });
                    } else {
                        const mapEl = document.getElementById(info.mapId);
                        if (mapEl) mapEl.innerHTML = '<div class="text-center small text-muted p-3" style="line-height: 130px;">Không có dữ liệu bản đồ.</div>';
                    }
                } catch (e) {
                    console.error("Lỗi tải Google Map cho slider: ", e);
                    const mapEl = document.getElementById(info.mapId);
                    if (mapEl) {
                        mapEl.innerHTML = '<div class="text-center small text-danger p-3" style="line-height: 130px;">Lỗi tải bản đồ.</div>';
                    }
                }
            });
        }, 500); 

    } 
}

document.addEventListener('DOMContentLoaded', initUniversities);



// ====================== AI EDUADVISOR MODULE ======================
// document.getElementById('aiSearch').addEventListener('click', async () => {
//     const keyword = document.getElementById('careerInput').value.trim();
//     if (!keyword) return alert('Vui lòng nhập ngành nghề!');

//     const res = await fetch(`http://127.0.0.1:5000/api/universities?keyword=${encodeURIComponent(keyword)}`);
//     const data = await res.json();
//     const container = document.getElementById('universityList');
//     container.innerHTML = '';

//     data.forEach((school, index) => {
//         const mapId = `map_${index}`;
//         const reviewsHtml = school.reviews.map(r => `
//       <div class="border rounded p-2 mb-2 bg-light">
//         <strong>${r.author}</strong>: ${r.text}
//         <div class="rating">⭐ ${r.rating}</div>
//       </div>`).join('');

//         const score2023 = school.admission_scores?.["2023"] || {};
//         const score2024 = school.admission_scores?.["2024"] || {};
//         const scoreHtml = Object.keys(score2024).map(major => `
//       <li>${major}: ${score2023[major] || '-'} → ${score2024[major]}</li>`).join('');

//         container.insertAdjacentHTML('beforeend', `
//       <div class="col-md-4">
//         <div class="card p-3 shadow-sm">
//           <h5 class="fw-bold">${school.school}</h5>
//           <div>⭐ ${school.rating}</div>
//           <ul>${scoreHtml}</ul>
//           <div><strong>Đánh giá từ Google Maps:</strong>${reviewsHtml}</div>
//           <div id="${mapId}" style="height:200px;border-radius:10px;margin-top:10px;"></div>
//         </div>
//       </div>`);

//         // Hiển thị bản đồ
//         setTimeout(() => {
//             if (school.location) {
//                 const map = new google.maps.Map(document.getElementById(mapId), {
//                     center: school.location,
//                     zoom: 15
//                 });
//                 new google.maps.Marker({
//                     position: school.location,
//                     map,
//                     title: school.school
//                 });
//             }
//         }, 300);
//     });
// });

// =================== LOAD TOP UNIVERSITIES ===================
// document.addEventListener('DOMContentLoaded', async () => {
//     const res = await fetch('http://127.0.0.1:5000/api/universities');
//     const data = await res.json();
//     const container = document.getElementById('universityList');
//     if (!container) return;

//     container.innerHTML = '';

//     data.forEach((school, index) => {
//         const reviewsHtml = school.reviews.map(r => `
//       <div class="border rounded p-2 mb-2 bg-light">
//         <strong>${r.author}</strong>: ${r.text}
//         <div class="rating text-warning">⭐ ${r.rating}</div>
//       </div>`).join('');

//         const score2023 = school.admission_scores?.["2023"] || {};
//         const score2024 = school.admission_scores?.["2024"] || {};
//         const scoreHtml = Object.keys(score2024).map(major => `
//       <li>${major}: ${score2023[major] || '-'} → ${score2024[major]}</li>`).join('');

//         const mapId = `map_${index}`;

//         container.insertAdjacentHTML('beforeend', `
//       <div class="col-md-6 col-lg-4">
//         <div class="card shadow-sm p-3 h-100">
//           <h5 class="fw-bold mb-2">${school.school}</h5>
//           <div class="rating text-warning mb-2">⭐ ${school.rating}</div>
//           <ul class="small text-muted mb-3">${scoreHtml}</ul>
//           <div><strong>Đánh giá từ Google Maps:</strong>${reviewsHtml}</div>
//           <div id="${mapId}" style="height:200px;border-radius:10px;margin-top:10px;"></div>
//         </div>
//       </div>`);

//         // Hiển thị bản đồ
//         setTimeout(() => {
//             if (school.location && school.location.lat && school.location.lng) {
//                 const map = new google.maps.Map(document.getElementById(mapId), {
//                     center: { lat: school.location.lat, lng: school.location.lng },
//                     zoom: 15
//                 });
//                 new google.maps.Marker({
//                     position: { lat: school.location.lat, lng: school.location.lng },
//                     map: map,
//                     title: school.school
//                 });
//             }
//         }, 300);
//     });
// });
