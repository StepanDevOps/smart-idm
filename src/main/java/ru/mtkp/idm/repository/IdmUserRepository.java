package ru.mtkp.idm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.IdmUser;

/**
 * Репозиторий локальных пользователей Smart IDM.
 */
@Repository
public interface IdmUserRepository extends JpaRepository<IdmUser, Long> {

    /**
     * Находит локального пользователя IDM по логину.
     *
     * @param username логин пользователя
     * @return найденный пользователь
     */
    Optional<IdmUser> findByUsername(String username);
}

