package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizResult;
import com.machpay.api.entity.QuizSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findAllBySeason(QuizSeason quizSeason);
}
