package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionResponse {
    private String question;

    private Long point;

    private List<AnswerResponse> answers;
}
