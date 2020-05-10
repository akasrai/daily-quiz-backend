package com.machpay.api.quiz;

import com.machpay.api.entity.Member;
import com.machpay.api.entity.QuizPlay;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.entity.User;
import com.machpay.api.quiz.dto.CurrentPlayerStatsResponse;
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

    public boolean isEligible(UserPrincipal userPrincipal) {
        QuizSeason quizSeason = quizSeasonService.getActiveSeason();
        if (quizQuestionAnswerService.existBySeason(quizSeason)) {
            User user = userService.findByEmail(userPrincipal.getEmail());
            Optional<QuizPlay> quizPlay = quizPlayRepository.findByUserAndSeason(user, quizSeason);

            if (quizPlay.isPresent()) {
                return !quizPlay.get().isLocked();
            }

            return true;
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
        QuizSeason currentSeason = quizSeasonService.getActiveSeason();
        List<QuizPlay> quizPlays = getAllByPosition(currentSeason);
        User user = userService.findByEmail(userPrincipal.getEmail());

        return calculateGamePosition(user, quizPlays);
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

    public List<QuizPlayResponse> getLeaderBoard() {
        QuizSeason currentSeason = quizSeasonService.getActiveSeason();
        List<QuizPlay> quizPlays = getAllByPosition(currentSeason);

        return getQuizPlayResponse(quizPlays);
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
