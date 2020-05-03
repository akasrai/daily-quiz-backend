package com.machpay.api.user.member;

import com.machpay.api.common.ListResponse;
import com.machpay.api.security.CurrentUser;
import com.machpay.api.security.UserPrincipal;
import com.machpay.api.user.member.dto.MemberResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('MEMBER')")
    public MemberResponse getCurrentMember(@CurrentUser UserPrincipal userPrincipal) {
        return memberService.getCurrentMember(userPrincipal.getEmail());
    }

    @GetMapping("/locked")
    @PreAuthorize("hasRole('ADMIN')")
    public ListResponse lockedSendersList() {
        List<MemberResponse> senderResponses = memberService.getLockedSenders();

        return new ListResponse(senderResponses);
    }

    @GetMapping("/{referenceId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public void unLock(@PathVariable("referenceId") UUID referenceId) {
        memberService.unLock(referenceId);
    }
}