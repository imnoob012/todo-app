package com.forgeon.todo_app.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.forgeon.todo_app.constant.Role;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.mapper.TodoMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoSecurity のテスト（権限判定ロジック）")
class TodoSecurityTest {

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoSecurity todoSecurity;

    /**
     * テスト用のCustomUserDetailsを作成するヘルパーメソッド
     */
    private CustomUserDetails createUser(Integer id, String username, String role) {
        return new CustomUserDetails(
            id, username, "password",
            Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * テスト用のTodoエンティティを作成するヘルパーメソッド
     */
    private Todo createTodoWithAssignees(List<Integer> assigneeIds) {
        Todo todo = new Todo();
        todo.setId(1);
        todo.setTitle("テストTODO");
        List<Member> assignees = assigneeIds.stream().map(id -> {
            Member m = new Member();
            m.setId(id);
            m.setUsername("user" + id);
            m.setRole(Role.MEMBER);
            return m;
        }).toList();
        todo.setAssignees(assignees);
        return todo;
    }

    @Nested
    @DisplayName("ADMINロールの権限判定")
    class AdminRoleTest {

        @Test
        @DisplayName("ADMINは任意のTODOを編集できること")
        void adminCanEditAnyTodo() {
            // Arrange
            CustomUserDetails admin = createUser(100, "admin", "ADMIN");
            Todo todo = createTodoWithAssignees(List.of(1, 2)); // admin自身はアサインされていない
            when(todoMapper.detail(1)).thenReturn(Optional.of(todo));

            // Act
            boolean result = todoSecurity.canEdit(1, admin);

            // Assert
            assertTrue(result, "ADMINはアサインされていなくても編集可能であるべき");
        }
    }

    @Nested
    @DisplayName("TODO_ADMINロールの権限判定")
    class TodoAdminRoleTest {

        @Test
        @DisplayName("TODO_ADMINは任意のTODOを編集できること")
        void todoAdminCanEditAnyTodo() {
            // Arrange
            CustomUserDetails todoAdmin = createUser(100, "todoAdmin", "TODO_ADMIN");
            Todo todo = createTodoWithAssignees(List.of(1, 2));
            when(todoMapper.detail(1)).thenReturn(Optional.of(todo));

            // Act
            boolean result = todoSecurity.canEdit(1, todoAdmin);

            // Assert
            assertTrue(result, "TODO_ADMINはアサインされていなくても編集可能であるべき");
        }
    }

    @Nested
    @DisplayName("MEMBERロールの権限判定")
    class MemberRoleTest {

        @Test
        @DisplayName("MEMBERは自分がアサインされているTODOを編集できること")
        void memberCanEditAssignedTodo() {
            // Arrange
            CustomUserDetails member = createUser(1, "member1", "MEMBER");
            Todo todo = createTodoWithAssignees(List.of(1, 2)); // member1はアサインされている
            when(todoMapper.detail(1)).thenReturn(Optional.of(todo));

            // Act
            boolean result = todoSecurity.canEdit(1, member);

            // Assert
            assertTrue(result, "アサインされているMEMBERは編集可能であるべき");
        }

        @Test
        @DisplayName("MEMBERは自分がアサインされていないTODOを編集できないこと")
        void memberCannotEditUnassignedTodo() {
            // Arrange
            CustomUserDetails member = createUser(99, "member99", "MEMBER");
            Todo todo = createTodoWithAssignees(List.of(1, 2)); // member99はアサインされていない
            when(todoMapper.detail(1)).thenReturn(Optional.of(todo));

            // Act
            boolean result = todoSecurity.canEdit(1, member);

            // Assert
            assertFalse(result, "アサインされていないMEMBERは編集不可であるべき");
        }
    }

    @Nested
    @DisplayName("TODOが存在しない場合")
    class TodoNotFoundTest {

        @Test
        @DisplayName("存在しないTODO IDの場合、falseが返ること")
        void shouldReturnFalseWhenTodoNotFound() {
            // Arrange
            CustomUserDetails admin = createUser(1, "admin", "ADMIN");
            when(todoMapper.detail(999)).thenReturn(Optional.empty());

            // Act
            boolean result = todoSecurity.canEdit(999, admin);

            // Assert
            assertFalse(result, "存在しないTODOに対してはfalseを返すべき");
        }
    }
}
