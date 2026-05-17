DELETE FROM todo_comments;
DELETE FROM todo_histories;
DELETE FROM todo_assignments;
DELETE FROM todos;
DELETE FROM members;

-- パスワードは「password」 --
INSERT INTO members 
(username, email, password, role, remarks, created_by, updated_by, deleted_at, deleted_by, deleted_flg)
VALUES
('admin', 'admin@example.com', '$2a$10$mJcfMj4MMAFslbw.1LbsbeYOy7UiYJiuVNYI7Fa7zs7jQra13O5lu', 'ADMIN', 'システム管理者アカウント', 'SYSTEM', 'SYSTEM', NULL, NULL, FALSE),
('taro_yamada', 'taro.yamada@example.com', '$2a$10$mJcfMj4MMAFslbw.1LbsbeYOy7UiYJiuVNYI7Fa7zs7jQra13O5lu', 'MEMBER', 'バックエンド開発担当', 'admin_user', 'admin_user', NULL, NULL, FALSE),
('hanako_sato', 'hanako.sato@example.com', '$2a$10$mJcfMj4MMAFslbw.1LbsbeYOy7UiYJiuVNYI7Fa7zs7jQra13O5lu', 'TODO_ADMIN', 'UI/UXデザイン担当', 'admin_user', 'admin_user', NULL, NULL, FALSE),
('jiro_tanaka', 'jiro.tanaka@example.com', '$2a$10$mJcfMj4MMAFslbw.1LbsbeYOy7UiYJiuVNYI7Fa7zs7jQra13O5lu', 'MEMBER', '2025年末で退職', 'admin_user', 'admin_user', '2025-12-31 23:59:59', 'admin_user', TRUE);

INSERT INTO todos
(title, priority, due_date, classification, status, completed_at, description, created_by, updated_by, deleted_at, deleted_by, deleted_flg)
VALUES
('要件定義書の作成', 1, CURRENT_DATE + 5, 'DOCS', 'IN_PROGRESS', NULL, '新規プロジェクトの要件定義を完了させる', 'taro_yamada', 'taro_yamada', NULL, NULL, FALSE),
('ログイン機能の実装', 2, CURRENT_DATE + 10, 'FEATURE', 'TODO', NULL, 'Spring Securityを使用した認証機能の実装', 'taro_yamada', 'taro_yamada', NULL, NULL, FALSE),
('トップ画面のモック作成', 2, CURRENT_DATE + 7, 'OTHER', 'DONE', CURRENT_TIMESTAMP, 'Figmaを使用してデザイン案を2パターン作成する', 'hanako_sato', 'hanako_sato', NULL, NULL, FALSE),
('備品購入', 3, CURRENT_DATE + 30, 'OTHER', 'TODO', NULL, 'ディスプレイ用のケーブルを購入する', 'admin_user', 'admin_user', NULL, NULL, FALSE),
('テストタスク', 2, CURRENT_DATE, 'TEST', 'DONE', CURRENT_TIMESTAMP, 'テスト作成', 'taro_yamada', 'taro_yamada', CURRENT_TIMESTAMP, 'taro_yamada', TRUE);



INSERT INTO todo_assignments 
(member_id, todo_id, created_by, updated_by)
VALUES
(2, 1, 'admin_user', 'admin_user'),
(2, 2, 'taro_yamada', 'taro_yamada'),
(3, 3, 'admin_user', 'admin_user'),
(2, 4, 'admin_user', 'admin_user'),
(3, 4, 'admin_user', 'admin_user');

INSERT INTO todo_comments
(todo_id, member_id, comment_text, created_by, updated_by)
VALUES
(1, 2, '要件の粒度を画面単位で整理中です。', 'taro_yamada', 'taro_yamada'),
(3, 3, 'モックは初版レビュー済みです。', 'hanako_sato', 'hanako_sato');

INSERT INTO todo_histories
(todo_id, field_name, before_value, after_value, changed_by)
VALUES
(1, 'ステータス', '未着手', '進行中', 'taro_yamada'),
(3, 'ステータス', '進行中', '完了', 'hanako_sato'),
(3, 'コメント', NULL, 'モックは初版レビュー済みです。', 'hanako_sato');
