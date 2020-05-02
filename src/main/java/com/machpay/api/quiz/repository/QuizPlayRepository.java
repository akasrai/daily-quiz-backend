package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizPlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizPlayRepository extends JpaRepository<QuizPlay, Long> {
}
