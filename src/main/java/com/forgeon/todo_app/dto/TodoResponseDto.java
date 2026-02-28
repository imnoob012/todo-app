package com.forgeon.todo_app.dto;

import java.time.LocalDate;
import java.util.List;

import com.forgeon.todo_app.constant.Classification;
import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodoResponseDto {
	private Integer id;
	private String title;
	private Priority priority;
	private LocalDate dueDate;
	private Classification classification;
	private String description;
	// Todo担当者のIDと名前
	private List<Member> assignees;
}
