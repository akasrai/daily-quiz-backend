package com.machpay.api.quiz;

import com.machpay.api.common.ListResponse;
import com.machpay.api.quiz.dto.AnswerRequest;
import com.machpay.api.quiz.dto.AnswerResponse;
import com.machpay.api.quiz.dto.PlayerCurrentStatusResponse;
import com.machpay.api.quiz.dto.QuestionRequest;
import com.machpay.api.quiz.dto.QuestionResponse;
import com.machpay.api.quiz.dto.QuizPlayResponse;
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
import java.util.List;

@RestController
@RequestMapping("/v1/quiz")
public class QuizController {
    @Autowired
    private QuizService quizService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public void createQuiz(@Valid @RequestBody QuestionRequest questionRequest,
                           @CurrentUser UserPrincipal userPrincipal) {
        quizService.createQuestion(questionRequest, userPrincipal.getId());
    }

    @GetMapping("")
    @PreAuthorize("hasRole('MEMBER')")
    public QuestionResponse getLatestQuestion() {
        return quizService.getLatestQuestion();
    }

    @PostMapping("/answer")
    @PreAuthorize("hasRole('MEMBER')")
    public AnswerResponse answer(@Valid @RequestBody AnswerRequest answerRequest,
                                 @CurrentUser UserPrincipal userPrincipal) {
        return quizService.checkAnswer(answerRequest, userPrincipal.getId());
    }

    @GetMapping("/leaderboard")
    @PreAuthorize("hasRole('MEMBER')")
    public ListResponse getLeaderBoard() {
        List<QuizPlayResponse> quizPlayResponseList = quizService.getLeaderBoard();

        return new ListResponse(quizPlayResponseList);
    }

    @GetMapping("/current/stats")
    @PreAuthorize("hasRole('MEMBER')")
    public PlayerCurrentStatusResponse getPlayerCurrentStatus(@CurrentUser UserPrincipal userPrincipal) {
        return quizService.getPlayerCurrentStatus(userPrincipal);
    }

    @GetMapping("/permission")
    @PreAuthorize("hasRole('MEMBER')")
    public boolean isEligible(@CurrentUser UserPrincipal userPrincipal) {
        return quizService.isEligible(userPrincipal);
    }
}
