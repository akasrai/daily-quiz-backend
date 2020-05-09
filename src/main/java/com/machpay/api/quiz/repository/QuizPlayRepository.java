package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizPlayRepository extends JpaRepository<QuizPlay, Long> {
    Optional<QuizPlay> findByUser(User user);

    Optional<QuizPlay> findByUserAndSeason(User user, QuizSeason season);

    List<QuizPlay> findAllByOrderByPointDescTimeTakenAsc();
}
