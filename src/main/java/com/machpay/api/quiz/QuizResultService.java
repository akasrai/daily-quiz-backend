package com.machpay.api.quiz;

import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizResult;
import com.machpay.api.quiz.repository.QuizResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizResultService {
    @Autowired
    private QuizResultRepository quizResultRepository;

    @Transactional
    public void createWinner(QuizPlay quizPlay, int position) {
        QuizResult quizResult = new QuizResult();
        quizResult.setWinner(quizPlay.getUser());
        quizResult.setSeason(quizPlay.getSeason());
        quizResult.setPosition(Long.valueOf(position));

        quizResultRepository.save(quizResult);
    }
}
