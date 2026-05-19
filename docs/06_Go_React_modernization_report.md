# Go + Reactモダン化 実装解説レポート

## 1. 今回の変更概要

旧構成のSpring Boot + Thymeleaf + Gradleを削除し、バックエンドをGo、フロントエンドをReact + TypeScript + Viteに置き換えました。

目的は、ポートフォリオとして「単なるCRUD」だけでなく、次の力が伝わる構成にすることです。

- APIと画面を分離したフルスタック構成を扱える
- GoでHTTP APIを設計できる
- TypeScriptでAPIレスポンスの型を明確にできる
- ダッシュボード、進捗、コメント、変更履歴など実務寄りの機能を作れる
- テストで重要な仕様を守れる

## 2. バックエンドの構成

バックエンドは `backend/` にまとめています。

```text
backend/
  cmd/server/main.go
  internal/domain/models.go
  internal/httpapi/server.go
  internal/httpapi/server_test.go
  internal/store/memory.go
```

### 2.1 起動処理

対象: `backend/cmd/server/main.go`

```go
port := env("PORT", "8080")
staticDir := env("STATIC_DIR", "../frontend/dist")

appStore := store.NewMemoryStore()
server := httpapi.NewServer(appStore, staticDir)
```

ここでは、ポート番号とフロントエンドの配信先を環境変数で変更できるようにしています。

未経験者向けに言うと、`main.go` はアプリの入口です。ここで「どのポートで待ち受けるか」「どのデータストアを使うか」「どのHTTPサーバーを起動するか」を決めています。

### 2.2 APIで返すデータ構造

対象: `backend/internal/domain/models.go`

```go
type Member struct {
	ID       int    `json:"id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Role     Role   `json:"role"`
	RoleName string `json:"roleName"`
	Remarks  string `json:"remarks"`
}
```

`Member` にはパスワードを持たせていません。以前のレビューで指摘された「APIがpasswordを返す問題」を起こさないためです。

Goの構造体タグ `json:"username"` は、JSONに変換したときのキー名を指定しています。フロントエンドはこのJSONを受け取って画面に表示します。

### 2.3 HTTPルーティング

対象: `backend/internal/httpapi/server.go`

```go
if strings.HasPrefix(r.URL.Path, "/api/") {
	s.handleAPI(w, r)
	return
}

s.serveStatic(w, r)
```

URLが `/api/` で始まる場合はJSON APIとして処理し、それ以外はReactの画面ファイルを返します。

これにより、1つのGoサーバーで次の2つを扱えます。

- `/api/todos` などのJSON API
- `/` などのReact SPA画面

### 2.4 データ操作の責務分離

対象: `backend/internal/httpapi/server.go`

```go
type Store interface {
	CurrentMember() domain.Member
	Members() []domain.Member
	Todos(filters domain.TodoFilters) []domain.Todo
	Todo(id int) (domain.Todo, error)
	AddComment(todoID int, body string) (domain.Todo, error)
	UpdateStatus(todoID int, statusCode string) (domain.Todo, error)
	Dashboard() domain.Dashboard
}
```

HTTP層は「リクエストを受けてレスポンスを返す」ことに集中し、データ操作は `Store` に任せています。

この形にしておくと、今は `MemoryStore` でも、将来は `PostgresStore` に差し替えやすくなります。実務では、このように「直接依存しすぎない設計」がよく求められます。

### 2.5 並行アクセスへの配慮

対象: `backend/internal/store/memory.go`

```go
s.mu.RLock()
defer s.mu.RUnlock()
```

GoのHTTPサーバーは複数リクエストを同時に処理します。そのため、同じデータに同時アクセスすると不整合が起きる可能性があります。

`sync.RWMutex` を使うことで、読み取り時は `RLock`、更新時は `Lock` を使い、データを安全に扱うようにしています。

### 2.6 コメント追加と履歴記録

対象: `backend/internal/store/memory.go`

```go
body = strings.TrimSpace(body)
if body == "" {
	return domain.Todo{}, errors.New("comment body is required")
}
```

空コメントは保存しないように、サーバー側でバリデーションしています。

コメントを追加したあとには、同じTODOに変更履歴も追加します。

```go
s.todos[i].Comments = append(s.todos[i].Comments, comment)
s.addHistoryLocked(i, "コメント", "-", body)
```

画面だけで入力チェックするのではなく、API側でも守るのが実務では重要です。

## 3. フロントエンドの構成

フロントエンドは `frontend/` にまとめています。

```text
frontend/
  src/App.tsx
  src/api.ts
  src/types.ts
  src/styles.css
```

### 3.1 APIレスポンスの型定義

対象: `frontend/src/types.ts`

```ts
export type Todo = {
  id: number;
  title: string;
  assignees: MemberSummary[];
  status: TodoStatus;
  comments?: TodoComment[];
  histories?: TodoHistory[];
};
```

TypeScriptの型を定義することで、「APIからどんなデータが返るか」をコード上で明確にしています。

たとえば `todo.status.code` を使うとき、`TODO | IN_PROGRESS | DONE` のいずれかだと分かるため、実装ミスを早く見つけやすくなります。

### 3.2 API呼び出しの共通化

対象: `frontend/src/api.ts`

```ts
async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init);
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: "通信に失敗しました" }));
    throw new Error(error.error ?? "通信に失敗しました");
  }
  return response.json() as Promise<T>;
}
```

API通信の成功・失敗処理を1か所にまとめています。

毎回 `fetch` のエラーハンドリングを書くと重複するため、共通関数にして、画面側は `api.todos()` や `api.dashboard()` のように読みやすく呼び出せるようにしました。

### 3.3 初期表示のデータ取得

対象: `frontend/src/App.tsx`

```tsx
const [nextMe, nextDashboard, nextTodos, nextMembers] = await Promise.all([
  api.me(),
  api.dashboard(),
  api.todos(params),
  api.members()
]);
```

`Promise.all` は複数のAPIを並行して呼び出します。

ダッシュボード、TODO一覧、メンバー一覧は互いに待つ必要がないため、順番に呼ぶよりも画面表示が速くなります。

### 3.4 Reactの状態管理

対象: `frontend/src/App.tsx`

```tsx
const [todos, setTodos] = useState<Todo[]>([]);
const [selectedTodo, setSelectedTodo] = useState<Todo | null>(null);
const [status, setStatus] = useState<StatusFilter>("ALL");
```

Reactでは、画面に表示するデータを `useState` で管理します。

- `todos`: 一覧に表示するTODO
- `selectedTodo`: 右側の詳細パネルに表示するTODO
- `status`: 現在選択しているステータスタブ

状態が変わるとReactが必要な部分を再描画します。

### 3.5 XSSへの基本対策

今回の画面はReactで値を表示しています。Reactは通常のテキスト表示を自動でエスケープするため、以前のレビューで指摘された `innerHTML` 直書きによるXSSリスクを避けやすい構成です。

今回のコードでは `dangerouslySetInnerHTML` を使っていません。

## 4. 追加した実務寄り機能

### ダッシュボード

期限切れ、今週期限、進行中、完了率、担当者別件数、優先度別件数、分類別件数を表示します。

実務では、単にデータを登録できるだけではなく、「今どこが危ないか」「誰に作業が偏っているか」を見える化することが重要です。

### ステータス管理

TODOに次の状態を持たせました。

- TODO
- IN_PROGRESS
- DONE

完了時には `CompletedAt` を設定し、未完了に戻した場合はクリアします。

### コメント

TODOごとにコメントを追加できます。やり取りや補足情報をタスク単位で残せるため、実務アプリらしさが増します。

### 変更履歴

ステータス変更やコメント追加を履歴として残します。

実務では「誰が、いつ、何を変えたか」を追えることが重要です。今は簡易実装ですが、DB化する場合も同じ考え方で履歴テーブルを作れます。

## 5. テストで確認していること

対象: `backend/internal/httpapi/server_test.go`

```go
request := httptest.NewRequest(http.MethodGet, "/api/members", nil)
response := httptest.NewRecorder()

server.ServeHTTP(response, request)
```

`httptest` を使うと、実際にサーバーを起動しなくてもAPIの挙動をテストできます。

今回のテストでは、特に次を確認しています。

- メンバーAPIがパスワードを返さない
- コメント追加でコメントと履歴が増える
- ダッシュボードAPIが集計値を返す

## 6. シニアエンジニアに質問されそうなポイントと回答

### Q1. なぜGoにしたのですか？

GoはHTTPサーバーを標準ライブラリだけで作りやすく、起動が速く、構成もシンプルです。今回のようなJSON APIには相性が良く、ポートフォリオとしても「Java以外のバックエンドも扱える」ことを示せます。

### Q2. なぜDBではなくインメモリなのですか？

今回は「Goバックエンド化」と「React UI化」を短いブランチで確認できるようにするため、DBなしで動く構成にしました。ただしHTTP層は `Store` インターフェースに依存しているため、将来 `PostgresStore` を追加すればDB実装に差し替えられます。

### Q3. パスワード漏えいの指摘にはどう対応していますか？

APIで返す `Member` 型にpasswordフィールドを持たせていません。そのため、JSON変換してもパスワードがレスポンスに含まれません。

### Q4. XSS対策はどう考えていますか？

Reactの通常描画はテキストをエスケープします。また、今回の実装では `dangerouslySetInnerHTML` を使っていません。ユーザー入力をHTMLとして直接流し込まない方針です。

### Q5. 同時アクセス時のデータ競合はどうしていますか？

`MemoryStore` に `sync.RWMutex` を持たせ、読み取り時は `RLock`、更新時は `Lock` を使っています。複数リクエストが同時に来ても、インメモリデータが壊れにくいようにしています。

### Q6. なぜ `Store` インターフェースを作ったのですか？

HTTP層とデータ保存層を分けるためです。これにより、APIのテストではメモリ実装を使い、本番ではDB実装を使うなど、差し替えがしやすくなります。

### Q7. トランザクションはどう考えますか？

今はインメモリなのでトランザクションはありません。DB化する場合、ステータス更新と履歴追加、コメント追加と履歴追加は1つのトランザクションで処理します。片方だけ保存される状態を防ぐためです。

### Q8. 認証・認可はどうしますか？

このブランチはUI/UXとAPI分離のモダン化が主目的のため、ログイン処理は簡略化しています。実運用に近づける場合は、CookieセッションまたはJWTを導入し、管理者のみ可能な操作をミドルウェアで制御します。

### Q9. なぜReact + TypeScriptにしたのですか？

TypeScriptにより、APIレスポンスや画面状態の型を明確にできます。TODO、メンバー、ダッシュボードの構造がコード上で分かるため、変更時の影響範囲も追いやすくなります。

### Q10. 状態管理ライブラリを使わない理由は？

今回の画面規模では `useState` と `useEffect` で十分です。画面数やAPIキャッシュが増えた場合は、TanStack QueryやZustandなどの導入を検討します。

### Q11. エラーハンドリングはどこにありますか？

フロントエンドでは `frontend/src/api.ts` の `request` 関数に通信エラー処理を集約しています。バックエンドでは `respondError` と `respondStoreError` でJSONエラーを返すようにしています。

### Q12. 次に改善するなら何をしますか？

優先度が高い順に、次を実施します。

1. PostgreSQL永続化
2. セッション認証とロール別認可
3. TODO作成・編集フォーム
4. フロントエンドのコンポーネント分割
5. E2Eテスト追加

## 7. 実行確認コマンド

```bash
cd frontend
npm install
npm run build
```

```bash
cd backend
go test ./...
go run ./cmd/server
```

ブラウザで `http://localhost:8080` を開くと、GoサーバーがReactのビルド済み画面を配信します。
