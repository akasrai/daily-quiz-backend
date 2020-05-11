package com.machpay.api.quiz;

import com.machpay.api.common.enums.ResultType;
import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Member;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizResult;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.CurrentPlayerStatsResponse;
import com.machpay.api.quiz.dto.LeaderBoardResponse;
import com.machpay.api.quiz.dto.QuizPlayResponse;
import com.machpay.api.quiz.repository.QuizPlayRepository;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.UserService;
import com.machpay.api.user.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizPlayService {
    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private QuizSeasonService quizSeasonService;

    @Autowired
    private QuizPlayRepository quizPlayRepository;

    @Autowired
    private QuizQuestionAnswerService quizQuestionAnswerService;


    public List<QuizPlay> getTop3QuizPlay(QuizSeason quizSeason) {
        return quizPlayRepository.findTop3BySeasonOrderByPointDescTimeTakenAsc(quizSeason);
    }

    public List<QuizPlay> getAllByPosition(QuizSeason quizSeason) {
        return quizPlayRepository.findAllBySeasonOrderByPointDescTimeTakenAsc(quizSeason);
    }

    public QuizPlay findByUserAndSeason(User user, QuizSeason season) {
        return quizPlayRepository.findByUserAndSeason(user, season).orElseThrow(() -> new ResourceNotFoundException(
                "User", "user id", user.getId()));
    }

    public boolean isEligible(UserPrincipal userPrincipal) {
        Optional<QuizSeason> currentSeason = quizSeasonService.findActiveSeason();

        if(currentSeason.isPresent()) {
            if (quizQuestionAnswerService.existBySeason(currentSeason.get())) {
                User user = userService.findByEmail(userPrincipal.getEmail());
                Optional<QuizPlay> quizPlay = quizPlayRepository.findByUserAndSeason(user, currentSeason.get());

                if (quizPlay.isPresent()) {
                    return !quizPlay.get().isLocked();
                }

                return true;
            }

            return false;
        }

        return false;
    }

    @Transactional
    public QuizPlay updateQuizPlay(User user, Long point, Long timeTaken) {
        QuizSeason quizSeason = quizSeasonService.getActiveSeason();
        Optional<QuizPlay> existingPoints = quizPlayRepository.findByUserAndSeason(user, quizSeason);

        if (existingPoints.isPresent()) {
            QuizPlay existingQuizPlay = existingPoints.get();
            existingQuizPlay.setPoint(existingQuizPlay.getPoint() + point);
            existingQuizPlay.setTimeTaken(existingQuizPlay.getTimeTaken() + timeTaken);

            return quizPlayRepository.save(existingQuizPlay);
        }

        return createQuizPlay(user, quizSeason);
    }

    @Transactional
    public void lockPlayerForQuiz(User user) {
        QuizSeason quizSeason = quizSeasonService.getActiveSeason();
        Optional<QuizPlay> existingPoints = quizPlayRepository.findByUserAndSeason(user, quizSeason);

        if (existingPoints.isPresent()) {
            QuizPlay existingQuizPlay = existingPoints.get();
            existingQuizPlay.setLocked(true);
            existingQuizPlay.setGamePlayed(existingQuizPlay.getGamePlayed() + 1);

            quizPlayRepository.save(existingQuizPlay);
        }

        createQuizPlay(user, quizSeason);
    }

    @Transactional
    public QuizPlay createQuizPlay(User user, QuizSeason quizSeason) {
        QuizPlay quizPlay = new QuizPlay();
        quizPlay.setUser(user);
        quizPlay.setLocked(true);
        quizPlay.setSeason(quizSeason);
        quizPlay.setPoint(Long.valueOf(0));
        quizPlay.setTimeTaken(Long.valueOf(0));
        quizPlay.setGamePlayed(Long.valueOf(1));

        return quizPlayRepository.save(quizPlay);
    }

    public CurrentPlayerStatsResponse getCurrentPlayerStats(UserPrincipal userPrincipal) {
       Optional<QuizSeason> currentSeason = quizSeasonService.findActiveSeason();

       if(currentSeason.isPresent()) {
           List<QuizPlay> quizPlays = getAllByPosition(currentSeason.get());
           User user = userService.findByEmail(userPrincipal.getEmail());

           return calculateGamePosition(user, quizPlays);
       }

       return new CurrentPlayerStatsResponse();
    }

    private CurrentPlayerStatsResponse calculateGamePosition(User user, List<QuizPlay> quizPlays) {
        int position = 0;
        CurrentPlayerStatsResponse currentPlayerStatsResponse = new CurrentPlayerStatsResponse();

        for (QuizPlay quizPlay : quizPlays) {
            position++;

            if (user.equals(quizPlay.getUser())) {
                currentPlayerStatsResponse.setPosition(position);
                currentPlayerStatsResponse.setPoint(quizPlay.getPoint());
                currentPlayerStatsResponse.setGamePlayed(quizPlay.getGamePlayed());

                break;
            }
        }

        return currentPlayerStatsResponse;
    }

    public LeaderBoardResponse getLeaderBoard() {
      Optional<QuizSeason> currentSeason = quizSeasonService.findActiveSeason();

        if (currentSeason.isPresent()) {
            List<QuizPlay> quizPlays = getAllByPosition(currentSeason.get());

            return getLeaderBoardResponse(quizPlays, ResultType.SEASON_LEADER_BOARD);
        }

        List<QuizResult> results = quizResultService.getWinnersBySeason();

        return getLeaderBoardResponse(getQuizPlayFromWinners(results), ResultType.SEASON_WINNERS);
    }

    private List<QuizPlay> getQuizPlayFromWinners(List<QuizResult> results) {
        return results.stream().map(quizResult -> {
            User user = quizResult.getWinner();
            QuizSeason quizSeason = quizResult.getSeason();

            return findByUserAndSeason(user, quizSeason);
        }).collect(Collectors.toList());

    }

    private LeaderBoardResponse getLeaderBoardResponse(List<QuizPlay> quizPlays, ResultType resultType) {
        LeaderBoardResponse leaderBoardResponse = new LeaderBoardResponse();
        leaderBoardResponse.setType(resultType.name());
        leaderBoardResponse.setResults(getQuizPlayResponse(quizPlays));

        return leaderBoardResponse;
    }

    private List<QuizPlayResponse> getQuizPlayResponse(List<QuizPlay> quizPlays) {
        return quizPlays.stream().map(quizPlay -> {
            QuizPlayResponse quizPlayResponse = new QuizPlayResponse();
            quizPlayResponse.setPoint(quizPlay.getPoint());
            quizPlayResponse.setGamePlayed(quizPlay.getGamePlayed());
            quizPlayResponse.setPlayer(getPlayer(quizPlay.getUser()));

            return quizPlayResponse;
        }).collect(Collectors.toList());
    }

    private QuizPlayResponse.Player getPlayer(User user) {
        QuizPlayResponse.Player player = new QuizPlayResponse.Player();
        Member member = memberService.findById(user.getId());
        player.setName(member.getFullName());
        player.setPhoto(member.getPhoto());

        return player;
    }
}
