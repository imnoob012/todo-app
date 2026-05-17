package com.forgeon.todo_app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.forgeon.todo_app.constant.Classification;
import com.forgeon.todo_app.constant.TodoStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Todo extends AbstractAudit {
	private Integer id;
	private String title;
	// Integerで管理
	private Integer priority;
	private LocalDate dueDate;
	private Classification classification;
	private TodoStatus status;
	private LocalDateTime completedAt;
	private String description;
	
	
	// Todo担当者のIDと名前
	private List<Member> assignees;
}
