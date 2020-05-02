package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class QuestionRequest {
    @NotNull
    private  String question;

    @NotNull
    private Long point;

    private String category;

    @NotNull
    private List<AnswerRequest> answers;
}
