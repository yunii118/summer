package org.choongang.order.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.member.entities.Farmer;
import org.choongang.member.repositories.FarmerRepository;
import org.choongang.order.constants.OrderStatus;
import org.choongang.order.controllers.OrderSearch;
import org.choongang.order.entities.OrderItem;
import org.choongang.order.entities.QOrderItem;
import org.choongang.order.repositories.OrderItemRepository;
import org.choongang.product.service.ProductInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemInfoService {

    private final OrderItemRepository orderItemRepository ;
    private final ProductInfoService productInfoService ;
    private final Utils utils ;
    private final HttpServletRequest request;
    private final EntityManager em;
    private final FarmerRepository farmerRepository;

    /**
     * 상품당 month개월 이내 판매 내역
     *
     * @param month
     * @param productSeq
     * @return
     */
    public ListData<OrderItem> getListMonth(int month, Long productSeq){
        QOrderItem orderItem = QOrderItem.orderItem;

        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime refDay = LocalDateTime.now().minusMonths(month);
        builder.and(orderItem.product.seq.eq(productSeq));
        builder.and(orderItem.createdAt.goe(refDay));

        /* 페이징 처리 */
        String pageStr = StringUtils.hasText(request.getParameter("page")) ?request.getParameter("page") : "1" ;
        String limitStr = StringUtils.hasText(request.getParameter("limit")) ? request.getParameter("limit") : "10";

        int page = Utils.onlyPositiveNumber(Integer.parseInt(pageStr), 1);
        int limit = Utils.onlyPositiveNumber(Integer.parseInt(limitStr), 10);

        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<OrderItem> data = orderItemRepository.findAll(builder, pageable);

        Pagination pagination = new Pagination(page, (int) data.getTotalElements(), 10, limit, request);

        List<OrderItem> items = data.getContent();

        return new ListData<>(items, pagination);
    }


    /**
     * 모든 주문 내역 조회
     */
    public ListData<OrderItem> getAll(OrderSearch search, String farmerId) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);

        QOrderItem orderItem = QOrderItem.orderItem ;
        BooleanBuilder andBuilder = new BooleanBuilder();

        /* 검색 조건 처리 S */
        String productNm = search.getProductNm();
        List<String> orderStatus = search.getOrderStatus() ;
        LocalDate sdate = search.getSdate();
        LocalDate edate = search.getEdate();

        String sopt = search.getSopt();
        sopt = StringUtils.hasText(sopt) ? sopt.trim() : "ALL";
        String skey = search.getSkey(); // 키워드

        // farmer인 경우에는 본인 주문만
        if (StringUtils.hasText(farmerId)) {
            andBuilder.and(orderItem.product.farmer.userId.eq(farmerId)) ;
        }

        if (StringUtils.hasText(productNm)) {
            andBuilder.and(orderItem.productName.contains(productNm.trim())) ;
        }

        if (orderStatus != null && !orderStatus.isEmpty()) {
            List<OrderStatus> statuses = orderStatus.stream().map(OrderStatus::valueOf).toList();
            andBuilder.and(orderItem.status.in(statuses));
        }

        if(sdate != null){
            andBuilder.and(orderItem.createdAt.goe(LocalDateTime.of(sdate, LocalTime.of(0, 0, 0))));
        }

        if (edate != null){
            andBuilder.and(orderItem.createdAt.loe(LocalDateTime.of(edate, LocalTime.of(23, 59, 59))));
        }

        // 조건 별 키워드 검색
        if (StringUtils.hasText(skey)) {
            skey = skey.trim() ;

            BooleanExpression cond1 = orderItem.orderInfo.orderName.contains(skey) ;
            BooleanExpression cond2 = orderItem.orderInfo.orderEmail.contains(skey) ;

            if (sopt.equals("orderName")) {
                andBuilder.and(cond1) ;
            } else if (sopt.equals("orderEmail")) {
                andBuilder.and(cond2) ;
            }
        }
        /* 검색 조건 처리 E */

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("createdAt")));
        Page<OrderItem> data = orderItemRepository.findAll(andBuilder, pageable);

        Pagination pagination = new Pagination(page, (int)data.getTotalElements(), limit, 10, request);

        return new ListData<>(data.getContent(), pagination);
    }

    public ListData<OrderItem> getAll(OrderSearch search) { return getAll(search, null) ; }

    /**
     * orderItem의 seq로 item 검색하여 반환
     */
    public OrderItem getOneItem(Long itemSeq) {
        OrderItem item = orderItemRepository.findById(itemSeq).orElseThrow(OrderItemNotFoundException::new) ;  // 없으면 예외 던짐

        productInfoService.addProductInfo(item.getProduct());    // product 정보 가공

        return item ;
    }

    public Long sumFarmersSale(Farmer farmer){
        QOrderItem orderItem = QOrderItem.orderItem;
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime refDay = LocalDateTime.now().minusMonths(3);
        builder.and(orderItem.createdAt.goe(refDay));
        builder.and(orderItem.product.farmer.eq(farmer));

        Iterator<OrderItem> iterator = orderItemRepository.findAll(builder).iterator();

        Long salePoint = 0L;

        while(iterator.hasNext()){
            OrderItem item = iterator.next();
            if(item.getStatus() == OrderStatus.DONE){
                salePoint += item.getEa();
            }
        }

        return salePoint;
    }

    /**
     * orderInfo seq로 item들 가져오기
     * @param infoSeq
     * @return
     */
    public List<OrderItem> getItem(Long infoSeq){
        QOrderItem orderItem = QOrderItem.orderItem;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(orderItem.orderInfo.seq.eq(infoSeq));

        Iterator<OrderItem> iterator = orderItemRepository.findAll(builder).iterator();
        List<OrderItem> items = new ArrayList<>();

        while(iterator.hasNext()){
            items.add(iterator.next());
        }

        return items;
    }




}
