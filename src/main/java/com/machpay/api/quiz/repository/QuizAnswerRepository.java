package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizAnswer;
import com.machpay.api.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findAllByQuestion(QuizQuestion quizQuestion);
}
