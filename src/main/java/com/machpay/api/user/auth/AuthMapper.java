package com.machpay.api.user.auth;

import com.machpay.api.entity.Member;
import com.machpay.api.user.auth.dto.GuestInfoResponse;
import com.machpay.api.user.auth.dto.SignUpRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthMapper {
    GuestInfoResponse toGuestInfoResponse(Member member);

    SignUpRequest toSignUpRequest(Member member);
}