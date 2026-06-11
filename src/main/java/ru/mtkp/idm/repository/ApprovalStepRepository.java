package ru.mtkp.idm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.ApprovalStep;
import ru.mtkp.idm.model.Request;

/**
 * Репозиторий этапов согласования заявок.
 */
@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Integer> {

	/**
	 * Находит все этапы согласования по заявке.
	 *
	 * @param requestId идентификатор заявки
	 * @return список этапов согласования
	 */
	List<ApprovalStep> findByRequestId(Integer requestId);

	/**
	 * Находит активные (pending) этапы согласования по заявке.
	 *
	 * @param requestId идентификатор заявки
	 * @return список активных этапов
	 */
	List<ApprovalStep> findByRequestIdAndDecision(Integer requestId, ru.mtkp.idm.model.AppDecision decision);

	/**
	 * Находит последний этап согласования по заявке.
	 *
	 * @param requestId идентификатор заявки
	 * @return опциональный последний этап
	 */
	Optional<ApprovalStep> findTopByRequestIdOrderByDecidedAtDesc(Integer requestId);
}
