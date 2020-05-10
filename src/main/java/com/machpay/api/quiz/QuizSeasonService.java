package com.machpay.api.quiz;

import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.quiz.dto.QuizSeasonRequest;
import com.machpay.api.quiz.repository.QuizSeasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizSeasonService {

    @Autowired
    private QuizPlayService quizPlayService;

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private QuizSeasonService quizSeasonService;

    @Autowired
    private QuizSeasonRepository quizSeasonRepository;

    public boolean isSeasonActive(QuizSeason currentSeason) {
        return quizSeasonRepository.existsByTitleAndActiveTrue(currentSeason.getTitle());
    }

    @Transactional
    public void create(QuizSeasonRequest quizSeasonRequest) {
        QuizSeason quizSeason = new QuizSeason();
        quizSeason.setTitle(quizSeasonRequest.getSeason());
        quizSeason.setActive(true);

        quizSeasonRepository.save(quizSeason);
    }

    public QuizSeason getActiveSeason() {
        return quizSeasonRepository.findFirstByActiveTrue().orElseThrow(() -> new ResourceNotFoundException("Quiz " +
                "Season", "active", true));
    }

    @Transactional
    public void hostNewSeason(QuizSeasonRequest quizSeasonRequest) {
        declareCurrentSeasonWinner();
        endCurrentSeason();
        create(quizSeasonRequest);
    }

    @Transactional
    public void declareCurrentSeasonWinner() {
        int position = 0;
        QuizSeason currentSeason = quizSeasonService.getActiveSeason();
        List<QuizPlay> quizPlays = quizPlayService.getTop3QuizPlay(currentSeason);

        for (QuizPlay quizPlay : quizPlays) {
            position++;
            quizResultService.createWinner(quizPlay, position);
        }
    }

    @Transactional
    public void endCurrentSeason() {
        QuizSeason quizSeason = getActiveSeason();
        quizSeason.setActive(false);

        quizSeasonRepository.save(quizSeason);
    }
}
