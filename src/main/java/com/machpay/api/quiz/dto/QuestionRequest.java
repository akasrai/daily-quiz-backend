package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class QuestionRequest {

    @NotNull
    private String question;

    @NotNull
    private Long point = Long.valueOf("10");

    private String category;

    @NotNull
    private List<Answer> answers;

    @Getter
    @Setter
    public static class Answer {
        @NotNull
        private String answer;

        @NotNull
        private boolean correct;
    }
}
