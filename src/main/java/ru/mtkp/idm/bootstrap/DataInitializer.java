package ru.mtkp.idm.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ru.mtkp.idm.model.User;
import ru.mtkp.idm.model.UserStatus;
import ru.mtkp.idm.repository.UserRepository;

import java.time.LocalDate;

/**
 * Инициализатор данных для разработки: создаёт первого администратора (login=admin) если его нет.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        var existing = userRepository.findByLogin("admin");
        if (existing.isEmpty()) {
            var admin = User.builder()
                    .login("admin")
                    .firstName("System")
                    .lastName("Administrator")
                    .emailWork("admin@example.com")
                    .hireDate(LocalDate.now())
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            System.out.println("Created default admin user: admin/admin (in-memory auth) and domain user record");
        }
    }
}

