package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
}
