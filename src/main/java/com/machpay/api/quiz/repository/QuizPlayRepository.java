package com.machpay.api.quiz.repository;

import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizPlayRepository extends JpaRepository<QuizPlay, Long> {
    Optional<QuizPlay> findByUser(User user);

    Optional<QuizPlay> findByUserAndSeason(User user, QuizSeason season);

    List<QuizPlay> findAllByOrderByPointDescTimeTakenAsc();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE QuizPlay q SET q.locked=false WHERE q.season=:quizSeason")
    void unLockByQuizSeason(@Param("quizSeason") QuizSeason quizSeason);
}
