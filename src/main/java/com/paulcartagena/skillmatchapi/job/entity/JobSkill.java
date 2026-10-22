package com.paulcartagena.skillmatchapi.job.entity;

import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "job_skill_requirements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "skill_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // false = desired ("nice to have") skill
    @Column(nullable = false)
    private boolean required;
}
