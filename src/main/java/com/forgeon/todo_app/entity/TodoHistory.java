package com.forgeon.todo_app.entity;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TodoHistory {
	private Integer id;
	private Integer todoId;
	private String fieldName;
	private String beforeValue;
	private String afterValue;
	private LocalDateTime changedAt;
	private String changedBy;
}
