package com.forgeon.todo_app.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TodoComment extends AbstractAudit {
	private Integer id;
	private Integer todoId;
	private Integer memberId;
	private String username;
	private String commentText;
}
