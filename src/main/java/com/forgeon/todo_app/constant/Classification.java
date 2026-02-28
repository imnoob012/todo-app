package com.forgeon.todo_app.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Classification {
	
	FEATURE("機能開発"),
	BUG("バグ修正"),
	REFACTOR("リファクタ"),
	TEST("テスト"),
	DOCS("ドキュメント"),
	OTHER("その他");
	
	private final String label;
}