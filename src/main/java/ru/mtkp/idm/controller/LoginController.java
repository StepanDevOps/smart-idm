package ru.mtkp.idm.controller;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

/**
 * Контроллер для страницы входа в систему (логин).
 * Страница публичная и не использует глобальный dashboard layout.
 */
@Controller
public class LoginController {

    private final MessageSource messageSource;

    public LoginController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Отображает страницу логина. Поддерживает вывод сообщения об ошибке авторизации и уведомления о выходе.
     * @param error параметр, присутствующий при неудачной аутентификации
     * @param logout параметр, присутствующий после успешного logout
     * @param model модель представления
     * @param locale текущая локаль
     * @return имя Thymeleaf шаблона
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model,
                            Locale locale) {
        if (error != null) {
            String err = messageSource.getMessage("login.error", null, "Invalid credentials", locale);
            model.addAttribute("errorMessage", err);
        }
        if (logout != null) {
            String msg = messageSource.getMessage("login.logout", null, "You have been logged out", locale);
            model.addAttribute("logoutMessage", msg);
        }
        model.addAttribute("pageTitle", messageSource.getMessage("login.title", null, locale));
        return "login";
    }
}

