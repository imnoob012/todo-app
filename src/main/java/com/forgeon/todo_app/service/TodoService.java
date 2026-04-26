package com.forgeon.todo_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.dto.TodoResponseDto;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.form.TodoSearchForm;
import com.forgeon.todo_app.mapper.TodoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	
	private final TodoMapper todoMapper;

	public List<TodoResponseDto> findAll(TodoSearchForm todoSearchForm) {
		
		List<Todo> entity = todoMapper.findAll(todoSearchForm);
		
		// Priorityはenumに変換
		return entity.stream()
					 .map(todo -> new TodoResponseDto(
							 todo.getId(),
							 todo.getTitle(),
							 Priority.getById(todo.getPriority()),
							 todo.getDueDate(),
							 todo.getClassification(),
							 todo.getDescription(),
							 todo.getAssignees()
					 ))
					 .toList();
	}
	
	@Transactional
	public void add(Todo entity, List<Integer> assigneeIds) {
		// 成功後、エンティティにTODOIDが付与されます
		todoMapper.insert(entity);
		
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			
			for (Integer assigneeId : assigneeIds) {
				todoMapper.insertAssignment(entity.getId(), assigneeId, entity.getCreatedBy());
			}
			
		}
	}

	public TodoResponseDto detail(Integer id) {
		Optional<Todo> entity = todoMapper.detail(id);
		return entity.map(e -> new TodoResponseDto(e.getId(),
												   e.getTitle(),
												   Priority.getById(e.getPriority()),
												   e.getDueDate(),
												   e.getClassification(),
												   e.getDescription(),
												   e.getAssignees()
												   ))
					 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID: " + id + "は存在しません"));
	}
	
	@Transactional
	public void update(Todo entity, List<Integer> assigneeIds) {
		todoMapper.update(entity);
		// 担当者を全て論理削除
		todoMapper.logicalDeleteAllAssignees(entity.getId(), entity.getUpdatedBy());
		// 必要な担当者の論理削除の有効化 & 新規の担当者を登録
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			for (Integer assigneeId : assigneeIds) {
				int updatedCount = todoMapper.reactivateAssignee(entity.getId(), assigneeId, entity.getUpdatedBy());			
				
				if (updatedCount == 0) {
					todoMapper.insertAssignment(entity.getId(), assigneeId, entity.getCreatedBy());
				}
			}
		}
	}
	
	@Transactional
	public void delete(Integer id, UserDetails userDetails) {
		todoMapper.delete(id, userDetails.getUsername());
		todoMapper.logicalDeleteAllAssignees(id, userDetails.getUsername());
	}
}
