package domain

import "time"

type Role string

const (
	RoleAdmin     Role = "ADMIN"
	RoleTodoAdmin Role = "TODO_ADMIN"
	RoleMember    Role = "MEMBER"
)

type Member struct {
	ID       int    `json:"id"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Role     Role   `json:"role"`
	RoleName string `json:"roleName"`
	Remarks  string `json:"remarks"`
}

type MemberSummary struct {
	ID       int    `json:"id"`
	Username string `json:"username"`
}

type Priority struct {
	ID    int    `json:"id"`
	Label string `json:"label"`
}

type Classification struct {
	Code  string `json:"code"`
	Label string `json:"label"`
}

type Status struct {
	Code  string `json:"code"`
	Label string `json:"label"`
}

type Todo struct {
	ID             int             `json:"id"`
	Title          string          `json:"title"`
	Assignees      []MemberSummary `json:"assignees"`
	Priority       Priority        `json:"priority"`
	DueDate        string          `json:"dueDate"`
	Classification Classification  `json:"classification"`
	Status         Status          `json:"status"`
	CompletedAt    *time.Time      `json:"completedAt,omitempty"`
	Description    string          `json:"description"`
	Comments       []Comment       `json:"comments,omitempty"`
	Histories      []History       `json:"histories,omitempty"`
}

type Comment struct {
	ID        int       `json:"id"`
	TodoID    int       `json:"todoId"`
	Author    string    `json:"author"`
	Body      string    `json:"body"`
	CreatedAt time.Time `json:"createdAt"`
}

type History struct {
	ID          int       `json:"id"`
	TodoID      int       `json:"todoId"`
	Field       string    `json:"field"`
	BeforeValue string    `json:"beforeValue"`
	AfterValue  string    `json:"afterValue"`
	ChangedBy   string    `json:"changedBy"`
	ChangedAt   time.Time `json:"changedAt"`
}

type Dashboard struct {
	Summary        DashboardSummary `json:"summary"`
	StatusCounts   []CountItem      `json:"statusCounts"`
	PriorityCounts []CountItem      `json:"priorityCounts"`
	AssigneeCounts []CountItem      `json:"assigneeCounts"`
	ClassCounts    []CountItem      `json:"classCounts"`
}

type DashboardSummary struct {
	Total        int     `json:"total"`
	Overdue      int     `json:"overdue"`
	DueThisWeek  int     `json:"dueThisWeek"`
	InProgress   int     `json:"inProgress"`
	Done         int     `json:"done"`
	ProgressRate float64 `json:"progressRate"`
}

type CountItem struct {
	Label string `json:"label"`
	Count int    `json:"count"`
}

type TodoFilters struct {
	Query      string
	Status     string
	AssigneeID int
}
