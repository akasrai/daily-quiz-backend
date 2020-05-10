package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentPlayerStatsResponse {
    private int position = 0;

    private Long point = Long.valueOf(0);

    private Long gamePlayed = Long.valueOf(0);

}
