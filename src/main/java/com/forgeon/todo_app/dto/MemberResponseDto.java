package com.forgeon.todo_app.dto;

import com.forgeon.todo_app.constant.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberResponseDto {
	private Integer id;
	private String username;
	private String email;
	private Role role;
	private String remarks;
}
