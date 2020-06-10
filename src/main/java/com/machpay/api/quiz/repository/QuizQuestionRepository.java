package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizQuestion;
import com.machpay.api.entity.QuizSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    QuizQuestion findFirstBySeasonOrderByCreatedAtDesc(QuizSeason season);

    Optional<QuizQuestion> findById(UUID id);

    boolean existsBySeason(QuizSeason season);

    List<QuizQuestion> findAllBySeasonOrderByCreatedAtDesc(QuizSeason quizSeason);
}
