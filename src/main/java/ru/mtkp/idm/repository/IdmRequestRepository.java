package ru.mtkp.idm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.IdmRequest;

/**
 * Репозиторий заявок IDM.
 */
@Repository
public interface IdmRequestRepository extends JpaRepository<IdmRequest, Long> {
}

