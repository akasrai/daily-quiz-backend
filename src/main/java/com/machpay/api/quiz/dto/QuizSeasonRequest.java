package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class QuizSeasonRequest {
    @NotNull
    private String title;

    private Long duration;

    private String description;
}
