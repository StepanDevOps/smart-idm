package ru.mtkp.idm.controller;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Locale;

/**
 * Контроллер для демонстрации базового лэйаута и фрагментов.
 * Предназначен для примера: как подключать `components/layout.html` и передавать базовые модельные атрибуты.
 */
@Controller
public class LayoutController {

    private final MessageSource messageSource;

    public LayoutController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Отображает демонстрационную страницу с общим лэйаутом.
     * @param model модель представления
     * @return имя Thymeleaf шаблона
     */
    @GetMapping({"/dashboard", "/app", "/demo", "/demo/layout-demo"})
    public String dashboard(Model model, Locale locale){
        // Делаем локализованную заглушку текущего пользователя для демонстрации фрагмента header
        model.addAttribute("currentUserLogin", messageSource.getMessage("user.login.admin", null, locale));
        model.addAttribute("currentUserFullName", messageSource.getMessage("user.fullName.admin", null, locale));
        model.addAttribute("pageTitle", messageSource.getMessage("page.defaultTitle", null, locale));
        // Возвращаем демонстрационный шаблон, который использует фрагменты в components/layout.html
        return "demo/layout-demo";
    }
}

