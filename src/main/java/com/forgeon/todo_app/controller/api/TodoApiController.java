package com.forgeon.todo_app.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forgeon.todo_app.dto.TodoResponseDto;
import com.forgeon.todo_app.form.TodoSearchForm;
import com.forgeon.todo_app.service.TodoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TodoApiController {
	private final TodoService todoService;
	
	@GetMapping("/todos")
	List<TodoResponseDto> search(TodoSearchForm todoSearchForm) {
		return todoService.findAll(todoSearchForm);
	}
	
}