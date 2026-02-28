package com.forgeon.todo_app.entity;

import com.forgeon.todo_app.constant.Role;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true) // 親クラスから継承した項目を含めて、データが完全一致するかを判定するため
@NoArgsConstructor
public class Member extends AbstractAudit {
	private Integer id;
	private String username;
	private String email;
	private String password;
	private Role role;
	private String remarks;
}