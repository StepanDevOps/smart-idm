package ru.mtkp.idm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Конфигурация MVC для поддержки локализации через cookie и параметр `lang`.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Создаёт резолвер локали, сохраняющий выбор пользователя в cookie.
     *
     * @return резолвер локали на основе cookie
     */
    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new CookieLocaleResolver("smart-idm-locale");
        resolver.setDefaultLocale(Locale.of("ru"));
        resolver.setCookiePath("/");
        resolver.setCookieMaxAge(Duration.ofDays(365));
        return resolver;
    }

    /**
     * Создаёт интерцептор, переключающий локаль по query-параметру `lang`.
     *
     * @return интерцептор смены локали
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Регистрирует интерцептор смены локали в MVC.
     *
     * @param registry реестр интерцепторов
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
