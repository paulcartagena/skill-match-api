package com.paulcartagena.skillmatchapi.skill.repository;

import com.paulcartagena.skillmatchapi.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    List<Skill> findByNameContainingIgnoreCase(String name);
}
