package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class QuestionResponse {
    private UUID id;

    private String question;

    private Long point;

    private String category;

    private List<Answer> answers;

    @Getter
    @Setter
    public static class Answer {
        private Long id;

        private String answer;
    }
}
