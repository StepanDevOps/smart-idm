package ru.mtkp.idm.controller;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mtkp.idm.dto.RoleAssignmentDTO;
import ru.mtkp.idm.model.Role;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.model.TargetSystem;
import ru.mtkp.idm.model.User;
import ru.mtkp.idm.repository.RoleAssignmentRepository;
import ru.mtkp.idm.repository.RoleRepository;
import ru.mtkp.idm.repository.TargetSystemRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.RoleAssignmentService;

/**
 * Контроллер управления назначениями ролей.
 */
@Slf4j
@Controller
@RequestMapping("/role-assignments")
@RequiredArgsConstructor
public class RoleAssignmentController {

	private final RoleAssignmentService roleAssignmentService;
	private final RoleAssignmentRepository roleAssignmentRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TargetSystemRepository targetSystemRepository;

	/**
	 * Список всех назначений ролей.
	 */
	@GetMapping
	public String listAssignments(Model model) {
		log.info("Получение списка всех назначений ролей");

		// Получаем все назначения (через сервис)
		// В реальном проекте здесь будет пагинация
		java.util.List<RoleAssignment> assignments = roleAssignmentService.getAllAssignmentsForAllUsers();

		model.addAttribute("assignments", assignments);
		model.addAttribute("pageTitle", "Назначения ролей");
		return "role-assignments";
	}

	/**
	 * Детальный просмотр назначения.
	 */
	@GetMapping("/{id}")
	public String viewAssignment(@PathVariable Integer id, Model model) {
		log.info("Получение назначения: id={}", id);

		RoleAssignment assignment = roleAssignmentService.getAssignmentById(id)
				.orElseThrow(() -> new IllegalArgumentException("Назначение не найдено: " + id));

		model.addAttribute("assignment", assignment);
		model.addAttribute("pageTitle", "Назначение роли #" + id);
		return "role-assignment-detail";
	}

	/**
	 * Форма создания назначения.
	 */
	@GetMapping("/new")
	public String newAssignmentForm(Model model) {
		log.info("Форма создания назначения");

		List<User> users = userRepository.findAll();
		List<TargetSystem> systems = targetSystemRepository.findAll();

		// По умолчанию показываем глобальные роли (system = null)
		List<Role> roles = roleRepository.findGlobalAndSystemRoles(null);

		model.addAttribute("users", users);
		model.addAttribute("systems", systems);
		model.addAttribute("roles", roles);
		model.addAttribute("assignment", new RoleAssignmentDTO());
		model.addAttribute("pageTitle", "Создать назначение роли");
		return "role-assignment-form";
	}

	/**
	 * Получение списка ролей для выбранной системы.
	 */
	@GetMapping("/roles/{systemId}")
	@ResponseBody
	public List<Role> getRolesBySystem(@PathVariable Integer systemId) {
		// systemId == -1 или null означает "Глобальные роли"
		return roleRepository.findGlobalAndSystemRoles(systemId == -1 ? null : systemId);
	}

	/**
	 * Создание назначения.
	 */
	@PostMapping
	public String createAssignment(
			@RequestParam Long userId,
			@RequestParam Integer roleId,
			@RequestParam(required = false) String reason,
			@RequestParam(required = false) String effectiveFrom,
			@RequestParam(required = false) String effectiveTo,
			RedirectAttributes redirectAttributes) {

		log.info("Создание назначения: userId={}, roleId={}, reason={}, effectiveFrom={}, effectiveTo={}",
				userId, roleId, reason, effectiveFrom, effectiveTo);

		LocalDate fromDate = null;
		if (effectiveFrom != null && !effectiveFrom.isEmpty()) {
			fromDate = LocalDate.parse(effectiveFrom);
		}

		LocalDate toDate = null;
		if (effectiveTo != null && !effectiveTo.isEmpty()) {
			toDate = LocalDate.parse(effectiveTo);
		}

		try {
			RoleAssignment assignment = roleAssignmentService.assignRoleToUser(
					userId, roleId, reason, fromDate, toDate);

			redirectAttributes.addFlashAttribute("successMessage",
					"Роль успешно назначена пользователю (assignmentId=" + assignment.getId() + ")");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/role-assignments/new";
		}

		return "redirect:/role-assignments";
	}

	/**
	 * Обновление назначения.
	 */
	@PutMapping("/{id}")
	public String updateAssignment(
			@PathVariable Integer id,
			@RequestParam(required = false) String reason,
			@RequestParam(required = false) String effectiveFrom,
			@RequestParam(required = false) String effectiveTo,
			RedirectAttributes redirectAttributes) {

		log.info("Обновление назначения: id={}, reason={}, effectiveFrom={}, effectiveTo={}",
				id, reason, effectiveFrom, effectiveTo);

		try {
			RoleAssignment assignment = roleAssignmentService.getAssignmentById(id)
					.orElseThrow(() -> new IllegalArgumentException("Назначение не найдено: " + id));

			LocalDate fromDate = assignment.getEffectiveFrom();
			LocalDate toDate = assignment.getEffectiveTo();

			if (effectiveFrom != null && !effectiveFrom.isEmpty()) {
				fromDate = LocalDate.parse(effectiveFrom);
			}
			if (effectiveTo != null && !effectiveTo.isEmpty()) {
				toDate = LocalDate.parse(effectiveTo);
			}

			// Валидация: даты
			if (fromDate != null && toDate != null && !toDate.isAfter(fromDate)) {
				throw new IllegalArgumentException("Дата окончания (effectiveTo) должна быть позже даты начала (effectiveFrom)");
			}

			// Обновляем поля
			if (reason != null) {
				assignment.setAssignmentReason(reason);
			}
			assignment.setEffectiveFrom(fromDate);
			assignment.setEffectiveTo(toDate);

			roleAssignmentRepository.save(assignment);

			redirectAttributes.addFlashAttribute("successMessage", "Назначение успешно обновлено");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/role-assignments/" + id;
		}

		return "redirect:/role-assignments/" + id;
	}

	/**
	 * Отзыв назначения.
	 */
	@DeleteMapping("/{id}")
	public String revokeAssignment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		log.info("Отзыв назначения: id={}", id);

		try {
			boolean revoked = roleAssignmentService.revokeRoleAssignment(id);
			if (revoked) {
				redirectAttributes.addFlashAttribute("successMessage", "Назначение успешно отозвано");
			} else {
				redirectAttributes.addFlashAttribute("errorMessage", "Назначение уже истекло или не найдено");
			}
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/role-assignments";
	}
}
