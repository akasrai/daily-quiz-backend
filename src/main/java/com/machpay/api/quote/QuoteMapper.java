package com.machpay.api.quote;

import com.machpay.api.entity.Quote;
import com.machpay.api.quote.dto.QuoteRequest;
import com.machpay.api.quote.dto.QuoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface QuoteMapper {
    Quote toQuote(QuoteRequest quoteRequest);

    @Mapping(source = "content", target = "quote")
    QuoteResponse toQuoteResponse(Quote quote);
}
