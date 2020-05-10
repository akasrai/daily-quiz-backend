package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentPlayerStatsResponse {
    private Long point;

    private int position;

    private Long gamePlayed;

}
