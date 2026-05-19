package httpapi

import (
	"encoding/json"
	"errors"
	"io/fs"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"github.com/imnoob012/todo-app/backend/internal/domain"
	"github.com/imnoob012/todo-app/backend/internal/store"
)

type Store interface {
	CurrentMember() domain.Member
	Members() []domain.Member
	Todos(filters domain.TodoFilters) []domain.Todo
	Todo(id int) (domain.Todo, error)
	AddComment(todoID int, body string) (domain.Todo, error)
	UpdateStatus(todoID int, statusCode string) (domain.Todo, error)
	Dashboard() domain.Dashboard
}

type Server struct {
	store     Store
	staticDir string
}

func NewServer(store Store, staticDir string) *Server {
	return &Server{store: store, staticDir: staticDir}
}

func (s *Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("X-Content-Type-Options", "nosniff")

	if r.URL.Path == "/api/health" {
		respondJSON(w, http.StatusOK, map[string]string{"status": "ok"})
		return
	}

	if strings.HasPrefix(r.URL.Path, "/api/") {
		s.handleAPI(w, r)
		return
	}

	s.serveStatic(w, r)
}

func (s *Server) handleAPI(w http.ResponseWriter, r *http.Request) {
	switch {
	case r.Method == http.MethodGet && r.URL.Path == "/api/me":
		respondJSON(w, http.StatusOK, s.store.CurrentMember())
	case r.Method == http.MethodGet && r.URL.Path == "/api/members":
		respondJSON(w, http.StatusOK, s.store.Members())
	case r.Method == http.MethodGet && r.URL.Path == "/api/dashboard":
		respondJSON(w, http.StatusOK, s.store.Dashboard())
	case r.Method == http.MethodGet && r.URL.Path == "/api/todos":
		respondJSON(w, http.StatusOK, s.store.Todos(todoFilters(r)))
	case strings.HasPrefix(r.URL.Path, "/api/todos/"):
		s.handleTodo(w, r)
	default:
		respondError(w, http.StatusNotFound, "endpoint not found")
	}
}

func (s *Server) handleTodo(w http.ResponseWriter, r *http.Request) {
	id, suffix, ok := parseTodoPath(r.URL.Path)
	if !ok {
		respondError(w, http.StatusNotFound, "todo not found")
		return
	}

	switch {
	case r.Method == http.MethodGet && suffix == "":
		todo, err := s.store.Todo(id)
		if err != nil {
			respondStoreError(w, err)
			return
		}
		respondJSON(w, http.StatusOK, todo)
	case r.Method == http.MethodPost && suffix == "comments":
		var request struct {
			Body string `json:"body"`
		}
		if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
			respondError(w, http.StatusBadRequest, "invalid json body")
			return
		}
		todo, err := s.store.AddComment(id, request.Body)
		if err != nil {
			respondStoreError(w, err)
			return
		}
		respondJSON(w, http.StatusCreated, todo)
	case r.Method == http.MethodPatch && suffix == "status":
		var request struct {
			Status string `json:"status"`
		}
		if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
			respondError(w, http.StatusBadRequest, "invalid json body")
			return
		}
		todo, err := s.store.UpdateStatus(id, request.Status)
		if err != nil {
			respondStoreError(w, err)
			return
		}
		respondJSON(w, http.StatusOK, todo)
	default:
		respondError(w, http.StatusNotFound, "endpoint not found")
	}
}

func (s *Server) serveStatic(w http.ResponseWriter, r *http.Request) {
	staticDir := s.staticDir
	if staticDir == "" {
		staticDir = "../frontend/dist"
	}

	if _, err := os.Stat(staticDir); err != nil {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("todo-app Go API is running. Build frontend with `npm run build` to serve the SPA."))
		return
	}

	filePath := filepath.Join(staticDir, filepath.Clean(r.URL.Path))
	if stat, err := os.Stat(filePath); err == nil && !stat.IsDir() {
		http.FileServer(http.Dir(staticDir)).ServeHTTP(w, r)
		return
	}

	indexPath := filepath.Join(staticDir, "index.html")
	if _, err := os.Stat(indexPath); errors.Is(err, fs.ErrNotExist) {
		respondError(w, http.StatusNotFound, "index.html not found")
		return
	}
	http.ServeFile(w, r, indexPath)
}

func todoFilters(r *http.Request) domain.TodoFilters {
	assigneeID, _ := strconv.Atoi(r.URL.Query().Get("assigneeId"))
	return domain.TodoFilters{
		Query:      strings.TrimSpace(r.URL.Query().Get("q")),
		Status:     strings.TrimSpace(r.URL.Query().Get("status")),
		AssigneeID: assigneeID,
	}
}

func parseTodoPath(path string) (int, string, bool) {
	parts := strings.Split(strings.TrimPrefix(path, "/api/todos/"), "/")
	if len(parts) == 0 || parts[0] == "" {
		return 0, "", false
	}
	id, err := strconv.Atoi(parts[0])
	if err != nil {
		return 0, "", false
	}
	if len(parts) == 1 {
		return id, "", true
	}
	if len(parts) == 2 {
		return id, parts[1], true
	}
	return 0, "", false
}

func respondJSON(w http.ResponseWriter, status int, value any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(value)
}

func respondStoreError(w http.ResponseWriter, err error) {
	if errors.Is(err, store.ErrNotFound) {
		respondError(w, http.StatusNotFound, "resource not found")
		return
	}
	respondError(w, http.StatusBadRequest, err.Error())
}

func respondError(w http.ResponseWriter, status int, message string) {
	respondJSON(w, status, map[string]string{"error": message})
}
