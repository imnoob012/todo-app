package com.forgeon.todo_app.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
	
	ADMIN("管理者"),
	MEMBER("メンバー"),
	TODO_ADMIN("TODO管理者");
	
	private final String label;
}

// 例：

// Role.TODO_ADMIN.label()
// → "TODO管理者"

// Role.ADMIN.name()
// → "ADMIN"