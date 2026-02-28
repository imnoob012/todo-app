package com.forgeon.todo_app.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class AbstractAudit {
	// 全テーブル共通のカラム
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime updatedAt;
	private String updatedBy;
	private LocalDateTime deletedAt;
	private String deletedBy;
	private Boolean deletedFlg;
}


// インターフェースによる定数定義では、スレッド間で共有される static 領域に値が固定されてしまい、Webサーバーのようなマルチスレッド環境下では致命的なデータ不整合を引き起こします。
// 抽象クラスでフィールドを定義することで、MyBatisが各レコードを個別のオブジェクトとしてインスタンス化する際に、非staticなインスタンス変数として安全に値をマッピングできるようにしています。