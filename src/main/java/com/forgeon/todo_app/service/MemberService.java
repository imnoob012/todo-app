package com.forgeon.todo_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	
	private final MemberMapper memberMapper;

	public List<MemberResponseDto> findAll(MemberSearchForm memberSearchForm) {
		
		List<Member> entity = memberMapper.findAll(memberSearchForm);
		
		return entity.stream()
					 .map(member -> new MemberResponseDto(
					 		 member.getId(),
					 		 member.getUsername(),
					 		 member.getEmail(),
					 		 member.getPassword(),
					 		 member.getRole(),
					 		 member.getRemarks()
					 ))
					 .toList();
	}

	public void add(Member entity) {
		memberMapper.add(entity);
	}

	public MemberResponseDto detail(Integer id) {
		
		Optional<Member> entity = memberMapper.detail(id);
		
		// クライアントによる存在しないIDが指定されたケース（404 NOT FOUND）を考慮
		return entity.map(e -> new MemberResponseDto(e.getId(),
													 e.getUsername(),
													 e.getEmail(),
													 e.getPassword(),
													 e.getRole(),
													 e.getRemarks()))
					  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID: " + id + "は存在しません"));
	}

	public void update(Member entity) {
		memberMapper.update(entity);
	}

	public void delete(Integer id, String currentUsername) {
		memberMapper.delete(id, currentUsername);
	}
	
}