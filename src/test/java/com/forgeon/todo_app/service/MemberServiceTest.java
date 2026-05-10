package com.forgeon.todo_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.forgeon.todo_app.constant.Role;
import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.mapper.MemberMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService のテスト")
class MemberServiceTest {

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    /**
     * テスト用のMemberエンティティを作成するヘルパーメソッド
     */
    private Member createTestMember(Integer id, String username, String email, Role role) {
        Member member = new Member();
        member.setId(id);
        member.setUsername(username);
        member.setEmail(email);
        member.setPassword("hashedPassword");
        member.setRole(role);
        member.setRemarks("テストメンバー");
        return member;
    }

    @Nested
    @DisplayName("findAll メソッド")
    class FindAllTest {

        @Test
        @DisplayName("メンバー一覧が正しく取得できること")
        void shouldReturnMemberList() {
            // Arrange（準備）
            MemberSearchForm searchForm = new MemberSearchForm();
            Member member = createTestMember(1, "testUser", "test@example.com", Role.MEMBER);
            when(memberMapper.findAll(searchForm)).thenReturn(Collections.singletonList(member));

            // Act（実行）
            var result = memberService.findAll(searchForm);

            // Assert（検証）
            assertEquals(1, result.size());
            MemberResponseDto dto = result.get(0);
            assertEquals(1, dto.getId());
            assertEquals("testUser", dto.getUsername());
            assertEquals("test@example.com", dto.getEmail());
            assertEquals(Role.MEMBER, dto.getRole());
            verify(memberMapper, times(1)).findAll(searchForm);
        }

        @Test
        @DisplayName("検索結果が0件の場合、空のリストが返ること")
        void shouldReturnEmptyListWhenNoMembers() {
            // Arrange
            MemberSearchForm searchForm = new MemberSearchForm();
            when(memberMapper.findAll(searchForm)).thenReturn(Collections.emptyList());

            // Act
            var result = memberService.findAll(searchForm);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("detail メソッド")
    class DetailTest {

        @Test
        @DisplayName("存在するIDでメンバー詳細が取得できること")
        void shouldReturnMemberDetail() {
            // Arrange
            Member member = createTestMember(1, "testUser", "test@example.com", Role.ADMIN);
            when(memberMapper.detail(1)).thenReturn(Optional.of(member));

            // Act
            MemberResponseDto result = memberService.detail(1);

            // Assert
            assertEquals(1, result.getId());
            assertEquals("testUser", result.getUsername());
            assertEquals(Role.ADMIN, result.getRole());
        }

        @Test
        @DisplayName("存在しないIDで404エラーが発生すること")
        void shouldThrowNotFoundWhenMemberNotExists() {
            // Arrange
            when(memberMapper.detail(999)).thenReturn(Optional.empty());

            // Act & Assert
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> memberService.detail(999)
            );
            assertEquals(404, exception.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("add メソッド")
    class AddTest {

        @Test
        @DisplayName("メンバーが正常に追加されること")
        void shouldAddMember() {
            // Arrange
            Member member = createTestMember(null, "newUser", "new@example.com", Role.MEMBER);

            // Act
            memberService.add(member);

            // Assert（Mapperのaddメソッドが1回呼ばれたことを検証）
            verify(memberMapper, times(1)).add(member);
        }
    }

    @Nested
    @DisplayName("update メソッド")
    class UpdateTest {

        @Test
        @DisplayName("メンバー情報が正常に更新されること")
        void shouldUpdateMember() {
            // Arrange
            Member member = createTestMember(1, "updatedUser", "updated@example.com", Role.TODO_ADMIN);

            // Act
            memberService.update(member);

            // Assert
            verify(memberMapper, times(1)).update(member);
        }
    }

    @Nested
    @DisplayName("delete メソッド")
    class DeleteTest {

        @Test
        @DisplayName("メンバーが正常に削除されること")
        void shouldDeleteMember() {
            // Act
            memberService.delete(1, "adminUser");

            // Assert
            verify(memberMapper, times(1)).delete(1, "adminUser");
        }
    }
}
