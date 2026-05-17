package com.forgeon.todo_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.constant.TodoStatus;
import com.forgeon.todo_app.dto.TodoCommentResponseDto;
import com.forgeon.todo_app.dto.TodoHistoryResponseDto;
import com.forgeon.todo_app.dto.TodoResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.form.TodoSearchForm;
import com.forgeon.todo_app.mapper.TodoMapper;
import com.forgeon.todo_app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoMapper todoMapper;

	public List<TodoResponseDto> findAll(TodoSearchForm todoSearchForm) {
		return todoMapper.findAll(todoSearchForm).stream()
				.map(this::convertToDto)
				.toList();
	}

	@Transactional
	public void add(Todo entity, List<Integer> assigneeIds) {
		applyCompletedAt(entity, null);
		todoMapper.insert(entity);

		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			for (Integer assigneeId : assigneeIds) {
				todoMapper.insertAssignment(entity.getId(), assigneeId, entity.getCreatedBy());
			}
		}

		todoMapper.insertHistory(entity.getId(), "TODO", null, "TODOを登録", entity.getCreatedBy());
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			Todo created = findEntity(entity.getId());
			insertHistoryIfChanged(entity.getId(), "担当者", null, assigneeNames(created), entity.getCreatedBy());
		}
	}

	public TodoResponseDto detail(Integer id) {
		return convertToDto(findEntity(id));
	}

	public List<TodoCommentResponseDto> findComments(Integer todoId) {
		findEntity(todoId);
		return todoMapper.findComments(todoId).stream()
				.map(comment -> new TodoCommentResponseDto(
						comment.getId(),
						comment.getTodoId(),
						comment.getMemberId(),
						comment.getUsername(),
						comment.getCommentText(),
						comment.getCreatedAt()))
				.toList();
	}

	public List<TodoHistoryResponseDto> findHistories(Integer todoId) {
		findEntity(todoId);
		return todoMapper.findHistories(todoId).stream()
				.map(history -> new TodoHistoryResponseDto(
						history.getId(),
						history.getTodoId(),
						history.getFieldName(),
						history.getBeforeValue(),
						history.getAfterValue(),
						history.getChangedAt(),
						history.getChangedBy()))
				.toList();
	}

	@Transactional
	public void update(Todo entity, List<Integer> assigneeIds) {
		Todo before = findEntity(entity.getId());
		applyCompletedAt(entity, before);
		todoMapper.update(entity);

		todoMapper.logicalDeleteAllAssignees(entity.getId(), entity.getUpdatedBy());
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			for (Integer assigneeId : assigneeIds) {
				int updatedCount = todoMapper.reactivateAssignee(entity.getId(), assigneeId, entity.getUpdatedBy());

				if (updatedCount == 0) {
					todoMapper.insertAssignment(entity.getId(), assigneeId, entity.getUpdatedBy());
				}
			}
		}

		Todo after = findEntity(entity.getId());
		insertUpdateHistories(before, after, entity.getUpdatedBy());
	}

	@Transactional
	public void addComment(Integer todoId, String commentText, CustomUserDetails currentUser) {
		findEntity(todoId);
		if (commentText == null || commentText.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "コメントを入力してください");
		}
		String normalizedComment = commentText.trim();
		todoMapper.insertComment(todoId, currentUser.getId(), normalizedComment, currentUser.getUsername());
		todoMapper.insertHistory(todoId, "コメント", null, normalizedComment, currentUser.getUsername());
	}

	@Transactional
	public void delete(Integer id, UserDetails userDetails) {
		findEntity(id);
		todoMapper.insertHistory(id, "TODO", null, "TODOを削除", userDetails.getUsername());
		todoMapper.delete(id, userDetails.getUsername());
		todoMapper.logicalDeleteAllAssignees(id, userDetails.getUsername());
	}

	private Todo findEntity(Integer id) {
		Optional<Todo> entity = todoMapper.detail(id);
		return entity.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID: " + id + "は存在しません"));
	}

	private TodoResponseDto convertToDto(Todo todo) {
		return new TodoResponseDto(
				todo.getId(),
				todo.getTitle(),
				Priority.getById(todo.getPriority()),
				todo.getDueDate(),
				todo.getClassification(),
				todo.getStatus(),
				todo.getCompletedAt(),
				todo.getDescription(),
				todo.getAssignees());
	}

	private void applyCompletedAt(Todo entity, Todo before) {
		if (entity.getStatus() == TodoStatus.DONE) {
			if (before != null && before.getStatus() == TodoStatus.DONE && before.getCompletedAt() != null) {
				entity.setCompletedAt(before.getCompletedAt());
			} else {
				entity.setCompletedAt(LocalDateTime.now());
			}
		} else {
			entity.setCompletedAt(null);
		}
	}

	private void insertUpdateHistories(Todo before, Todo after, String changedBy) {
		insertHistoryIfChanged(after.getId(), "TODO名", before.getTitle(), after.getTitle(), changedBy);
		insertHistoryIfChanged(after.getId(), "優先度", priorityLabel(before), priorityLabel(after), changedBy);
		insertHistoryIfChanged(after.getId(), "期限", dateValue(before), dateValue(after), changedBy);
		insertHistoryIfChanged(after.getId(), "分類", before.getClassification().getLabel(), after.getClassification().getLabel(), changedBy);
		insertHistoryIfChanged(after.getId(), "ステータス", before.getStatus().getLabel(), after.getStatus().getLabel(), changedBy);
		insertHistoryIfChanged(after.getId(), "内容", before.getDescription(), after.getDescription(), changedBy);
		insertHistoryIfChanged(after.getId(), "担当者", assigneeNames(before), assigneeNames(after), changedBy);
	}

	private void insertHistoryIfChanged(Integer todoId, String fieldName, String beforeValue, String afterValue, String changedBy) {
		if (!Objects.equals(beforeValue, afterValue)) {
			todoMapper.insertHistory(todoId, fieldName, beforeValue, afterValue, changedBy);
		}
	}

	private String priorityLabel(Todo todo) {
		Priority priority = Priority.getById(todo.getPriority());
		return priority == null ? "" : priority.getLabel();
	}

	private String dateValue(Todo todo) {
		return todo.getDueDate() == null ? "" : todo.getDueDate().toString();
	}

	private String assigneeNames(Todo todo) {
		if (todo.getAssignees() == null || todo.getAssignees().isEmpty()) {
			return "未定";
		}
		return todo.getAssignees().stream()
				.map(Member::getUsername)
				.sorted()
				.collect(Collectors.joining(", "));
	}
}
