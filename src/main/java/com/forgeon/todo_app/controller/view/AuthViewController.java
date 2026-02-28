package com.forgeon.todo_app.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {
	
	@GetMapping("/login")
	String login() {
		return "login";
	}
	
	@GetMapping("/menu")
	String menu() {
		return "menu";
	}
}