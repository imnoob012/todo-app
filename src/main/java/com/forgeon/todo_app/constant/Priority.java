package com.forgeon.todo_app.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT) // EnumをJson形式に変換（シリアライズ）される際の出力形式できるようにする
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
	
	// JSON出力: {"id": 1, "label": "高"}
}
