package com.forgeon.todo_app.controller.view;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.forgeon.todo_app.dto.MemberResponseDto;
import com.forgeon.todo_app.entity.Member;
import com.forgeon.todo_app.form.MemberForm;
import com.forgeon.todo_app.form.MemberSearchForm;
import com.forgeon.todo_app.security.CustomUserDetails;
import com.forgeon.todo_app.service.MemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class MemberViewController {
	
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	
	@GetMapping("/members")
	String members(Model model, MemberSearchForm memberSearchForm) {
		
		model.addAttribute("members", memberService.findAll(memberSearchForm));
		model.addAttribute("memberSearchForm", memberSearchForm);
		return "members";
	}
	
	@GetMapping("/members/add")
	String add(Model model) {
		
		model.addAttribute("memberForm", new MemberForm());
		
		return "member-form";
	}
	
	@PostMapping("/members/add")
	String add(@Validated(MemberForm.Add.class) MemberForm form,
			   BindingResult result, Model model, 
			   @AuthenticationPrincipal UserDetails userDetails) {
		
		if (result.hasErrors()) {
			return "member-form";
		}
		
		Member entity = convertToEntity(form, userDetails);
		memberService.add(entity);
		
		return "redirect:/members";
	}
	
	@GetMapping("/members/{id}")
	String detail(@PathVariable("id") Integer id, Model model) {
		 model.addAttribute("member", memberService.detail(id));
		return "member-detail";
	}
	
	@GetMapping("/members/edit/{id}")
	String edit(@PathVariable("id") Integer id, Model model) {
		MemberResponseDto dto = memberService.detail(id);
		
		// 画面表示用
		model.addAttribute("id", id);
		
		// 二重でハッシュ化させてしまうので、空で渡す
		model.addAttribute("memberForm", new MemberForm(dto.getUsername(),
														dto.getEmail(),
														null,
														dto.getRole(),
														dto.getRemarks()));
		
		return "member-edit";
	}
	
	@PostMapping("/members/update/{id}")
	String update(@Validated(MemberForm.Update.class) MemberForm form, BindingResult result,
			      @PathVariable("id") Integer id, Model model,
			      @AuthenticationPrincipal UserDetails userDetails) {
		if (result.hasErrors()) {
			model.addAttribute("id", id);
			return "member-edit";
		}
		
		Member entity = convertToEntity(form, userDetails);
		// formにidは入っていないので
		entity.setId(id);
		memberService.update(entity);

		return "redirect:/members/" + id;
	}
	
	@GetMapping("/members/delete/{id}")
	String deleteConfirm(@PathVariable("id") Integer id, Model model) {
		model.addAttribute(id);
		return "member-delete-confirm";
	}
	
	@PostMapping("/members/delete/{id}")
	String deleteExecute(@PathVariable("id") Integer id,
						 @AuthenticationPrincipal CustomUserDetails currentUser,
						 HttpServletRequest request) {
		memberService.delete(id, currentUser.getUsername());
		if (currentUser.getId().equals(id)) {
			try {
				request.logout();
				SecurityContextHolder.clearContext();
			} catch(ServletException e) {
				e.printStackTrace();
			}
			return "redirect:/login";
			
		}
		return "member-delete-success";
	}
	
	// 変換メソッド(FORM→ENTITY)
	private Member convertToEntity(MemberForm form, UserDetails userDetails) {
		
		Member entity = new Member();
		
		// 変換処理（パスワードはエンコードする）
		entity.setUsername(form.getUsername());
		entity.setEmail(form.getEmail());
		if (form.getPassword() != null && !form.getPassword().isEmpty()) {
			entity.setPassword(passwordEncoder.encode(form.getPassword()));			
		} else {
			// MapperXMLの編集処理のパスワードの分岐を正常に動作させるため
			entity.setPassword(null);
		}
		
		entity.setRole(form.getRole());
		entity.setRemarks(form.getRemarks());
		// 監査情報（名前）
		entity.setCreatedBy(userDetails.getUsername());
		// 削除情報をセットする際にもこちらの値を使用するものとする
		entity.setUpdatedBy(userDetails.getUsername());
		
		return entity;
	}
}