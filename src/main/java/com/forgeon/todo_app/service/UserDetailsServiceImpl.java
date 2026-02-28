package com.forgeon.todo_app.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.mapper.MemberMapper;
import com.forgeon.todo_app.security.CustomUserDetails;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private MemberMapper memberMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Member> user = memberMapper.findForAuth(username);
		
		return user.map(existUser -> new CustomUserDetails(
						existUser.getId(),
						existUser.getUsername(),
						existUser.getPassword(),
						Collections.singleton(new SimpleGrantedAuthority(existUser.getRole().name()))
					 ))
				   .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりませんでした。"));
	}
}