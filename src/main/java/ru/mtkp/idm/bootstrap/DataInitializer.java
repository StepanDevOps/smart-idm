package ru.mtkp.idm.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ru.mtkp.idm.model.IdmUser;
import ru.mtkp.idm.model.IdmUserRole;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.IdmUserRepository;
import ru.mtkp.idm.repository.UserRepository;

import java.time.LocalDate;

/**
 * Инициализатор данных для разработки: создаёт доменного администратора и локальную учетную запись IDM.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IdmUserRepository idmUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(@org.springframework.lang.NonNull UserRepository userRepository,
                           @org.springframework.lang.NonNull IdmUserRepository idmUserRepository,
                           @org.springframework.lang.NonNull PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.idmUserRepository = idmUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByLogin("admin").isEmpty()) {
            var admin = User.builder()
                    .login("admin")
                    .firstName("System")
                    .lastName("Administrator")
                    .emailWork("admin@example.com")
                    .hireDate(LocalDate.now())
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println("Создан доменный пользователь admin.");
        }

        if (idmUserRepository.findByUsername("admin").isEmpty()) {
            var idmAdmin = IdmUser.builder()
                    .username("admin")
                    .fullName("System Administrator")
                    .passwordHash(passwordEncoder.encode("admin"))
                    .role(IdmUserRole.ADMIN)
                    .enabled(true)
                    .build();
            idmUserRepository.save(idmAdmin);
            System.out.println("Создан локальный пользователь Smart IDM admin.");
        }
    }
}

