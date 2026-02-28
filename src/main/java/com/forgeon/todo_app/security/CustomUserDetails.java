package com.forgeon.todo_app.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;


public class CustomUserDetails extends User {
	// Thymeleafで認可処理に使うため、UserにIDを上乗せ
	private final Integer id;
	
	public CustomUserDetails(Integer id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
}