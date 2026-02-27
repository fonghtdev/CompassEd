package com.compassed.compassed_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.RoleAccessService;

@Component
@Profile("mysql")
public class DataSeeder implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final RoleAccessService roleAccessService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.seed.admin-email:}")
    private String adminEmail;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    public DataSeeder(
            SubjectRepository subjectRepository,
            RoadmapRepository roadmapRepository,
            UserRepository userRepository,
            RoleAccessService roleAccessService) {
        this.subjectRepository = subjectRepository;
        this.roadmapRepository = roadmapRepository;
        this.userRepository = userRepository;
        this.roleAccessService = roleAccessService;
    }

    @Override
    public void run(String... args) {
        seedSubjectAndRoadmaps("MATH", "Toan");
        seedSubjectAndRoadmaps("LITERATURE", "Ngu van");
        seedSubjectAndRoadmaps("ENGLISH", "Tieng Anh");
        seedDefaultAdmin();
    }

    private void seedSubjectAndRoadmaps(String code, String name) {
        Subject subject = subjectRepository.findByCode(code).orElseGet(() -> {
            Subject s = new Subject();
            s.setCode(code);
            s.setName(name);
            return subjectRepository.save(s);
        });

        createRoadmapIfMissing(subject, Level.L1,
                name + " - Level 1",
                "Cung co kien thuc nen tang");

        createRoadmapIfMissing(subject, Level.L2,
                name + " - Level 2",
                "Nang cao ky nang lam bai");

        createRoadmapIfMissing(subject, Level.L3,
                name + " - Level 3",
                "Luyen de va toi uu diem so");
    }

    private void createRoadmapIfMissing(
            Subject subject,
            Level level,
            String title,
            String desc) {
        roadmapRepository.findBySubject_IdAndLevel(subject.getId(), level)
                .orElseGet(() -> {
                    Roadmap r = new Roadmap();
                    r.setSubject(subject);
                    r.setLevel(level);
                    r.setTitle(title);
                    r.setDescription(desc);
                    return roadmapRepository.save(r);
                });
    }

    private void seedDefaultAdmin() {
        String email = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        String password = adminPassword == null ? "" : adminPassword.trim();
        if (email.isBlank() || password.length() < 6) {
            return;
        }

        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            roleAccessService.assignRole(existing, UserRole.ADMIN);
        }, () -> {
            User admin = new User();
            admin.setEmail(email);
            admin.setFullName("System Admin");
            admin.setPasswordHash(passwordEncoder.encode(password));
            User saved = userRepository.save(admin);
            roleAccessService.assignRole(saved, UserRole.ADMIN);
        });
    }
}
