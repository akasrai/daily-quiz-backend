package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Top10SeasonStatsResponse {
    private String title;

    private int season;

    private String description;

    private boolean active;

    private String createdAt;

    private String updatedAt;

    private String endsAt;

    private Long duration;

    private List<QuizPlayResponse> winners;
}
