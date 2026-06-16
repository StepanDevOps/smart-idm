package ru.mtkp.idm.controller;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.mtkp.idm.dto.RoleAssignmentDTO;
import ru.mtkp.idm.model.RoleAssignment;
import ru.mtkp.idm.model.User;
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
	private final UserRepository userRepository;

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

		java.util.List<User> users = userRepository.findAll();
		model.addAttribute("users", users);
		model.addAttribute("assignment", new RoleAssignmentDTO());
		model.addAttribute("pageTitle", "Создать назначение роли");
		return "role-assignment-form";
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

			// Обновляем поля
			if (reason != null) {
				assignment.setAssignmentReason(reason);
			}
			if (effectiveFrom != null && !effectiveFrom.isEmpty()) {
				assignment.setEffectiveFrom(LocalDate.parse(effectiveFrom));
			}
			if (effectiveTo != null && !effectiveTo.isEmpty()) {
				assignment.setEffectiveTo(LocalDate.parse(effectiveTo));
			}

			roleAssignmentService.assignRoleToUser(
					assignment.getUser().getId(),
					assignment.getRole().getId(),
					reason,
					assignment.getEffectiveFrom(),
					assignment.getEffectiveTo()
			);

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
