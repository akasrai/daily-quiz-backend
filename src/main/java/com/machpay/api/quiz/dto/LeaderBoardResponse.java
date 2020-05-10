package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeaderBoardResponse {
    private String type;

    List<QuizPlayResponse> results;
}
