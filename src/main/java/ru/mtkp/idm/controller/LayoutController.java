package ru.mtkp.idm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для демонстрации базового лэйаута и фрагментов.
 * Предназначен для примера: как подключать `components/layout.html` и передавать базовые модельные атрибуты.
 */
@Controller
public class LayoutController {

    /**
     * Отображает демонстрационную страницу с общим лэйаутом.
     * @param model модель представления
     * @return имя Thymeleaf шаблона
     */
    @GetMapping({"/dashboard", "/app", "/demo", "/demo/layout-demo"})
    public String dashboard(Model model){
        // Делаем лёгкую заглушку текущего пользователя для демонстрации фрагмента header
        model.addAttribute("currentUserLogin", "admin");
        model.addAttribute("currentUserFullName", "Иван Иванов");
        model.addAttribute("pageTitle", "Панель управления");
        // Возвращаем демонстрационный шаблон который использует фрагменты в components/layout.html
        return "demo/layout-demo";
    }
}

