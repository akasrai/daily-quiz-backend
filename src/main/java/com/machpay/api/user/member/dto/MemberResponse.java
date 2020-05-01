package com.machpay.api.user.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponse {
    private Long id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String imageUrl;
}
