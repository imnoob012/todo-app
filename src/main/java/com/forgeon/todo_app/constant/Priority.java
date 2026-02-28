package com.forgeon.todo_app.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Priority {
	
	HIGH(1, "高"),
	MEDIUM(2, "中"),
	LOW(3, "低");
	
	// DB保存値
	private final Integer id;
	
	// 画面表示値
	private final String label;
	
	// enumに変換するメソッド
	public static Priority getById(Integer id) {
        return Arrays.stream(values())
            .filter(data -> data.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}
