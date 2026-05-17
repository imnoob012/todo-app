package com.forgeon.todo_app.controller.view;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.forgeon.todo_app.security.SecurityConfig;
import com.forgeon.todo_app.mapper.DashboardMapper;
import com.forgeon.todo_app.mapper.MemberMapper;
import com.forgeon.todo_app.mapper.TodoMapper;
import com.forgeon.todo_app.service.MemberService;

@WebMvcTest(controllers = MemberViewController.class,
		excludeAutoConfiguration = MybatisAutoConfiguration.class,
		excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = Mapper.class))
@Import(SecurityConfig.class)
class MemberViewControllerSecurityTest {

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
	@WithMockUser(authorities = "MEMBER")
	void memberCannotOpenMemberCreateForm() throws Exception {
		mockMvc.perform(get("/members/add"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "MEMBER")
	void memberCannotCreateMemberByDirectPost() throws Exception {
		mockMvc.perform(post("/members/add")
				.with(csrf())
				.param("username", "new-user")
				.param("email", "new-user@example.com")
				.param("password", "password")
				.param("role", "ADMIN")
				.param("remarks", "test"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "MEMBER")
	void memberCannotUpdateOtherMemberByDirectPost() throws Exception {
		mockMvc.perform(post("/members/update/1")
				.with(csrf())
				.param("username", "admin")
				.param("email", "admin@example.com")
				.param("password", "password")
				.param("role", "ADMIN")
				.param("remarks", "test"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "MEMBER")
	void memberCannotDeleteMemberByDirectPost() throws Exception {
		mockMvc.perform(post("/members/delete/1").with(csrf()))
				.andExpect(status().isForbidden());
	}
}
