package com.forgeon.todo_app.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodoHistoryResponseDto {
	private Integer id;
	private Integer todoId;
	private String fieldName;
	private String beforeValue;
	private String afterValue;
	private LocalDateTime changedAt;
	private String changedBy;
}
