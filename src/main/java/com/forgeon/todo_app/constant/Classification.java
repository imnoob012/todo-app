package com.forgeon.todo_app.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT) // EnumをJson形式に変換（シリアライズ）される際の出力形式できるようにする
public enum Classification {
	
	FEATURE("機能開発"),
	BUG("バグ修正"),
	REFACTOR("リファクタ"),
	TEST("テスト"),
	DOCS("ドキュメント"),
	OTHER("その他");
	
	private final String label;
}

// JSON出力: {"label": "機能開発"}