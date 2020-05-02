package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    QuizQuestion findFirstByOrderByCreatedAtDesc();
}
