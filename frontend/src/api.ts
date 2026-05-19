import type { Dashboard, Member, Todo } from "./types";

const jsonHeaders = {
  "Content-Type": "application/json"
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init);
  if (!response.ok) {
    const error = await response.json().catch(() => ({ error: "通信に失敗しました" }));
    throw new Error(error.error ?? "通信に失敗しました");
  }
  return response.json() as Promise<T>;
}

export const api = {
  me: () => request<Member>("/api/me"),
  dashboard: () => request<Dashboard>("/api/dashboard"),
  members: () => request<Member[]>("/api/members"),
  todos: (params: URLSearchParams) => request<Todo[]>(`/api/todos?${params.toString()}`),
  todo: (id: number) => request<Todo>(`/api/todos/${id}`),
  addComment: (id: number, body: string) =>
    request<Todo>(`/api/todos/${id}/comments`, {
      method: "POST",
      headers: jsonHeaders,
      body: JSON.stringify({ body })
    }),
  updateStatus: (id: number, status: string) =>
    request<Todo>(`/api/todos/${id}/status`, {
      method: "PATCH",
      headers: jsonHeaders,
      body: JSON.stringify({ status })
    })
};
