package com.forgeon.todo_app.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT) // EnumをJson形式に変換（シリアライズ）される際の出力形式できるようにする
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

// JSON出力: {"label": "管理者"}