package com.fpt.careermate.repository;

import com.fpt.careermate.domain.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidDateTokenRepo extends JpaRepository<InvalidToken, String> {
}
