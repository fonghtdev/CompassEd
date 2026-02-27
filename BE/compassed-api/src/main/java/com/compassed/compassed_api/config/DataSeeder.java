package com.compassed.compassed_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SubjectRepository subjectRepository;
    private final RoadmapRepository roadmapRepository;

    public DataSeeder(SubjectRepository subjectRepository, RoadmapRepository roadmapRepository) {
        this.subjectRepository = subjectRepository;
        this.roadmapRepository = roadmapRepository;
    }

    @Override
    public void run(String... args) {
        seedSubjectAndRoadmaps("MATH", "Toán");
        seedSubjectAndRoadmaps("LITERATURE", "Ngữ văn");
        seedSubjectAndRoadmaps("ENGLISH", "Tiếng Anh");
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
                "Củng cố kiến thức nền tảng");

        createRoadmapIfMissing(subject, Level.L2,
                name + " - Level 2",
                "Nâng cao kỹ năng làm bài");

        createRoadmapIfMissing(subject, Level.L3,
                name + " - Level 3",
                "Luyện đề & tối ưu điểm số");
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

}
