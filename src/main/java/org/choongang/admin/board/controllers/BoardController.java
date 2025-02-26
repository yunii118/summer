package org.choongang.admin.board.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.menus.AdminMenu;
import org.choongang.board.entities.Board;
import org.choongang.board.service.config.BoardConfigDeleteService;
import org.choongang.board.service.config.BoardConfigInfoService;
import org.choongang.board.service.config.BoardConfigSaveService;
import org.choongang.commons.ListData;
import org.choongang.commons.MenuDetail;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.Pagination;
import org.choongang.member.constants.Authority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller("adminBoardController")
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class BoardController implements ExceptionProcessor {

    private final BoardConfigSaveService configSaveService;
    private final BoardConfigInfoService configInfoService;
    private final BoardConfigDeleteService configDeleteService;

    private final BoardConfigValidator configValidator;

    @ModelAttribute("menuCode")
    public String getMenuCode() { // 주 메뉴 코드
        return "board";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> getSubMenus() { // 서브 메뉴
        return AdminMenu.getMenus("board");
    }

    /**
     * 게시판 목록
     *
     * @return
     */
    @GetMapping
    public String list(@ModelAttribute BoardSearch search, Model model) {
        commonProcess("list", model);

        ListData<Board> data = configInfoService.getList(search, true);

        List<Board> items = data.getItems();
        Pagination pagination = data.getPagination();

        model.addAttribute("items", items);
        model.addAttribute("pagination", pagination);

        return "admin/board/list";
    }

    /**
     * 게시판 목록 - 수정
     *
     * @param chks
     * @return
     */
    @PatchMapping
    public String editList(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        configSaveService.saveList(chks); // 업데이트할 수 있게 넣어준다.

        model.addAttribute("script", "parent.location.reload();");

        return "common/_execute_script";
    }

    @DeleteMapping
    public String deleteList(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        configDeleteService.deleteList(chks);

        model.addAttribute("script", "parent.location.reload();");

        return "common/_execute_script";
    }

    /**
     * 게시판 등록
     *
     * @return
     */
    @GetMapping("/add")
    public String add(@ModelAttribute RequestBoardConfig config, Model model) {
        commonProcess("add", model);

        return "admin/board/add";
    }

    @GetMapping("/edit/{bid}")
    public String edit(@PathVariable("bid") String bid, Model model) {
        commonProcess("edit", model);

        RequestBoardConfig form = configInfoService.getForm(bid);
        System.out.println(form);
        model.addAttribute("requestBoardConfig", form);

        return "admin/board/edit";
    }


    /**
     * 게시판 등록/수정 처리
     *
     * @return
     */
    @PostMapping("/save")
    public String save(@Valid RequestBoardConfig config, Errors errors, Model model) {
        String mode = config.getMode();

        commonProcess(mode, model);

        configValidator.validate(config, errors);

        if (errors.hasErrors()) {
            errors.getAllErrors().stream().forEach(System.out::println);
            return "admin/board/" + mode;
        }

        configSaveService.save(config);

        return "redirect:/admin/board";
    }

    /**
     * 게시글 관리
     *
     * @return
     */
    @GetMapping("/posts")
    public String posts(Model model) {
        commonProcess("posts", model);

        return "admin/board/posts";
    }

    /**
     * 공통 처리
     *
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model) {
        String pageTitle = "게시판 목록";
        mode = StringUtils.hasText(mode) ? mode : "list";

        if (mode.equals("add")) {
            pageTitle = "게시판 등록";

        } else if (mode.equals("edit")) {
            pageTitle = "게시판 수정";

        } else if (mode.equals("posts")) {
            pageTitle = "게시글 관리";

        }

        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (mode.equals("add") || mode.equals("edit")) { // 게시판 등록 또는 수정
            addCommonScript.add("ckeditor5/ckeditor");
            addCommonScript.add("fileManager");

            addScript.add("board/form");
            model.addAttribute("authorities", Authority.getList());

        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("subMenuCode", mode);
        model.addAttribute("addCommonScript", addCommonScript);
        model.addAttribute("addScript", addScript);
    }
}
