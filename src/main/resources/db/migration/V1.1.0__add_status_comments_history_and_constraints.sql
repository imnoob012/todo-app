ALTER TABLE todos
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    ADD COLUMN completed_at TIMESTAMP;

ALTER TABLE todos
    ADD CONSTRAINT check_todo_status
    CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'));

CREATE UNIQUE INDEX uq_members_username_active
    ON members (username)
    WHERE deleted_flg = FALSE;

CREATE UNIQUE INDEX uq_members_email_active
    ON members (email)
    WHERE deleted_flg = FALSE;

CREATE INDEX idx_todos_status ON todos (status);
CREATE INDEX idx_todos_due_date ON todos (due_date);

CREATE TABLE todo_comments (
    id SERIAL PRIMARY KEY,
    todo_id INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    comment_text VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    deleted_flg BOOLEAN NOT NULL DEFAULT FALSE,

    FOREIGN KEY(todo_id) REFERENCES todos(id),
    FOREIGN KEY(member_id) REFERENCES members(id)
);

CREATE TABLE todo_histories (
    id SERIAL PRIMARY KEY,
    todo_id INTEGER NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    before_value VARCHAR(2000),
    after_value VARCHAR(2000),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(50) NOT NULL,

    FOREIGN KEY(todo_id) REFERENCES todos(id)
);

CREATE INDEX idx_todo_comments_todo_id ON todo_comments (todo_id);
CREATE INDEX idx_todo_histories_todo_id ON todo_histories (todo_id);
