package com.forgeon.todo_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
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
import org.springframework.web.server.ResponseStatusException;

import com.forgeon.todo_app.constant.Classification;
import com.forgeon.todo_app.constant.Priority;
import com.forgeon.todo_app.dto.TodoResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.entity.Todo;
import com.forgeon.todo_app.form.TodoSearchForm;
import com.forgeon.todo_app.mapper.TodoMapper;
import com.forgeon.todo_app.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService のテスト")
class TodoServiceTest {

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoService todoService;

    /**
     * テスト用のTodoエンティティを作成するヘルパーメソッド
     */
    private Todo createTestTodo(Integer id, String title, Integer priority) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTitle(title);
        todo.setPriority(priority);
        todo.setDueDate(LocalDate.of(2026, 12, 31));
        todo.setClassification(Classification.FEATURE);
        todo.setDescription("テスト用TODO");
        todo.setAssignees(Collections.emptyList());
        todo.setCreatedBy("testUser");
        todo.setUpdatedBy("testUser");
        return todo;
    }

    /**
     * テスト用のCustomUserDetailsを作成するヘルパーメソッド
     */
    private CustomUserDetails createTestUser(Integer id, String username, String role) {
        return new CustomUserDetails(
            id, username, "password",
            Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }

    @Nested
    @DisplayName("findAll メソッド")
    class FindAllTest {

        @Test
        @DisplayName("TODO一覧が正しく取得でき、PriorityがEnumに変換されること")
        void shouldReturnTodoListWithPriorityEnum() {
            // Arrange
            TodoSearchForm searchForm = new TodoSearchForm();
            Todo todo = createTestTodo(1, "テストタスク", 1);
            when(todoMapper.findAll(searchForm)).thenReturn(Collections.singletonList(todo));

            // Act
            List<TodoResponseDto> result = todoService.findAll(searchForm);

            // Assert
            assertEquals(1, result.size());
            TodoResponseDto dto = result.get(0);
            assertEquals("テストタスク", dto.getTitle());
            assertEquals(Priority.HIGH, dto.getPriority()); // Integer 1 → Priority.HIGH に変換されていること
        }

        @Test
        @DisplayName("検索結果が0件の場合、空のリストが返ること")
        void shouldReturnEmptyList() {
            // Arrange
            TodoSearchForm searchForm = new TodoSearchForm();
            when(todoMapper.findAll(searchForm)).thenReturn(Collections.emptyList());

            // Act
            var result = todoService.findAll(searchForm);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("detail メソッド")
    class DetailTest {

        @Test
        @DisplayName("存在するIDでTODO詳細が取得できること")
        void shouldReturnTodoDetail() {
            // Arrange
            Todo todo = createTestTodo(1, "詳細テスト", 2);
            when(todoMapper.detail(1)).thenReturn(Optional.of(todo));

            // Act
            TodoResponseDto result = todoService.detail(1);

            // Assert
            assertEquals("詳細テスト", result.getTitle());
            assertEquals(Priority.MEDIUM, result.getPriority());
        }

        @Test
        @DisplayName("存在しないIDで404エラーが発生すること")
        void shouldThrowNotFoundWhenTodoNotExists() {
            // Arrange
            when(todoMapper.detail(999)).thenReturn(Optional.empty());

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> todoService.detail(999)
            );
            assertEquals(404, exception.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("add メソッド")
    class AddTest {

        @Test
        @DisplayName("担当者ありでTODOが正常に追加されること")
        void shouldAddTodoWithAssignees() {
            // Arrange
            Todo todo = createTestTodo(null, "新規タスク", 1);
            // insert後にMyBatisがIDを採番する想定
            doAnswer(invocation -> {
                Todo arg = invocation.getArgument(0);
                arg.setId(10); // 自動採番されたIDを模擬
                return null;
            }).when(todoMapper).insert(todo);

            List<Integer> assigneeIds = Arrays.asList(1, 2, 3);

            // Act
            todoService.add(todo, assigneeIds);

            // Assert
            verify(todoMapper, times(1)).insert(todo);
            verify(todoMapper, times(3)).insertAssignment(anyInt(), anyInt(), anyString());
        }

        @Test
        @DisplayName("担当者なしでTODOが追加されること")
        void shouldAddTodoWithoutAssignees() {
            // Arrange
            Todo todo = createTestTodo(null, "担当者なしタスク", 3);

            // Act
            todoService.add(todo, null);

            // Assert
            verify(todoMapper, times(1)).insert(todo);
            verify(todoMapper, never()).insertAssignment(anyInt(), anyInt(), anyString());
        }

        @Test
        @DisplayName("空の担当者リストでTODOが追加されること")
        void shouldAddTodoWithEmptyAssignees() {
            // Arrange
            Todo todo = createTestTodo(null, "空リストタスク", 2);

            // Act
            todoService.add(todo, Collections.emptyList());

            // Assert
            verify(todoMapper, times(1)).insert(todo);
            verify(todoMapper, never()).insertAssignment(anyInt(), anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("update メソッド")
    class UpdateTest {

        @Test
        @DisplayName("担当者の再割り当てを含むTODO更新が正しく動作すること")
        void shouldUpdateTodoWithReassignment() {
            // Arrange
            Todo todo = createTestTodo(1, "更新タスク", 1);
            List<Integer> assigneeIds = Arrays.asList(2, 3);
            // assigneeId=2は既存（reactivate成功）、assigneeId=3は新規（reactivate失敗→insert）
            when(todoMapper.reactivateAssignee(1, 2, "testUser")).thenReturn(1);
            when(todoMapper.reactivateAssignee(1, 3, "testUser")).thenReturn(0);

            // Act
            todoService.update(todo, assigneeIds);

            // Assert
            verify(todoMapper).update(todo);
            verify(todoMapper).logicalDeleteAllAssignees(1, "testUser");
            verify(todoMapper).reactivateAssignee(1, 2, "testUser");
            verify(todoMapper).reactivateAssignee(1, 3, "testUser");
            // assigneeId=3のみ新規insert
            verify(todoMapper, times(1)).insertAssignment(eq(1), eq(3), anyString());
        }
    }

    @Nested
    @DisplayName("delete メソッド")
    class DeleteTest {

        @Test
        @DisplayName("TODOと担当者アサインが同時に論理削除されること")
        void shouldDeleteTodoAndAssignments() {
            // Arrange
            CustomUserDetails user = createTestUser(1, "adminUser", "ADMIN");

            // Act
            todoService.delete(1, user);

            // Assert
            verify(todoMapper).delete(1, "adminUser");
            verify(todoMapper).logicalDeleteAllAssignees(1, "adminUser");
        }
    }
}
