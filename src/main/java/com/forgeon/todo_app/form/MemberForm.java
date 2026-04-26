package com.forgeon.todo_app.form;

import com.forgeon.todo_app.constant.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor // 登録フォームをGETメソッドでnewするため
public class MemberForm {
	
	@NotBlank(message="ユーザー名は必須です", groups = {Add.class, Update.class})
	@Size(max=50, message="ユーザー名は50文字以内で入力してください", groups = {Add.class, Update.class})
	private String username;
	
	@NotBlank(message="メールアドレスは必須です", groups = {Add.class, Update.class})
	@Size(max=50, message="メールアドレスは50文字以内で入力してください", groups = {Add.class, Update.class})
	@Email(message="正しいメールアドレス形式で入力してください", groups = {Add.class, Update.class})
	private String email;
	
	@NotBlank(message="パスワードは必須です", groups = Add.class)
	@Size(max=50, message="パスワードは50文字以内で入力してください", groups = {Add.class, Update.class})
	private String password;
	
	@NotNull(message="権限を選択してください", groups = {Add.class, Update.class})
	private Role role;
	
	@NotBlank(message="備考は必須です", groups = {Add.class, Update.class})
	@Size(max=2000, message="備考は2000文字以内で入力してください", groups = {Add.class, Update.class})
	private String remarks;
	
	// グループ
	public interface Update {}
	public interface Add {}
}