package com.forgeon.todo_app.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.form.MemberSearchForm;

@Mapper
public interface MemberMapper {

	Optional<Member> findForAuth(String username);

	List<Member> findAll(MemberSearchForm memberSearchForm);

	int countByUsername(@Param("username") String username,
						@Param("excludeId") Integer excludeId);

	int countByEmail(@Param("email") String email,
					 @Param("excludeId") Integer excludeId);

	void add(Member entity);

	Optional<Member> detail(Integer id);

	void update(Member entity);
	
	// 引数が2つ以上だと@Paramが必要
	void delete(
			@Param("id") Integer id,
			@Param("currentUsername") String currentUsername
	);
}
