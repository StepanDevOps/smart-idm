package ru.mtkp.idm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.mtkp.idm.model.TargetSystem;

/**
 * Репозиторий целевых систем.
 */
@Repository
public interface TargetSystemRepository extends JpaRepository<TargetSystem, Integer> {

	/**
	 * Находит систему по имени.
	 *
	 * @param name имя системы
	 * @return найденная система
	 */
	Optional<TargetSystem> findByName(String name);
}
