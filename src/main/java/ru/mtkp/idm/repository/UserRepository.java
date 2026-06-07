package ru.mtkp.idm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.User;

/**
 * Репозиторий пользователей IDM.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Находит пользователя по имени учетной записи.
	 *
	 * @param username имя учетной записи
	 * @return найденный пользователь
	 */
	Optional<User> findByUsername(String username);
}

