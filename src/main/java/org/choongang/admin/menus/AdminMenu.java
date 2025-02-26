package org.choongang.admin.menus;

import org.choongang.commons.MenuDetail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdminMenu {

    private final static Map<String, List<MenuDetail>> adminMenus;

    static {
        adminMenus = new HashMap<>();
        adminMenus.put("config", Arrays.asList(
                new MenuDetail("config", "기본설정", "/admin/config"),
                new MenuDetail("payment", "결제설정", "/admin/config/payment"),
                new MenuDetail("api", "API 설정", "/admin/config/api")
        ));
        adminMenus.put("member", Arrays.asList(
            new MenuDetail("list", "회원목록", "/admin/member")
        ));

        adminMenus.put("board", Arrays.asList(
                new MenuDetail("list", "게시판목록", "/admin/board"),
                new MenuDetail("add", "게시판등록", "/admin/board/add"),
                new MenuDetail("posts", "게시글관리", "/admin/board/posts")
        ));

        adminMenus.put("recipe", Arrays.asList(
                new MenuDetail("list", "레시피리스트", "/admin/recipe"),
                new MenuDetail("category", "레시피분류등록", "/admin/recipe/category")
        ));

        adminMenus.put("product", Arrays.asList(
                new MenuDetail("list", "상품리스트", "/admin/product"),
                new MenuDetail("add", "상품등록", "/admin/product/add"),
                new MenuDetail("category", "상품분류", "/admin/product/category"),
                new MenuDetail("display", "상품진열관리", "/admin/product/display")
        ));
        adminMenus.put("order", Arrays.asList(
                new MenuDetail("list", "주문목록", "/admin/order"),
                new MenuDetail("setting", "주문설정", "/admin/order/setting")
        ));
        adminMenus.put("customer", Arrays.asList(
                new MenuDetail("list", "상담목록", "/admin/customer"),
                new MenuDetail("add", "상담등록", "/admin/customer/add")
        ));
        adminMenus.put("banner", Arrays.asList(
                new MenuDetail("group", "배너관리", "/admin/banner"),
                new MenuDetail("add", "배너등록", "/admin/banner/group")
        ));



    }

    public static List<MenuDetail> getMenus(String code) {
        return adminMenus.get(code);
    }
}
