package com.forgeon.todo_app.form;

import lombok.Data;

@Data
public class TodoSearchForm {
	private Integer id;
	private String title;
	private String assignee;
	private String priority;
	private String dueDate;
	private String classification;
	private String status;
	private String description;
	
	private Integer assigneeId;
	
	private String sort; // ソート対象
	private String direction; // asc or desc
}
