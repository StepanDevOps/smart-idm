package ru.mtkp.idm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.Request;

/**
 * Репозиторий заявок (работает с сущностью Request).
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
}

