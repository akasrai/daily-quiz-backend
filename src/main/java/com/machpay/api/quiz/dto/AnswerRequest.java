package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class AnswerRequest {
    @NotNull
    private UUID question;

    @NotNull
    private Long answer;

    @NotNull
    private Long timeTaken;
}
