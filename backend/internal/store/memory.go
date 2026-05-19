package store

import (
	"errors"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/imnoob012/todo-app/backend/internal/domain"
)

var ErrNotFound = errors.New("not found")

type MemoryStore struct {
	mu              sync.RWMutex
	members         []domain.Member
	todos           []domain.Todo
	nextCommentID   int
	nextHistoryID   int
	currentUsername string
}

func NewMemoryStore() *MemoryStore {
	now := time.Now().In(time.Local)
	completedAt := now.Add(-48 * time.Hour)

	return &MemoryStore{
		currentUsername: "admin",
		nextCommentID:   4,
		nextHistoryID:   5,
		members: []domain.Member{
			{ID: 1, Username: "admin", Email: "admin@example.com", Role: domain.RoleAdmin, RoleName: "管理者", Remarks: "システム全体を管理するアカウント"},
			{ID: 2, Username: "taro_yamada", Email: "taro.yamada@example.com", Role: domain.RoleMember, RoleName: "メンバー", Remarks: "バックエンド開発担当"},
			{ID: 3, Username: "hanako_sato", Email: "hanako.sato@example.com", Role: domain.RoleTodoAdmin, RoleName: "TODO管理者", Remarks: "UI/UXとタスク進行管理を担当"},
		},
		todos: []domain.Todo{
			{
				ID:             1,
				Title:          "要件定義書の作成",
				Assignees:      []domain.MemberSummary{{ID: 2, Username: "taro_yamada"}},
				Priority:       domain.Priority{ID: 1, Label: "高"},
				DueDate:        now.AddDate(0, 0, 5).Format(time.DateOnly),
				Classification: domain.Classification{Code: "DOCS", Label: "ドキュメント"},
				Status:         domain.Status{Code: "IN_PROGRESS", Label: "進行中"},
				Description:    "新規プロジェクトの要件定義を完了させる",
				Comments: []domain.Comment{
					{ID: 1, TodoID: 1, Author: "taro_yamada", Body: "要件の粒度を画面単位で整理中です。", CreatedAt: now.Add(-4 * time.Hour)},
				},
				Histories: []domain.History{
					{ID: 1, TodoID: 1, Field: "ステータス", BeforeValue: "未着手", AfterValue: "進行中", ChangedBy: "taro_yamada", ChangedAt: now.Add(-4 * time.Hour)},
				},
			},
			{
				ID:             2,
				Title:          "ログイン体験の見直し",
				Assignees:      []domain.MemberSummary{{ID: 2, Username: "taro_yamada"}, {ID: 3, Username: "hanako_sato"}},
				Priority:       domain.Priority{ID: 2, Label: "中"},
				DueDate:        now.AddDate(0, 0, 10).Format(time.DateOnly),
				Classification: domain.Classification{Code: "FEATURE", Label: "機能開発"},
				Status:         domain.Status{Code: "TODO", Label: "未着手"},
				Description:    "認証後の導線と権限ごとの表示を整理する",
			},
			{
				ID:             3,
				Title:          "トップ画面のモック作成",
				Assignees:      []domain.MemberSummary{{ID: 3, Username: "hanako_sato"}},
				Priority:       domain.Priority{ID: 2, Label: "中"},
				DueDate:        now.AddDate(0, 0, 7).Format(time.DateOnly),
				Classification: domain.Classification{Code: "OTHER", Label: "その他"},
				Status:         domain.Status{Code: "DONE", Label: "完了"},
				CompletedAt:    &completedAt,
				Description:    "Figmaを使用してデザイン案を2パターン作成する",
				Comments: []domain.Comment{
					{ID: 2, TodoID: 3, Author: "hanako_sato", Body: "モックは初版レビュー済みです。", CreatedAt: now.Add(-48 * time.Hour)},
				},
				Histories: []domain.History{
					{ID: 2, TodoID: 3, Field: "ステータス", BeforeValue: "進行中", AfterValue: "完了", ChangedBy: "hanako_sato", ChangedAt: now.Add(-48 * time.Hour)},
				},
			},
			{
				ID:             4,
				Title:          "備品購入",
				Assignees:      []domain.MemberSummary{{ID: 2, Username: "taro_yamada"}},
				Priority:       domain.Priority{ID: 3, Label: "低"},
				DueDate:        now.AddDate(0, 0, 30).Format(time.DateOnly),
				Classification: domain.Classification{Code: "OTHER", Label: "その他"},
				Status:         domain.Status{Code: "TODO", Label: "未着手"},
				Description:    "ディスプレイ用のケーブルを購入する",
			},
		},
	}
}

func (s *MemoryStore) CurrentMember() domain.Member {
	s.mu.RLock()
	defer s.mu.RUnlock()

	for _, member := range s.members {
		if member.Username == s.currentUsername {
			return member
		}
	}
	return domain.Member{}
}

func (s *MemoryStore) Members() []domain.Member {
	s.mu.RLock()
	defer s.mu.RUnlock()

	return append([]domain.Member(nil), s.members...)
}

func (s *MemoryStore) Todos(filters domain.TodoFilters) []domain.Todo {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]domain.Todo, 0, len(s.todos))
	for _, todo := range s.todos {
		if !matchesTodo(todo, filters) {
			continue
		}
		result = append(result, cloneTodo(todo))
	}
	sort.Slice(result, func(i, j int) bool {
		return result[i].ID < result[j].ID
	})
	return result
}

func (s *MemoryStore) Todo(id int) (domain.Todo, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	for _, todo := range s.todos {
		if todo.ID == id {
			return cloneTodo(todo), nil
		}
	}
	return domain.Todo{}, ErrNotFound
}

func (s *MemoryStore) AddComment(todoID int, body string) (domain.Todo, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	body = strings.TrimSpace(body)
	if body == "" {
		return domain.Todo{}, errors.New("comment body is required")
	}

	for i := range s.todos {
		if s.todos[i].ID != todoID {
			continue
		}
		comment := domain.Comment{
			ID:        s.nextCommentID,
			TodoID:    todoID,
			Author:    s.currentUsername,
			Body:      body,
			CreatedAt: time.Now().In(time.Local),
		}
		s.nextCommentID++
		s.todos[i].Comments = append(s.todos[i].Comments, comment)
		s.addHistoryLocked(i, "コメント", "-", body)
		return cloneTodo(s.todos[i]), nil
	}
	return domain.Todo{}, ErrNotFound
}

func (s *MemoryStore) UpdateStatus(todoID int, statusCode string) (domain.Todo, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	nextStatus, ok := statusByCode(statusCode)
	if !ok {
		return domain.Todo{}, errors.New("unknown status")
	}

	for i := range s.todos {
		if s.todos[i].ID != todoID {
			continue
		}
		before := s.todos[i].Status.Label
		if before == nextStatus.Label {
			return cloneTodo(s.todos[i]), nil
		}

		s.todos[i].Status = nextStatus
		if nextStatus.Code == "DONE" {
			now := time.Now().In(time.Local)
			s.todos[i].CompletedAt = &now
		} else {
			s.todos[i].CompletedAt = nil
		}
		s.addHistoryLocked(i, "ステータス", before, nextStatus.Label)
		return cloneTodo(s.todos[i]), nil
	}
	return domain.Todo{}, ErrNotFound
}

func (s *MemoryStore) Dashboard() domain.Dashboard {
	s.mu.RLock()
	defer s.mu.RUnlock()

	now := time.Now().In(time.Local)
	weekEnd := now.AddDate(0, 0, 7)
	summary := domain.DashboardSummary{Total: len(s.todos)}
	statusCounts := map[string]int{}
	priorityCounts := map[string]int{}
	classCounts := map[string]int{}
	assigneeCounts := map[string]int{}

	for _, todo := range s.todos {
		if todo.Status.Code == "DONE" {
			summary.Done++
		}
		if todo.Status.Code == "IN_PROGRESS" {
			summary.InProgress++
		}

		dueDate, err := time.Parse(time.DateOnly, todo.DueDate)
		if err == nil && todo.Status.Code != "DONE" {
			if dueDate.Before(dateOnly(now)) {
				summary.Overdue++
			}
			if !dueDate.Before(dateOnly(now)) && !dueDate.After(dateOnly(weekEnd)) {
				summary.DueThisWeek++
			}
		}

		statusCounts[todo.Status.Label]++
		priorityCounts[todo.Priority.Label]++
		classCounts[todo.Classification.Label]++
		if len(todo.Assignees) == 0 {
			assigneeCounts["未担当"]++
		}
		for _, assignee := range todo.Assignees {
			assigneeCounts[assignee.Username]++
		}
	}

	if summary.Total > 0 {
		summary.ProgressRate = float64(summary.Done) * 100 / float64(summary.Total)
	}

	return domain.Dashboard{
		Summary:        summary,
		StatusCounts:   orderedCounts(statusCounts, []string{"未着手", "進行中", "完了"}),
		PriorityCounts: orderedCounts(priorityCounts, []string{"高", "中", "低"}),
		AssigneeCounts: sortedCounts(assigneeCounts),
		ClassCounts:    sortedCounts(classCounts),
	}
}

func (s *MemoryStore) addHistoryLocked(todoIndex int, field, before, after string) {
	history := domain.History{
		ID:          s.nextHistoryID,
		TodoID:      s.todos[todoIndex].ID,
		Field:       field,
		BeforeValue: before,
		AfterValue:  after,
		ChangedBy:   s.currentUsername,
		ChangedAt:   time.Now().In(time.Local),
	}
	s.nextHistoryID++
	s.todos[todoIndex].Histories = append([]domain.History{history}, s.todos[todoIndex].Histories...)
}

func matchesTodo(todo domain.Todo, filters domain.TodoFilters) bool {
	if filters.Status != "" && todo.Status.Code != filters.Status {
		return false
	}
	if filters.AssigneeID > 0 {
		found := false
		for _, assignee := range todo.Assignees {
			if assignee.ID == filters.AssigneeID {
				found = true
				break
			}
		}
		if !found {
			return false
		}
	}
	if filters.Query == "" {
		return true
	}
	query := strings.ToLower(filters.Query)
	haystacks := []string{
		todo.Title,
		todo.Description,
		todo.Status.Label,
		todo.Priority.Label,
		todo.Classification.Label,
	}
	for _, assignee := range todo.Assignees {
		haystacks = append(haystacks, assignee.Username)
	}
	for _, haystack := range haystacks {
		if strings.Contains(strings.ToLower(haystack), query) {
			return true
		}
	}
	return false
}

func statusByCode(code string) (domain.Status, bool) {
	statuses := map[string]domain.Status{
		"TODO":        {Code: "TODO", Label: "未着手"},
		"IN_PROGRESS": {Code: "IN_PROGRESS", Label: "進行中"},
		"DONE":        {Code: "DONE", Label: "完了"},
	}
	status, ok := statuses[code]
	return status, ok
}

func cloneTodo(todo domain.Todo) domain.Todo {
	todo.Assignees = append([]domain.MemberSummary(nil), todo.Assignees...)
	todo.Comments = append([]domain.Comment(nil), todo.Comments...)
	todo.Histories = append([]domain.History(nil), todo.Histories...)
	return todo
}

func orderedCounts(source map[string]int, order []string) []domain.CountItem {
	result := make([]domain.CountItem, 0, len(source))
	used := map[string]bool{}
	for _, label := range order {
		if count, ok := source[label]; ok {
			result = append(result, domain.CountItem{Label: label, Count: count})
			used[label] = true
		}
	}
	for label, count := range source {
		if !used[label] {
			result = append(result, domain.CountItem{Label: label, Count: count})
		}
	}
	return result
}

func sortedCounts(source map[string]int) []domain.CountItem {
	result := make([]domain.CountItem, 0, len(source))
	for label, count := range source {
		result = append(result, domain.CountItem{Label: label, Count: count})
	}
	sort.Slice(result, func(i, j int) bool {
		if result[i].Count == result[j].Count {
			return result[i].Label < result[j].Label
		}
		return result[i].Count > result[j].Count
	})
	return result
}

func dateOnly(value time.Time) time.Time {
	return time.Date(value.Year(), value.Month(), value.Day(), 0, 0, 0, 0, value.Location())
}
