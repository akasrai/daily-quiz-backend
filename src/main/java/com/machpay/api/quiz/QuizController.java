package com.machpay.api.quiz;

import com.machpay.api.quiz.dto.QuestionRequest;
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
    private QuizService quizService;

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public void createQuiz(@Valid @RequestBody QuestionRequest questionRequest,
                           @CurrentUser UserPrincipal userPrincipal) {
        quizService.createQuestion(questionRequest, userPrincipal.getId());
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('USER')")
    public void getLatestQuestion() {
        quizService.getLatestQuestion();
    }
}
