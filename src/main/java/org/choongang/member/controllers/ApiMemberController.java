package org.choongang.member.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionRestProcessor;
import org.choongang.commons.rests.JSONData;
import org.choongang.member.repositories.MemberRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class ApiMemberController implements ExceptionRestProcessor {

    private final MemberRepository memberRepository;

    /**
     * 이메일 중복 여부 체크
     */
    @GetMapping("/email_dup_check")
    public JSONData<Object> duplicateEmailCheck(@RequestParam("email") String email) {
        boolean isExists = memberRepository.existsByEmail(email);

        JSONData<Object> data = new JSONData<>();
        data.setSuccess(isExists);

        return data;
    }

    /**
     * 아이디 중복 여부 체크
     */
    @GetMapping("/userId_dup_check")
    public JSONData<Object> duplicateIdCheck(@RequestParam("userId") String userId) {
        boolean isExists = memberRepository.existsByUserId(userId);

        JSONData<Object> data = new JSONData<>() ;
        data.setSuccess(isExists);

        return data ;
    }

    /**
     * 닉네임 중복 여부 체크
     */
    @GetMapping("/nickname_dup_check")
    public JSONData<Object> duplicateNicknameCheck(@RequestParam("nickname") String nickname) {
        boolean isExists = memberRepository.existsByNickname(nickname);

        JSONData<Object> data = new JSONData<>() ;
        data.setSuccess(isExists);

        return data ;
    }
}