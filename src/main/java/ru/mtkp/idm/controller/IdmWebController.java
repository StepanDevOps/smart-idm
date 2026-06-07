package ru.mtkp.idm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.mtkp.idm.repository.IdmRequestRepository;
import ru.mtkp.idm.service.WorkflowEngineService;

/**
 * Веб-контроллер главной страницы IDM.
 */
@Controller
@RequiredArgsConstructor
public class IdmWebController {

	private final IdmRequestRepository idmRequestRepository;
	private final WorkflowEngineService workflowEngineService;

	/**
	 * Открывает главную страницу со списком заявок.
	 *
	 * @param model модель представления
	 * @return имя шаблона
	 */
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("requests", idmRequestRepository.findAll());
		return "index";
	}

	/**
	 * Создает симуляцию IDM-события из веб-формы.
	 *
	 * @param eventType тип события
	 * @param userId идентификатор пользователя
	 * @param details детали события
	 * @return перенаправление на главную страницу
	 */
	@PostMapping("/events/simulate")
	public String simulateEvent(@RequestParam String eventType,
								@RequestParam Long userId,
								@RequestParam String details) {
		workflowEngineService.handleIdmEvent(eventType, userId, details);
		return "redirect:/";
	}
}

