package com.machpay.api.quote;

import com.machpay.api.quote.dto.QuoteRequest;
import com.machpay.api.quote.dto.QuoteResponse;
import com.machpay.api.security.CurrentUser;
import com.machpay.api.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/quote")
public class QuoteController {
    @Autowired
    private QuoteService quoteService;

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public void create(@Valid @RequestBody QuoteRequest quoteRequest, @CurrentUser UserPrincipal userPrincipal) {
        quoteService.create(quoteRequest, userPrincipal.getId());
    }

    @GetMapping("")
    public QuoteResponse getRandomQuote() {
        return quoteService.getRandomQuote();
    }
}
