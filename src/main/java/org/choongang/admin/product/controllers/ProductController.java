package org.choongang.admin.product.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.config.service.ConfigInfoService;
import org.choongang.admin.menus.AdminMenu;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.ListData;
import org.choongang.commons.MenuDetail;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.commons.exceptions.UnAuthorizedException;
import org.choongang.farmer.management.menus.FarmerMenu;
import org.choongang.file.service.FileInfoService;
import org.choongang.member.MemberUtil;
import org.choongang.member.entities.Farmer;
import org.choongang.member.service.MemberInfoService;
import org.choongang.product.constants.MainCategory;
import org.choongang.product.constants.ProductStatus;
import org.choongang.product.entities.Category;
import org.choongang.product.entities.Product;
import org.choongang.product.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller("adminProductController")
@RequiredArgsConstructor
@RequestMapping({"/admin/product", "/farmer/product"})
@SessionAttributes("requestProduct")
public class ProductController implements ExceptionProcessor {

    private final HttpServletRequest request;
    private final CategoryValidator categoryValidator;
    private final CategorySaveService categorySaveService;
    private final CategoryInfoService categoryInfoService;
    private final ProductValidator productValidator;
    private final ProductSaveService productSaveService;
    private final MemberUtil memberUtil;
    private final MemberInfoService memberInfoService;
    private final ProductInfoService productInfoService;
    private final ProductDeleteService productDeleteService;
    private final CategoryDeleteService categoryDeleteService;
    private final ProductDisplayService productDisplayService;
    private final ConfigInfoService configInfoService;
    private final FileInfoService fileInfoService;


    @ModelAttribute("menuCode")
    public String getMenuCode(){
        return "product";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> getSubMenus(){
        if (memberUtil.isFarmer()) {
            return FarmerMenu.getMenus("products") ;
        }

        return AdminMenu.getMenus("product");
    }

    @ModelAttribute("requestProduct")
    public RequestProduct requestProduct() {
        return new RequestProduct();
    }

    @GetMapping("/select")
    public String selectCate(Model model){
        List<Category> categories = categoryInfoService.getList();

        List<String> addCss = new ArrayList<>();
        addCss.add("product/select");
        List<String> addJs = new ArrayList<>();
        addJs.add("product/select");

        model.addAttribute("addCss", addCss);
        model.addAttribute("addScript", addJs);

        return "admin/product/select_category";
    }

    /**
     * 상품 목록
     * @param model
     * @return
     */
    @GetMapping
    public String list(@ModelAttribute ProductSearch form ,Model model){

        commonProcess("list", model);

        ListData<Product> data = productInfoService.getList(form, true, false);
        List<String> cateCd = categoryInfoService.getList().stream().map(s -> s.getCateCd()).toList();
        List<String> addCss = new ArrayList<>();


        model.addAttribute("items", data.getItems());
        model.addAttribute("pagenation", data.getPagination());
        model.addAttribute("cateCd", cateCd);

        return "admin/product/list";
    }

    /**
     * 상품 등록
     * @param model
     * @return
     */

    @GetMapping("/add")
    public String add(@ModelAttribute RequestProduct form, Model model){
        commonProcess("add", model);

        if(!memberUtil.isAdmin() && !memberUtil.isFarmer()){
            throw new UnAuthorizedException();
        }

        if(memberUtil.isAdmin()){
            List<Farmer> farmers = memberInfoService.getFarmerList();
            model.addAttribute("farmers", farmers);
        }

        List<Category> categories = categoryInfoService.getList();
        model.addAttribute("categories", categories); // 사용가능한 category들 추가

        return "admin/product/add";
    }

    @GetMapping("/edit/{seq}")
    public String edit(@PathVariable("seq") Long seq, Model model){

        commonProcess("edit", model);

        RequestProduct form = productInfoService.getForm(seq);
        System.out.println("폼 " + form );
        form.setMode("edit");

        String cateNm = categoryInfoService.get(form.getCateCd()).getCateNm();

        model.addAttribute("requestProduct", form);
        model.addAttribute("cateNm", cateNm);
        return "admin/product/edit";
    }

    @PatchMapping
    public String editList(@RequestParam("chk") List<Integer> chks, Model model){

        commonProcess("list", model);
        productSaveService.saveList(chks);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    @DeleteMapping
    public String deleteList(@RequestParam("chk") List<Integer> chks, Model model){
        commonProcess("list", model);


        productDeleteService.deleteList(chks);
        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }



    /**
     * 상품 등록, 수정처리
     * @param model
     * @return
     */
    @PostMapping("/save")
    public String save(@Valid RequestProduct form, Errors errors, Model model, SessionStatus status){
        String mode = form.getMode();
        commonProcess(mode, model);

        productValidator.validate(form, errors);

        if(errors.hasErrors()){
            if(memberUtil.isAdmin()){
                List<Farmer> farmers = memberInfoService.getFarmerList();
                model.addAttribute("farmers", farmers);
            }
            String gid = form.getGid();
            form.setEditorImages(fileInfoService.getList(gid, "description"));
            form.setMainImages(fileInfoService.getList(gid, "product_main"));
            form.setListImages(fileInfoService.getList(gid, "product_list"));

            return "admin/product/" + mode;
        }

        productSaveService.save(form);

        status.setComplete();

        return "redirect:/admin/product";

    }


    @GetMapping("/category")
    public String category(@ModelAttribute RequestCategory form, Model model){
        commonProcess("category", model);

       /* if(!memberUtil.isAdmin()){
            throw new UnAuthorizedException();
        }
        */
        List<Category> items = categoryInfoService.getList();
        Month[] months = Month.values();

        model.addAttribute("items", items);
        model.addAttribute("months", months);


        return "admin/product/category";

    }

    /**
     * 상품분류 추가, 수정
     * @return
     */

    @PostMapping("/category")
    public String categoryPs(@Valid RequestCategory form, Errors errors, Model model){
        commonProcess("category", model);

        categoryValidator.validate(form, errors);

        if(errors.hasErrors()){
            List<String> messages = errors.getFieldErrors().stream()
                    .map(e -> e.getCodes())
                    .map(s -> Utils.getMessage(s[0]))
                    .toList();

            throw new AlertException(messages.get(0), HttpStatus.BAD_REQUEST);
        }

        categorySaveService.save(form);

        //분류 추가가 완료되면 부모창 새로고침
        model.addAttribute("script", "parent.location.reload()");

        return "common/_execute_script";
    }

    @PatchMapping("/category")
    public String categoryEdit(@RequestParam("chk") List<Integer> chks, Model model){
        commonProcess("category", model);

        categorySaveService.saveList(chks);

        // 수정 완료 -> 새로고침
        model.addAttribute("script", "parent.location.reload()");

        return "common/_execute_script";
    }



    @DeleteMapping("/category")
    public String categoryDelete(@RequestParam("chk") List<Integer> chks, Model model){

        commonProcess("category", model);

        categoryDeleteService.deleteList(chks);
        // 삭제 완료 -> 새로고침
        model.addAttribute("script", "parent.location.reload()");
        return "common/_execute_script";
    }

    @GetMapping("/display")
    public String display(Model model){

        commonProcess("display", model);

        List<Long> codes = configInfoService.get("displayCodes", new TypeReference<List<Long>>() {}).orElse(null);

        model.addAttribute("codes", codes);



        return "admin/product/display";
    }


    @PostMapping("/display")
    public String displayPs(@RequestParam("num") List<Long> codes, Model model){
        commonProcess("display", model);

        productDisplayService.save(codes);

        model.addAttribute("script", "parent.location.reload();");

        return "common/_execute_script";
    }

    @GetMapping("/select_product")
    public String selectProduct(@RequestParam("target") String target, @ModelAttribute ProductSearch search, Model model){

        search.setLimit(7);
        ListData<Product> data = productInfoService.getList(search, true, false);
        List<String> cateCd = categoryInfoService.getList().stream().map(s -> s.getCateCd()).toList();


        model.addAttribute("items", data.getItems());
        model.addAttribute("pagenation", data.getPagination());
        model.addAttribute("cateCd", cateCd);

        model.addAttribute("addScript", new String[]{"product/select_product"});
        return "admin/product/select_product";
    }


    /**
     * 공통처리부분
     *
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model){

        mode = Objects.requireNonNullElse(mode, "list");
        String pageTitle = "상품목록";

        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if(mode.equals("add") || mode.equals("edit")){
            pageTitle = mode.equals("edit")? "상품수정" : "상품등록";
            addCommonScript.add("ckeditor5/ckeditor");
            addCommonScript.add("fileManager");
            addScript.add("product/form");


        } else if (mode.equals("category")) {
            pageTitle = "상품분류";
        }else if (mode.equals("display")){
            pageTitle = "상품진열 관리";
            addScript.add("product/display");
        }

        model.addAttribute("mainCategory", MainCategory.getList());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("addCommonScript", addCommonScript);
        model.addAttribute("addScript", addScript);
        model.addAttribute("subMenuCode", mode);
    }
}
