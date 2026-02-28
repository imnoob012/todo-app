package com.forgeon.todo_app.controller.view;


import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.dto.TodoResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.form.TodoForm;
import com.forgeon.todo_app.form.TodoSearchForm;
import com.forgeon.todo_app.security.CustomUserDetails;
import com.forgeon.todo_app.service.MemberService;
import com.forgeon.todo_app.service.TodoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoViewController {
	
	private final TodoService todoService;
	private final MemberService memberService;
	
	@GetMapping("/todos")
	String todos(Model model, TodoSearchForm todoSearchForm) {
		List<TodoResponseDto> todoList = todoService.findAll(todoSearchForm);
		model.addAttribute("todos", todoList);
		
		if (todoSearchForm.getAssigneeId() != null) {
			MemberResponseDto assignee = memberService.detail(todoSearchForm.getAssigneeId());
			model.addAttribute("assignee", assignee);
		}
		return "todos";
	}
	
	@GetMapping("/todos/add")
	String add(Model model, TodoForm todoForm) {
		
		model.addAttribute("todo", todoForm);
		model.addAttribute("members", memberService.findAll(new MemberSearchForm()));
		
		return "todo-form";
	}
	
	@PostMapping("/todos/add")
	String add(@Validated TodoForm form, BindingResult result,
			   Model model, @AuthenticationPrincipal UserDetails userDetails) {
		
		if (result.hasErrors()) {
			// フォームのバリデーション外であるmembersは手動で設定
			model.addAttribute("members", memberService.findAll(new MemberSearchForm()));
			return "todo-form";
		}
		
		Todo entity = convertToEntity(form, userDetails);
		todoService.add(entity, form.getAssigneeIds());
		
		return "redirect:/todos";
	}
	
	@GetMapping("/todos/{id}")
	String detail(@PathVariable("id") Integer id, Model model) {
		model.addAttribute("todo", todoService.detail(id));
		return "todo-detail";
	}
	
	@GetMapping("/todos/edit/{id}")
	@PreAuthorize("@todoSecurity.canEdit(#id, principal)")
	String edit(@PathVariable("id") Integer id, Model model,
				@AuthenticationPrincipal CustomUserDetails currentUser) {
		
		TodoResponseDto dto = todoService.detail(id);
	
		// formにtodoidがないので
		model.addAttribute("id", id);

		List<Integer> assigneeIds = new ArrayList<>();
		
		for (Member assignee : dto.getAssignees()) {
			assigneeIds.add(assignee.getId());
		}
		
		model.addAttribute("todoForm", new TodoForm(dto.getTitle(),
												assigneeIds,
												dto.getPriority(),
												dto.getDueDate(),
												dto.getClassification(),
												dto.getDescription()
												));
		// 担当者欄に全メンバー表示する為
		model.addAttribute("members", memberService.findAll(new MemberSearchForm()));
		
		return "todo-edit";
	}
	
	@PostMapping("/todos/update/{id}")
	@PreAuthorize("@todoSecurity.canEdit(#id, principal)")
	String update(@PathVariable("id") Integer id, Model model,
				  @Validated TodoForm todoForm, BindingResult result,
				  @AuthenticationPrincipal CustomUserDetails currentUser) {
		if (result.hasErrors()) {
			model.addAttribute("id", id);
			model.addAttribute("members", memberService.findAll(new MemberSearchForm()));
			return "todo-edit";
		}
		
		Todo entity = convertToEntity(todoForm, currentUser);
		
		// idはformにないため
		entity.setId(id);
		
		todoService.update(entity, todoForm.getAssigneeIds());
		return "redirect:/todos/" + id;
	}
	
	@GetMapping("/todos/delete/{id}")
	@PreAuthorize("@todoSecurity.canEdit(#id, principal)")
	String deleteConfirm(@PathVariable("id") Integer id, Model model,
						 @AuthenticationPrincipal CustomUserDetails currentUser) {
		model.addAttribute(id);
		return "todo-delete-confirm";
	}
	
	@PostMapping("/todos/delete/{id}")
	@PreAuthorize("@todoSecurity.canEdit(#id, principal)")
	String deleteSuccess(@PathVariable("id") Integer id, @AuthenticationPrincipal CustomUserDetails currentUser) {
		todoService.delete(id, currentUser);
		return "todo-delete-success";
	}
	
	// FORMからENTITYへ変換
	private Todo convertToEntity(TodoForm form, UserDetails userDetails) {
		
		Todo entity = new Todo();
		
		entity.setTitle(form.getTitle());
		entity.setPriority(form.getPriority().getId());
		entity.setDueDate(form.getDueDate());
		entity.setClassification(form.getClassification());
		entity.setDescription(form.getDescription());
		
		// 監査情報（名前）
		entity.setCreatedBy(userDetails.getUsername());
		// 削除情報をセットする際にもこちらの値を使用するものとする
		entity.setUpdatedBy(userDetails.getUsername());
		
		return entity;
	}
}