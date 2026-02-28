-- メンバー情報 --
CREATE TABLE members (
    id SERIAL PRIMARY KEY, -- ID
    username VARCHAR(50) NOT NULL, -- ユーザー名
    email VARCHAR(50) NOT NULL, -- メールアドレス
    password VARCHAR NOT NULL, -- パスワード
    role VARCHAR(10) NOT NULL, -- 権限
    remarks VARCHAR(2000), -- 備考
    -- 全テーブル共通のカラム --
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    created_by VARCHAR(50) NOT NULL, -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新日時
    updated_by VARCHAR(50) NOT NULL, -- 更新者
    deleted_at TIMESTAMP, -- 削除日時
    deleted_by VARCHAR(50), -- 削除者
    deleted_flg BOOLEAN NOT NULL DEFAULT FALSE -- 削除フラグ
 );
    
-- TODO情報 --
CREATE TABLE todos (
	id SERIAL PRIMARY KEY, -- ID
	title VARCHAR(50) NOT NULL, -- TODO名
	priority SMALLINT NOT NULL, -- 優先度
	due_date DATE, -- 期限
	classification VARCHAR(10) NOT NULL, -- 分類
	description VARCHAR(2000), -- 内容
	-- 全テーブル共通のカラム --
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    created_by VARCHAR(50) NOT NULL, -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新日時
    updated_by VARCHAR(50) NOT NULL, -- 更新者
    deleted_at TIMESTAMP, -- 削除日時
    deleted_by VARCHAR(50), -- 削除者
    deleted_flg BOOLEAN NOT NULL DEFAULT FALSE, -- 削除フラグ
    
	CONSTRAINT check_priority_digit CHECK (priority BETWEEN 0 AND 9)
);

-- MEMBER⇄TODO --
CREATE TABLE todo_assignments (
	id SERIAL PRIMARY KEY, -- ID
	member_id INTEGER NOT NULL, -- メンバーID
	todo_id INTEGER NOT NULL, -- TODO ID
	-- 全テーブル共通のカラム --
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 作成日時
    created_by VARCHAR(50) NOT NULL, -- 作成者
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 更新日時
    updated_by VARCHAR(50) NOT NULL, -- 更新者
    deleted_at TIMESTAMP, -- 削除日時
    deleted_by VARCHAR(50), -- 削除者
    deleted_flg BOOLEAN NOT NULL DEFAULT FALSE, -- 削除フラグ
    
    FOREIGN KEY(todo_id) REFERENCES todos(id),
   	FOREIGN KEY(member_id) REFERENCES members(id)
);