package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AnswerRequest {
    @NotNull
    private String answer;

    @NotNull
    private boolean correct;
}
