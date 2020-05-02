package com.machpay.api.quote.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class QuoteRequest {
    @NotNull
    private String content;

    private String author;
}
