package org.choongang.order.controllers;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.choongang.cart.constants.CartType;
import org.choongang.cart.service.CartData;
import org.choongang.commons.AddressAssist;
import org.choongang.member.entities.AbstractMember;
import org.choongang.member.entities.Address;

import java.util.List;

@Data
public class RequestOrder {

    private List<Long> cartSeq; // 장바구니 등록 번호

    private CartType cartType;

    @NotBlank
    private String orderName; // 주문자 이름

    @NotBlank
    private String orderCellPhone1; // 주문자 번호
    @NotBlank
    private String orderCellPhone2; // 주문자 번호
    @NotBlank
    private String orderCellPhone3; // 주문자 번호

    @NotBlank
    @Email
    private String orderEmail; // 주문자 이메일

    @NotBlank
    private String receiverName; // 받는분 이름

    @NotBlank
    private String receiverCellPhone1; // 받는분 번호
    @NotBlank
    private String receiverCellPhone2; // 받는분 번호
    @NotBlank
    private String receiverCellPhone3; // 받는분 번호

    @NotBlank
    private String zoneCode; // 받는사람 우편번호

    @NotBlank
    private String address; // 받는사람 주소

    private String addressSub; // 받는사람 세부주소

    private String deliveryMemo; // 배송 메모

    private int usePoint = 0;

    private int payPrice;

    private String depositor; // 무통장 입금일 경우 입금자 명

    @AssertTrue
    private boolean agree; // 결제약관 동의 여부

}
