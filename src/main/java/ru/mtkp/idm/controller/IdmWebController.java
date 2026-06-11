package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.mtkp.idm.repository.RequestRepository;
import ru.mtkp.idm.repository.UserRepository;
import ru.mtkp.idm.service.RequestService;
import ru.mtkp.idm.service.WorkflowEngineService;

/**
 * Веб-контроллер главной страницы IDM и симуляции событий.
 */
@Controller
@RequiredArgsConstructor
public class IdmWebController {

	private final RequestRepository requestRepository;
	private final UserRepository userRepository;
	private final WorkflowEngineService workflowEngineService;
	private final RequestService requestService;

	/**
	 * Открывает главную страницу со списком заявок.
	 */
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("requests", requestRepository.findAll());
		return "index";
	}

	/**
	 * Создаёт симуляцию IDM-события из веб-формы.
	 */
	@PostMapping("/events/simulate")
	public String simulateEvent(@RequestParam String eventType,
								@RequestParam Long userId,
								@RequestParam String details) {
		workflowEngineService.handleIdmEvent(eventType, userId, details);
		return "redirect:/";
	}

	/**
	 * Страница согласования заявки (GET для отображения формы).
	 */
	@GetMapping("/requests/{id}/approve")
	public String approveRequestPage(@PathVariable Integer id, Model model) {
		model.addAttribute("requestId", id);
		model.addAttribute("request", requestRepository.findById(id).orElse(null));
		return "approve-request";
	}

	/**
	 * Обработка решения по заявке (POST).
	 * Вызывает подпрограмму resolveRequestStep (рис. 8.3).
	 */
	@PostMapping("/requests/{id}/approve")
	public String approveRequestPost(@PathVariable Integer id,
									 @RequestParam Long approverId,
									 @RequestParam(defaultValue = "true") boolean approved) {
		requestService.resolveRequestStep(id, approverId, approved);
		return "redirect:/requests";
	}
}