package com.forgeon.todo_app.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.form.TodoSearchForm;

@Mapper
public interface TodoMapper {

	List<Todo> findAll(TodoSearchForm todoSearchForm);

	void insert(Todo entity);
	
	void insertAssignment(@Param("todoId") Integer todoId,
			              @Param("memberId") Integer memberId,
						  @Param("createdBy") String createdBy);

	Optional<Todo> detail(Integer id);

	void update(Todo entity);

	void logicalDeleteAllAssignees(@Param("todoId") Integer todoId,
								   @Param("updatedBy") String updatedBy);

	int reactivateAssignee(@Param("todoId") Integer todoId,
						   @Param("assigneeId") Integer assigneeId,
						   @Param("updatedBy") String updatedBy);

	void delete(@Param("id")Integer id, @Param("currentUsername")String currentUsername);
	
	

}
