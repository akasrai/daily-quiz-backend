package com.machpay.api.quiz;

import com.machpay.api.common.ListResponse;
import com.machpay.api.entity.QuizSeason;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.AnswerResponse;
import com.machpay.api.quiz.dto.CurrentPlayerStatsResponse;
import com.machpay.api.quiz.dto.LeaderBoardResponse;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.dto.QuizSeasonRequest;
import com.machpay.api.security.CurrentUser;
import com.machpay.api.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/quiz")
public class QuizController {
    @Autowired
    private QuizPlayService quizPlayService;

    @Autowired
    private QuizSeasonService quizSeasonService;

    @Autowired
    private QuizQuestionAnswerService quizQuestionAnswerService;


    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public void createQuiz(@Valid @RequestBody QuestionRequest questionRequest,
                           @CurrentUser UserPrincipal userPrincipal) {
        quizQuestionAnswerService.createQuestion(questionRequest, userPrincipal.getId());
    }

    @GetMapping("")
    @PreAuthorize("hasRole('MEMBER')")
    public QuestionResponse getLatestQuestion(@CurrentUser UserPrincipal userPrincipal) {
        return quizQuestionAnswerService.getLatestQuestion(userPrincipal);
    }

    @PostMapping("/answer")
    @PreAuthorize("hasRole('MEMBER')")
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest answerRequest,
                                 @CurrentUser UserPrincipal userPrincipal) {
        return quizQuestionAnswerService.checkAnswer(answerRequest, userPrincipal.getId());
    }

    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('MEMBER')")
    public LeaderBoardResponse getLeaderBoard() {
        return quizPlayService.getLeaderBoard();
    }

    @GetMapping("/current/stats")
    @PreAuthorize("hasRole('MEMBER')")
    public CurrentPlayerStatsResponse getPlayerCurrentStats(@CurrentUser UserPrincipal userPrincipal) {
        return quizPlayService.getCurrentPlayerStats(userPrincipal);
    }

    @GetMapping("/permission")
    @PreAuthorize("hasRole('MEMBER')")
    public boolean isEligible(@CurrentUser UserPrincipal userPrincipal) {
        return quizPlayService.isEligible(userPrincipal);
    }

    @GetMapping("/end/season")
    @PreAuthorize(("hasRole('ADMIN')"))
    public void endCurrentSeason() {
        quizSeasonService.endCurrentSeason();
    }

    @PostMapping("/host/season")
    @PreAuthorize("hasRole('ADMIN')")
    public void hostNewSeason(@Valid @RequestBody QuizSeasonRequest quizSeasonRequest) {
        quizSeasonService.hostNewSeason(quizSeasonRequest);
    }

    @GetMapping("/current/season")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public QuizSeason getCurrentSeason() {
        return quizSeasonService.getActiveSeason();
    }

    @GetMapping("/seasons/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ListResponse getAllSeasonInfo() {
        return new ListResponse(quizSeasonService.getTop10SeasonStats());
    }

    @GetMapping("/current/season/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ListResponse getAllCurrentSeasonQuestion() {
        return new ListResponse(quizQuestionAnswerService.listAllCurrentSeasonQuestion());
    }
}
