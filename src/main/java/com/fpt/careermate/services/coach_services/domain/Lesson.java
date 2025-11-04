package com.fpt.careermate.services.coach_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String lessonOverview;

    @Column(columnDefinition = "TEXT")
    String coreContent;

    @Column(columnDefinition = "TEXT")
    String exercise;

    @Column(columnDefinition = "TEXT")
    String conclusion;

    boolean marked;

    @Column(nullable = false)
    int position;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    Module module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Question> questions = new ArrayList<>();
}
