package com.forgeon.todo_app.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodoCommentResponseDto {
	private Integer id;
	private Integer todoId;
	private Integer memberId;
	private String username;
	private String commentText;
	private LocalDateTime createdAt;
}
