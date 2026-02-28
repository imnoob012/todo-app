package com.forgeon.todo_app.form;

import lombok.Data;

@Data
public class MemberSearchForm {
	private Integer id;
	private String username;
	private String email;
	private String password;
	private String role;
	private String remarks;
	
	private String sort; // ソート対象
	private String direction; // asc or desc
}