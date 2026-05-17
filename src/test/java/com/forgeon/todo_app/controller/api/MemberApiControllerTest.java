package com.forgeon.todo_app.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.forgeon.todo_app.constant.Role;
import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.mapper.DashboardMapper;
import com.forgeon.todo_app.mapper.MemberMapper;
import com.forgeon.todo_app.mapper.TodoMapper;
import com.forgeon.todo_app.security.SecurityConfig;
import com.forgeon.todo_app.service.MemberService;

@WebMvcTest(controllers = MemberApiController.class,
		excludeAutoConfiguration = MybatisAutoConfiguration.class,
		excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Mapper.class))
@Import(SecurityConfig.class)
class MemberApiControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	MemberService memberService;

	@MockitoBean
	DashboardMapper dashboardMapper;

	@MockitoBean
	MemberMapper memberMapper;

	@MockitoBean
	TodoMapper todoMapper;

	@Test
	@WithMockUser
	void membersApiDoesNotExposePassword() throws Exception {
		given(memberService.findAll(any(MemberSearchForm.class)))
				.willReturn(List.of(new MemberResponseDto(1, "admin", "admin@example.com", Role.ADMIN, "管理者")));

		mockMvc.perform(get("/api/members").with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username").value("admin"))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}
}
