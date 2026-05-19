import {
  AlertTriangle,
  CalendarDays,
  CheckCircle2,
  History,
  LayoutDashboard,
  MessageSquare,
  Search,
  Users
} from "lucide-react";
import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { api } from "./api";
import type { CountItem, Dashboard, Member, Todo } from "./types";

type StatusFilter = "ALL" | "TODO" | "IN_PROGRESS" | "DONE";

const statuses: Array<{ code: StatusFilter; label: string }> = [
  { code: "ALL", label: "すべて" },
  { code: "TODO", label: "未着手" },
  { code: "IN_PROGRESS", label: "進行中" },
  { code: "DONE", label: "完了" }
];

export function App() {
  const [me, setMe] = useState<Member | null>(null);
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [todos, setTodos] = useState<Todo[]>([]);
  const [members, setMembers] = useState<Member[]>([]);
  const [selectedTodo, setSelectedTodo] = useState<Todo | null>(null);
  const [status, setStatus] = useState<StatusFilter>("ALL");
  const [query, setQuery] = useState("");
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = async () => {
    setError(null);
    const params = new URLSearchParams();
    if (query.trim()) params.set("q", query.trim());
    if (status !== "ALL") params.set("status", status);

    const [nextMe, nextDashboard, nextTodos, nextMembers] = await Promise.all([
      api.me(),
      api.dashboard(),
      api.todos(params),
      api.members()
    ]);
    setMe(nextMe);
    setDashboard(nextDashboard);
    setTodos(nextTodos);
    setMembers(nextMembers);
  };

  useEffect(() => {
    reload()
      .catch((err: unknown) => setError(err instanceof Error ? err.message : "読み込みに失敗しました"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    reload().catch((err: unknown) => setError(err instanceof Error ? err.message : "検索に失敗しました"));
  }, [status]);

  const selectedTodoID = selectedTodo?.id;
  useEffect(() => {
    if (!selectedTodoID) return;
    api.todo(selectedTodoID)
      .then(setSelectedTodo)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : "TODO詳細の取得に失敗しました"));
  }, [selectedTodoID]);

  const focusTodos = useMemo(() => todos.slice(0, 4), [todos]);

  const submitSearch = (event: FormEvent) => {
    event.preventDefault();
    reload().catch((err: unknown) => setError(err instanceof Error ? err.message : "検索に失敗しました"));
  };

  const openTodo = async (id: number) => {
    const todo = await api.todo(id);
    setSelectedTodo(todo);
  };

  const changeStatus = async (id: number, nextStatus: string) => {
    const todo = await api.updateStatus(id, nextStatus);
    setSelectedTodo(todo);
    await reload();
  };

  const submitComment = async (event: FormEvent) => {
    event.preventDefault();
    if (!selectedTodo || !comment.trim()) return;
    const todo = await api.addComment(selectedTodo.id, comment.trim());
    setSelectedTodo(todo);
    setComment("");
    await reload();
  };

  if (loading) {
    return <div className="boot-screen">Loading workspace...</div>;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">T</span>
          <div>
            <strong>Todo Console</strong>
            <small>Go + React</small>
          </div>
        </div>
        <nav className="nav-list">
          <a href="#dashboard"><LayoutDashboard size={18} /> ダッシュボード</a>
          <a href="#todos"><CheckCircle2 size={18} /> TODO</a>
          <a href="#members"><Users size={18} /> メンバー</a>
        </nav>
        {me && (
          <div className="viewer">
            <span>{me.roleName}</span>
            <strong>{me.username}</strong>
          </div>
        )}
      </aside>

      <main>
        <header className="page-header">
          <div>
            <p>Team task operations</p>
            <h1>タスクの状態と担当を一画面で追える管理画面</h1>
          </div>
          <form className="search-box" onSubmit={submitSearch}>
            <Search size={18} />
            <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="TODO名・担当者・内容で検索" />
            <button type="submit">検索</button>
          </form>
        </header>

        {error && <div className="error-banner">{error}</div>}

        {dashboard && (
          <section id="dashboard" className="summary-grid">
            <Metric label="総TODO数" value={dashboard.summary.total} icon={<CheckCircle2 />} />
            <Metric label="期限切れ" value={dashboard.summary.overdue} icon={<AlertTriangle />} tone="danger" />
            <Metric label="今週期限" value={dashboard.summary.dueThisWeek} icon={<CalendarDays />} />
            <Metric label="進行中" value={dashboard.summary.inProgress} icon={<History />} />
            <Metric label="完了率" value={`${dashboard.summary.progressRate.toFixed(1)}%`} icon={<LayoutDashboard />} tone="success" />
          </section>
        )}

        {dashboard && (
          <section className="analytics-grid">
            <BarPanel title="ステータス別" items={dashboard.statusCounts} total={dashboard.summary.total} />
            <BarPanel title="担当者別" items={dashboard.assigneeCounts} total={dashboard.summary.total} />
            <BarPanel title="優先度別" items={dashboard.priorityCounts} total={dashboard.summary.total} />
            <BarPanel title="分類別" items={dashboard.classCounts} total={dashboard.summary.total} />
          </section>
        )}

        <section id="todos" className="workspace-grid">
          <div className="work-panel wide">
            <div className="panel-header">
              <div>
                <p>TODO board</p>
                <h2>TODO一覧</h2>
              </div>
              <div className="status-tabs">
                {statuses.map((item) => (
                  <button key={item.code} className={status === item.code ? "active" : ""} onClick={() => setStatus(item.code)}>
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="todo-table">
              {todos.map((todo) => (
                <button className="todo-row" key={todo.id} onClick={() => openTodo(todo.id)}>
                  <span className={`status-dot ${todo.status.code.toLowerCase()}`} />
                  <span>
                    <strong>{todo.title}</strong>
                    <small>{todo.assignees.map((assignee) => assignee.username).join(", ") || "未担当"}</small>
                  </span>
                  <span>{todo.status.label}</span>
                  <span>{todo.priority.label}</span>
                  <span>{todo.dueDate}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="work-panel">
            <div className="panel-header compact">
              <div>
                <p>Focus</p>
                <h2>優先確認</h2>
              </div>
            </div>
            <div className="focus-list">
              {focusTodos.map((todo) => (
                <button key={todo.id} onClick={() => openTodo(todo.id)}>
                  <span>{todo.priority.label}</span>
                  <strong>{todo.title}</strong>
                  <small>{todo.status.label}</small>
                </button>
              ))}
            </div>
          </div>
        </section>

        <section id="members" className="work-panel">
          <div className="panel-header compact">
            <div>
              <p>Members</p>
              <h2>メンバー一覧</h2>
            </div>
          </div>
          <div className="member-grid">
            {members.map((member) => (
              <article key={member.id} className="member-card">
                <strong>{member.username}</strong>
                <span>{member.roleName}</span>
                <small>{member.email}</small>
                <p>{member.remarks}</p>
              </article>
            ))}
          </div>
        </section>
      </main>

      {selectedTodo && (
        <aside className="detail-drawer" aria-label="TODO詳細">
          <button className="close-button" onClick={() => setSelectedTodo(null)}>閉じる</button>
          <div className="drawer-header">
            <span className={`status-pill ${selectedTodo.status.code.toLowerCase()}`}>{selectedTodo.status.label}</span>
            <h2>{selectedTodo.title}</h2>
            <p>{selectedTodo.description}</p>
          </div>

          <div className="drawer-actions">
            {statuses.filter((item) => item.code !== "ALL").map((item) => (
              <button key={item.code} onClick={() => changeStatus(selectedTodo.id, item.code)}>
                {item.label}
              </button>
            ))}
          </div>

          <dl className="detail-list">
            <div><dt>担当者</dt><dd>{selectedTodo.assignees.map((assignee) => assignee.username).join(", ")}</dd></div>
            <div><dt>優先度</dt><dd>{selectedTodo.priority.label}</dd></div>
            <div><dt>期限</dt><dd>{selectedTodo.dueDate}</dd></div>
            <div><dt>分類</dt><dd>{selectedTodo.classification.label}</dd></div>
          </dl>

          <section className="drawer-section">
            <h3><MessageSquare size={18} /> コメント</h3>
            <form className="comment-form" onSubmit={submitComment}>
              <textarea value={comment} onChange={(event) => setComment(event.target.value)} placeholder="進捗や確認事項を残す" />
              <button type="submit">追加</button>
            </form>
            {(selectedTodo.comments ?? []).map((item) => (
              <article className="comment-card" key={item.id}>
                <div>
                  <strong>{item.author}</strong>
                  <time>{formatDate(item.createdAt)}</time>
                </div>
                <p>{item.body}</p>
              </article>
            ))}
          </section>

          <section className="drawer-section">
            <h3><History size={18} /> 変更履歴</h3>
            {(selectedTodo.histories ?? []).map((item) => (
              <article className="history-card" key={item.id}>
                <span>{item.field}</span>
                <p>{item.beforeValue} → {item.afterValue}</p>
                <small>{item.changedBy} / {formatDate(item.changedAt)}</small>
              </article>
            ))}
          </section>
        </aside>
      )}
    </div>
  );
}

function Metric({ label, value, icon, tone }: { label: string; value: number | string; icon: ReactNode; tone?: string }) {
  return (
    <article className={`metric-card ${tone ?? ""}`}>
      <div>{icon}</div>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function BarPanel({ title, items, total }: { title: string; items: CountItem[]; total: number }) {
  return (
    <article className="bar-panel">
      <h2>{title}</h2>
      {items.map((item) => (
        <div className="bar-item" key={item.label}>
          <div>
            <span>{item.label}</span>
            <strong>{item.count}</strong>
          </div>
          <div className="bar-track">
            <span style={{ width: `${total === 0 ? 0 : (item.count / total) * 100}%` }} />
          </div>
        </div>
      ))}
    </article>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ja-JP", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}
