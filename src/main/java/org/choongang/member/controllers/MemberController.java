package org.choongang.member.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.choongang.commons.exceptions.UnAuthorizedException;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileInfoService;
import org.choongang.member.MemberUtil;
import org.choongang.member.service.*;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.service.OrderInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@SessionAttributes({"EmailAuthVerified", "BusinessNoVerified", "mType"})    // model에 같은 속성명의 값이 있으면 세션에 저장하여 유지됨
public class MemberController implements ExceptionProcessor {

    private final Utils utils;
    private final JoinService joinService;
    private final FindPwService findPwService ;
    private final FileInfoService fileInfoService;
    private final MemberUtil memberUtil ;
    private final MemberInfoService memberInfoService ;
    private final InfoSaveService infoSaveService ;
    private final MemberDeleteService memberDeleteService ;
    private final HttpServletResponse response;
    private final HttpSession session ;
    private final NonmemberValidator nonmemberValidator;
    private final OrderInfoService orderInfoService;

    @GetMapping("/join")
    public String join(@ModelAttribute RequestJoin form, Model model) {
        commonProcess("join", model);

        String mType = "M" ;
        session.setAttribute("mType", mType) ;

        // 이메일 인증과 사업자등록 번호 인증 여부 false --> 초기화
        model.addAttribute("EmailAuthVerified", false);
        model.addAttribute("BusinessNoVerified", false) ;

        return utils.tpl("member/join");
    }

    @PostMapping("/join")
    public String joinPs(@Valid RequestJoin form, Errors errors, Model model,
                         SessionStatus sessionStatus) {
        commonProcess("join", model);

        joinService.process(form, errors);

        if (errors.hasErrors()) {
            return utils.tpl("member/join");
        }

        // 세션값 비우기 --> 이메일 인증과 사업자등록번호 인증 초기화
        sessionStatus.setComplete();

        // 회원가입 완료 메세지 띄우기
        try {
            response.setContentType("text/html; charset=utf-8");
            PrintWriter writer = response.getWriter() ;
            writer.write("<script>alert('회원가입을 완료했습니다!😊'); " +
                    "location.href='/member/login'</script>");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/member/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        commonProcess("login", model);

        return utils.tpl("member/login");
    }

    /**
     * 회원정보 페이지로
     */
    @GetMapping("/info")
    public String info(Model model) {
        commonProcess("memberInfo", model);

        if (!memberUtil.isLogin()) {
            throw new UnAuthorizedException("로그인이 필요한 페이지입니다.") ;
        }

        RequestMemberInfo form = memberInfoService.getMemberInfo() ;
        model.addAttribute("requestMemberInfo", form) ;

        return utils.tpl("member/info");
    }

    /**
     * 회원정보 수정
     */
    @PatchMapping("/info")
    public String infoSave(@Valid RequestMemberInfo form, Errors errors, Model model) {
        commonProcess("memberInfo", model);

        infoSaveService.saveInfo(form, errors);

        if (errors.hasErrors()) {
            List<FileInfo> profileImages = fileInfoService.getList(form.getGid(), "profile_img");
            if (profileImages != null && !profileImages.isEmpty()) {
                form.setProfileImage(profileImages.get(0));
            }

            List<FileInfo> businessPermitFiles = fileInfoService.getList(form.getGid(), "business_permit");
            form.setBusinessPermitFiles(businessPermitFiles);

            return utils.tpl("member/info");
        }

        return "redirect:/mypage";
    }

    /**
     * 회원 탈퇴 처리
     */
    @GetMapping("/delete")
    public String deleteMember(Model model) {
        commonProcess("deleteMember", model);

        memberDeleteService.deleteMember();

        String script = String.format("alert('%s'); location.href='/member/logout';", Utils.getMessage("탈퇴처리되었습니다.", "commons"));
        model.addAttribute("script", script) ;

        return "/common/_execute_script" ;
    }

    /**
     * 비밀번호 찾기 양식
     */
    @GetMapping("/find_pw")
    public String findPw(@ModelAttribute RequestFindPw form, Model model) {
        commonProcess("find_pw", model);

        return utils.tpl("member/find_pw");
    }

    /**
     * 비밀번호 찾기 처리
     */
    @PostMapping("/find_pw")
    public String findPwPs(@Valid RequestFindPw form, Errors errors, Model model) {
        commonProcess("find_pw", model);

        findPwService.process(form, errors); // 비밀번호 찾기 처리

        if (errors.hasErrors()) {
            return utils.tpl("member/find_pw");
        }

        // 비밀번호 찾기에 이상 없다면 완료 페이지로 이동
        return "redirect:/member/find_pw_done";
    }

    /**
     * 비밀번호 찾기 완료 페이지
     *
     * @param model
     * @return
     */
    @GetMapping("/find_pw_done")
    public String findPwDone(Model model) {
        commonProcess("find_pw", model);

        return utils.tpl("member/find_pw_done");
    }

    /**
     * 장바구니 이동 페이지
     *
     * @param model
     * @return
     */
    @GetMapping("/cart")
    public String cart(Model model){
        commonProcess("cart", model);

        return utils.tpl("member/cart");

    }


    /**
     * 비회원 주문조회
     * @param model
     * @return
     */
    @GetMapping("/not")
    public String notMember(@ModelAttribute RequestNonmember form, Model model){
        commonProcess("not", model);



        return utils.tpl("member/member_not");



    }

    @PostMapping("/not")
    public String notMemberPs(@Valid RequestNonmember form, Errors errors, Model model){
        commonProcess("not", model);
        nonmemberValidator.validate(form, errors);

        if (errors.hasErrors()) {

            return utils.tpl("member/member_not");
        }

        OrderInfo orderInfo = orderInfoService.getNonmember(form.getOrderNo(), form.getOrderCellPhone());

        if(orderInfo == null){
            throw new AlertBackException("올바르지 않은 주문번호/주문자 전화번호 입니다.", HttpStatus.BAD_REQUEST);
        }

        return "redirect:/order/detail/" + orderInfo.getSeq();
    }

    private void commonProcess(String mode, Model model) {
        mode = StringUtils.hasText(mode) ? mode : "join";
        String pageTitle = Utils.getMessage("회원가입", "commons");

        List<String> addCommonScript = new ArrayList<>();    // 공통 자바스크립트
        List<String> addCss = new ArrayList<>();
        List<String> addScript = new ArrayList<>();    // 프론트 자바스크립트

        if (mode.equals("login")) { // 로그인
            addCss.add("member/login");
            pageTitle = Utils.getMessage("로그인", "commons");
        } else if (mode.equals("join")) { // 회원가입
            // 공통 JS
            addCommonScript.add("address");
            addCommonScript.add("fileManager");
            // 회원가입 페이지를 위한 CSS, JS
            addScript.add("member/join");
            addScript.add("member/form");
            addCss.add("member/join") ;

        } else if (mode.equals("find_pw")) { // 비밀번호 찾기
            pageTitle = Utils.getMessage("비밀번호_찾기", "commons");
            addCss.add("member/login");
        } else if (mode.equals("cart")) {
            // 장바구니
            pageTitle = Utils.getMessage("장바구니", "commons");
        } else if (mode.equals("memberInfo")) {
            pageTitle = Utils.getMessage("회원정보_수정", "commons") ;
            addScript.add("member/info") ;
            addScript.add("member/form");
            addCommonScript.add("address");
            addCommonScript.add("fileManager");
        } else if (mode.equals("not")) {
            pageTitle = "비회원 주문조회";
            addCss.add("member/login");
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addCss", addCss);
        model.addAttribute("addScript", addScript);
        model.addAttribute("addCommonScript", addCommonScript);
    }



}