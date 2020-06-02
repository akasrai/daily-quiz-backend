package com.machpay.api.quiz;

import com.machpay.api.common.exception.BadRequestException;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizResult;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.quiz.dto.QuizPlayResponse;
import com.machpay.api.quiz.dto.QuizSeasonRequest;
import com.machpay.api.quiz.dto.Top10SeasonStatsResponse;
import com.machpay.api.quiz.repository.QuizSeasonRepository;
import com.machpay.api.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional
    public QuizSeason create(QuizSeasonRequest quizSeasonRequest) {
        QuizSeason quizSeason = new QuizSeason();
        quizSeason.setActive(true);
        quizSeason.setTitle(quizSeasonRequest.getTitle());
        quizSeason.setDuration(quizSeasonRequest.getDuration());
        quizSeason.setDescription(quizSeasonRequest.getDescription());
        quizSeason.setSeason(quizSeasonRepository.countAllByTitleNotNull() + 1);

        return quizSeasonRepository.save(quizSeason);
    }

    public QuizSeason getActiveSeason() {
        return quizSeasonRepository.findFirstByActiveTrue().orElseThrow(() -> new ResourceNotFoundException("Quiz " +
                "Season", "active", true));
    }

    public Optional<QuizSeason> findActiveSeason() {
        return quizSeasonRepository.findFirstByActiveTrue();
    }

    @Transactional
    public QuizSeason hostNewSeason(QuizSeasonRequest quizSeasonRequest) {
        Optional<QuizSeason> currentSeason = quizSeasonService.findActiveSeason();

        if (!currentSeason.isPresent()) {
            return create(quizSeasonRequest);
        }

        throw new BadRequestException("A season is currently running");
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
        declareCurrentSeasonWinner();
        QuizSeason quizSeason = getActiveSeason();
        quizSeason.setActive(false);

        quizSeasonRepository.save(quizSeason);
    }

    public List<Top10SeasonStatsResponse> getTop10SeasonStats() {
        List<QuizSeason> top10Seasons = quizSeasonRepository.findTop10ByOrderByIdDesc();

        return top10Seasons.stream().map(season -> {
            Top10SeasonStatsResponse top10SeasonStatsResponse = new Top10SeasonStatsResponse();
            top10SeasonStatsResponse.setTitle(season.getTitle());
            top10SeasonStatsResponse.setActive(season.isActive());
            top10SeasonStatsResponse.setSeason(season.getSeason());
            top10SeasonStatsResponse.setDuration(season.getDuration());
            top10SeasonStatsResponse.setWinners(getWinnersBySeason(season));
            top10SeasonStatsResponse.setDescription(season.getDescription());
            top10SeasonStatsResponse.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd").format(season.getCreatedAt()));
            top10SeasonStatsResponse.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd").format(season.getUpdatedAt()));
            top10SeasonStatsResponse.setEndsAt(new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(season.getCreatedAt(),
                    season.getDuration().intValue())));

            return top10SeasonStatsResponse;

        }).collect(Collectors.toList());
    }

    private List<QuizPlayResponse> getWinnersBySeason(QuizSeason season) {
        List<QuizResult> quizResult = quizResultService.getWinnersBySeason(season);

        return quizResult.stream().map(result -> {
            QuizPlayResponse quizPlayResponse = new QuizPlayResponse();
            QuizPlay quizPlay = quizPlayService.findByUserAndSeason(result.getWinner(), season);
            QuizPlayResponse.Player player = quizPlayService.getPlayer(result.getWinner());
            quizPlayResponse.setPlayer(player);
            quizPlayResponse.setPoint(quizPlay.getPoint());
            quizPlayResponse.setGamePlayed(quizPlay.getGamePlayed());

            return quizPlayResponse;
        }).collect(Collectors.toList());
    }
}
