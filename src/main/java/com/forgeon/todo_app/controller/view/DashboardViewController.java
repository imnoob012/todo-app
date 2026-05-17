package com.forgeon.todo_app.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.forgeon.todo_app.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardViewController {

	private final DashboardService dashboardService;

	@GetMapping("/dashboard")
	String dashboard(Model model) {
		model.addAttribute("dashboard", dashboardService.dashboard());
		return "dashboard";
	}
}
