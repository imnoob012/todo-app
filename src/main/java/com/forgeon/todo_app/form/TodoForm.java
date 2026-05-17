package com.forgeon.todo_app.form;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.forgeon.todo_app.constant.Classification;
import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.constant.TodoStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // 登録フォームをGETメソッドでnewするため
public class TodoForm {
	
	@NotBlank(message="TODO名は必須です")
	@Size(max=50, message="TODO名は50文字以内で入力してください")
	private String title;
	
	// 担当者が未決定の状態を考慮
	private List<Integer> assigneeIds;
	
	@NotNull(message="優先度を選択してください")
	private Priority priority;
	
	@NotNull(message="期限は必須です")
	// バックエンドに送る形式を固定化。(Spring Bootに変換を任せる)
	@DateTimeFormat(pattern="yyyy-MM-dd")
	@FutureOrPresent(message = "今日以降の日付にしてください") // 過去日付の入力を禁止
	private LocalDate dueDate;
	
	@NotNull(message="分類を選択してください")
	private Classification classification;

	@NotNull(message="ステータスを選択してください")
	private TodoStatus status;
	
	@NotBlank(message="内容は必須です")
	@Size(max=2000, message="内容は2000文字以内で入力してください")
	private String description;
	
}
