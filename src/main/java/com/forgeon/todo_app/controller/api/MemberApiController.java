package com.forgeon.todo_app.controller.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberApiController {
	private final MemberService memberService;
	
	@GetMapping("/members")
	List<MemberResponseDto> search(MemberSearchForm memberSearchForm) {
		return memberService.findAll(memberSearchForm);
	}
}