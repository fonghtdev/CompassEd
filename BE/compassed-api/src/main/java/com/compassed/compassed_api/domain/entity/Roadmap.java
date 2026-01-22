package com.compassed.compassed_api.domain.entity;

import com.compassed.compassed_api.domain.enums.Level;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "roadmaps",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subject_id", "level"})
    }
)
@Getter
@Setter
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Roadmap thuộc môn nào
    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // Level của roadmap (L1 / L2 / L3)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    // Tên roadmap (VD: Toán L1 – Nền tảng)
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    // Thứ tự hiển thị
    private Integer displayOrder;
}
