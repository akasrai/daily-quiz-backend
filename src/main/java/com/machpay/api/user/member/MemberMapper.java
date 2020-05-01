package com.machpay.api.user.member;

import com.machpay.api.entity.Member;
import com.machpay.api.user.auth.dto.SignUpRequest;
import com.machpay.api.user.member.dto.MemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MemberMapper {
    Member toMember(SignUpRequest signUpRequest);

    MemberResponse toMemberResponse(Member member);

    List<MemberResponse> toMemberResponseList(List<Member> members);
}