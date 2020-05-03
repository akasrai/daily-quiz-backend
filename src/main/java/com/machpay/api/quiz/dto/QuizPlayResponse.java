package com.machpay.api.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizPlayResponse {
    private Long point;

    private Long gamePlayed;

    private Player player;

    @Getter
    @Setter
    public static class Player {
        private String name;

        private String photo;
    }
}
