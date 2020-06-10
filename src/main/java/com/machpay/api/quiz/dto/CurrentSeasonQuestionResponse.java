package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CurrentSeasonQuestionResponse {
    private String createdAt;

    private String question;

    private String category;

    private Long point;

    private List<Answers> answers;

    @Getter
    @Setter
    public static class Answers {
        private String answer;

        private boolean correct;
    }
}
