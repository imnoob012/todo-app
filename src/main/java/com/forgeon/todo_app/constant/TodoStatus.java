package com.forgeon.todo_app.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TodoStatus {
	TODO("未着手"),
	IN_PROGRESS("進行中"),
	DONE("完了");

	private final String label;
}
