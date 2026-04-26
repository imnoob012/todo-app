package com.forgeon.todo_app.security;

import java.util.List;
import org.springframework.stereotype.Component;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;

// ログインしているユーザーがアサインしているTodoの編集権限があるかないかの処理
// ADMINまたはTODO_ADMINは任意のTODOを編集可能
@Component("todoSecurity")
@RequiredArgsConstructor
public class TodoSecurity {

 private final TodoMapper todoMapper;

    public boolean canEdit(Integer todoId, CustomUserDetails currentUser) {
        Todo todo = todoMapper.detail(todoId)
                     .orElse(null);
        if (todo == null) return false;
        
        // ADMINまたはTODO_ADMINかどうか
        List<String> adminRoles = List.of("ADMIN", "TODO_ADMIN");
        boolean isAdmin = currentUser.getAuthorities().stream()
                      .anyMatch(cu -> adminRoles.contains(cu.getAuthority()));
        if (isAdmin) return true;

        // アサインされている複数の担当者IDとログインユーザーIDの比較
        return todo.getAssignees().stream()
                      .anyMatch(assignee -> assignee.getId().equals(currentUser.getId()));
    }
}