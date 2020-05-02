package com.machpay.api.quote;

import com.machpay.api.common.exception.ResourceNotFoundException;
import com.machpay.api.entity.Quote;
import com.machpay.api.entity.User;
import com.machpay.api.quote.dto.QuoteRequest;
import com.machpay.api.quote.dto.QuoteResponse;
import com.machpay.api.user.UserService;
import com.machpay.api.user.admin.AdminService;
import com.machpay.api.user.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class QuoteService {
    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteMapper quoteMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MemberService memberService;

    public Quote findById(Long id) {
        return quoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quote", "id", id));
    }

    @Transactional
    public void create(QuoteRequest quoteRequest, Long useId) {
        User user = userService.findById(useId);
        Quote quote = quoteMapper.toQuote(quoteRequest);
        quote.setUser(user);

        quoteRepository.save(quote);
    }

    public QuoteResponse getRandomQuote() {
        Long quoteId = getRandomQuoteId();
        Quote quote = findById(quoteId);
        QuoteResponse quoteResponse = quoteMapper.toQuoteResponse(quote);
        quoteResponse.setWriter(getWriter(quote.getUser()));

        return quoteResponse;
    }

    private String getWriter(User user) {
        if (adminService.existsByEmail(user.getEmail())) {
            return adminService.findByEmail(user.getEmail()).getFullName();
        }

        return memberService.findById(user.getId()).getFullName();
    }

    private Long getRandomQuoteId() {
        int totalQuote = quoteRepository.countAllByActiveTrue();
        Random rand = new SecureRandom();
        int randomId = rand.nextInt(totalQuote - 1) + 1;

        return Long.valueOf(randomId);
    }
}
