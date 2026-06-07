package ru.mtkp.idm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import ru.mtkp.idm.model.IdmUser;
import ru.mtkp.idm.repository.IdmUserRepository;

/**
 * Конфигурация Spring Security для Smart IDM.
 * Аутентификация выполняется по локальным пользователям IDM из базы данных.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final IdmUserRepository idmUserRepository;

    /**
     * Создает конфигурацию безопасности.
     *
     * @param idmUserRepository репозиторий локальных пользователей IDM
     */
    public SecurityConfig(IdmUserRepository idmUserRepository) {
        this.idmUserRepository = idmUserRepository;
    }

    /**
     * Настройка HTTP безопасности: разрешаем публичный доступ к статическим ресурсам и странице логина,
     * все остальные запросы требуют аутентификации. Настроен form login с кастомной страницей /login.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(org.springframework.security.config.Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        return username -> idmUserRepository.findByUsername(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    /**
     * Криптографический энкодер паролей для локальных пользователей IDM.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Преобразует доменную сущность локального пользователя в Spring Security UserDetails.
     *
     * @param idmUser локальный пользователь IDM
     * @return объект для Spring Security
     */
    private UserDetails toUserDetails(IdmUser idmUser) {
        return org.springframework.security.core.userdetails.User.withUsername(idmUser.getUsername())
                .password(idmUser.getPasswordHash())
                .roles(idmUser.getRole().name())
                .disabled(!idmUser.isEnabled())
                .build();
    }
}

