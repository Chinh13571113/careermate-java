package com.fpt.careermate.repository;

import com.fpt.careermate.domain.JdSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface JdSkillRepo extends JpaRepository<JdSkill, Integer> {
    Optional<JdSkill> findSkillByName(String name);
}
