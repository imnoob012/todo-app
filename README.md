# todo-app

チームのTODO、担当者、進捗、コメント、変更履歴を一画面で確認できるタスク管理アプリです。

このブランチでは、旧Spring Boot + Thymeleaf構成を、Goバックエンド + React/TypeScriptフロントエンドのSPA構成に置き換えています。ポートフォリオで見せやすいように、ダッシュボード、ステータス更新、コメント、変更履歴、APIテストも含めています。

## 主な機能

- ダッシュボード: 期限切れ、今週期限、進行中、完了率、担当者別件数を表示
- TODO一覧: ステータスタブとキーワード検索
- TODO詳細: 担当者、優先度、分類、説明、コメント、変更履歴を確認
- ステータス更新: TODO / IN_PROGRESS / DONE をAPI経由で更新
- コメント追加: TODOごとのコメントを追加し、履歴にも記録
- メンバー表示: APIレスポンスにパスワードを含めない安全なDTO設計

## 技術スタック

| 分類 | 技術 |
|---|---|
| バックエンド | Go 1.22+, net/http |
| フロントエンド | React 19, TypeScript, Vite |
| UI | CSS Grid/Flexbox, lucide-react |
| テスト | Go testing, httptest |
| データ保持 | インメモリストア |

インメモリストアはデモとレビューをしやすくするための実装です。実運用では、同じ `Store` インターフェースの裏側をPostgreSQLなどのDB実装に差し替える想定です。

## アーキテクチャ

```text
Browser
  |
  | SPA assets / JSON API
  v
React + TypeScript frontend
  |
  | /api/*
  v
Go net/http server
  |
  | Store interface
  v
MemoryStore
```

Go側は `backend/internal/httpapi` がHTTPを受け、`backend/internal/store` がTODOやメンバーのデータ操作を担当します。React側は `frontend/src/api.ts` にAPI呼び出しを集約し、`frontend/src/App.tsx` で画面状態を管理しています。

## セットアップ

必要なもの:

- Go 1.22以上
- Node.js 20.19以上、または22.12以上
- npm

依存関係をインストールします。

```bash
cd frontend
npm install
```

フロントエンドをビルドし、GoサーバーからSPAとして配信します。

```bash
cd frontend
npm run build

cd ../backend
go test ./...
go run ./cmd/server
```

起動後、ブラウザで `http://localhost:8080` を開きます。

開発中にViteのホットリロードを使う場合は、別ターミナルで次を実行します。

```bash
cd backend
go run ./cmd/server
```

```bash
cd frontend
npm run dev
```

Vite開発サーバーは `http://127.0.0.1:5173` で起動します。`/api` は `http://localhost:8080` にプロキシされます。

## ディレクトリ構成

```text
backend/
  cmd/server/main.go              Goサーバーの起動点
  internal/domain/models.go       APIで返すデータ構造
  internal/httpapi/server.go      ルーティングとJSONレスポンス
  internal/httpapi/server_test.go APIテスト
  internal/store/memory.go        インメモリのデータ操作

frontend/
  src/App.tsx                     Reactのメイン画面
  src/api.ts                      APIクライアント
  src/types.ts                    APIレスポンスの型定義
  src/styles.css                  UIスタイル
```

## テスト

バックエンド:

```bash
cd backend
go test ./...
```

フロントエンド:

```bash
cd frontend
npm run build
```

今回のAPIテストでは、次の観点を確認しています。

- `/api/members` がパスワードを返さないこと
- コメント追加時にコメントと変更履歴が増えること
- ダッシュボード集計APIが期待通りの形式を返すこと

## 解説資料

未経験者向けの実装解説と、シニアエンジニアから質問されやすいポイントへの回答は次にまとめています。

- `docs/06_Go_React_modernization_report.md`
