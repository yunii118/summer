package org.choongang.order.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.member.MemberUtil;
import org.choongang.order.entities.OrderInfo;
import org.choongang.order.entities.QOrderInfo;
import org.choongang.order.repositories.OrderInfoRepository;
import org.choongang.product.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class OrderInfoService {
    private final OrderInfoRepository orderInfoRepository;
    private final MemberUtil memberUtil;
    private final Utils utils;
    private final HttpServletRequest request;

    /**
     *  주문서 조회
     *
     * @Param seq
     * @return
     */
    public OrderInfo get(Long seq){
        OrderInfo orderInfo = orderInfoRepository.findById(seq).orElseThrow(OrderNotFoundException::new);

        return orderInfo;
    }

    /**
     * 최근 3개월 이내 주문내역 조회
     * @return
     */
    public ListData<OrderInfo> getList(){

        /* 3개월 이내 내역 조회 */
        QOrderInfo orderInfo = QOrderInfo.orderInfo;

        BooleanBuilder andBuilder = new BooleanBuilder();


        Long memberSeq = memberUtil.getMember().getSeq();
        LocalDateTime today = LocalDateTime.now().minusMonths(3);

        andBuilder.and(orderInfo.member.seq.eq(memberSeq));
        andBuilder.and(orderInfo.createdAt.goe(today));

        /* 페이징 처리 */
        int page = 1;
        int limit = utils.isMobile() ? 5 : 10;

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("createdAt")));

        Page<OrderInfo> data = orderInfoRepository.findAll(andBuilder, pageable);

        Pagination pagination = new Pagination(page, (int) data.getTotalElements(), 10, limit, request);

        List<OrderInfo> items = data.getContent();

        return new ListData<>(items, pagination);
    }

}
