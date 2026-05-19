package httpapi

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/imnoob012/todo-app/backend/internal/store"
)

func TestMembersAPIDoesNotExposePassword(t *testing.T) {
	server := NewServer(store.NewMemoryStore(), "")
	request := httptest.NewRequest(http.MethodGet, "/api/members", nil)
	response := httptest.NewRecorder()

	server.ServeHTTP(response, request)

	if response.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", response.Code)
	}
	if strings.Contains(response.Body.String(), "password") {
		t.Fatalf("members api must not expose password: %s", response.Body.String())
	}
}

func TestAddCommentRecordsCommentAndHistory(t *testing.T) {
	server := NewServer(store.NewMemoryStore(), "")
	request := httptest.NewRequest(http.MethodPost, "/api/todos/1/comments", strings.NewReader(`{"body":"レビューしました"}`))
	response := httptest.NewRecorder()

	server.ServeHTTP(response, request)

	if response.Code != http.StatusCreated {
		t.Fatalf("expected status 201, got %d", response.Code)
	}

	var todo struct {
		Comments  []map[string]any `json:"comments"`
		Histories []map[string]any `json:"histories"`
	}
	if err := json.Unmarshal(response.Body.Bytes(), &todo); err != nil {
		t.Fatal(err)
	}
	if len(todo.Comments) < 2 {
		t.Fatalf("expected appended comment, got %#v", todo.Comments)
	}
	if todo.Histories[0]["field"] != "コメント" {
		t.Fatalf("expected latest history field to be comment, got %#v", todo.Histories[0])
	}
}

func TestDashboardReturnsSummary(t *testing.T) {
	server := NewServer(store.NewMemoryStore(), "")
	request := httptest.NewRequest(http.MethodGet, "/api/dashboard", nil)
	response := httptest.NewRecorder()

	server.ServeHTTP(response, request)

	if response.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", response.Code)
	}
	if !strings.Contains(response.Body.String(), `"total":4`) {
		t.Fatalf("expected dashboard total count: %s", response.Body.String())
	}
}
