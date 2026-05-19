export type Member = {
  id: number;
  username: string;
  email: string;
  role: "ADMIN" | "TODO_ADMIN" | "MEMBER";
  roleName: string;
  remarks: string;
};

export type MemberSummary = {
  id: number;
  username: string;
};

export type TodoStatus = {
  code: "TODO" | "IN_PROGRESS" | "DONE";
  label: string;
};

export type Todo = {
  id: number;
  title: string;
  assignees: MemberSummary[];
  priority: {
    id: number;
    label: string;
  };
  dueDate: string;
  classification: {
    code: string;
    label: string;
  };
  status: TodoStatus;
  completedAt?: string;
  description: string;
  comments?: TodoComment[];
  histories?: TodoHistory[];
};

export type TodoComment = {
  id: number;
  todoId: number;
  author: string;
  body: string;
  createdAt: string;
};

export type TodoHistory = {
  id: number;
  todoId: number;
  field: string;
  beforeValue: string;
  afterValue: string;
  changedBy: string;
  changedAt: string;
};

export type Dashboard = {
  summary: {
    total: number;
    overdue: number;
    dueThisWeek: number;
    inProgress: number;
    done: number;
    progressRate: number;
  };
  statusCounts: CountItem[];
  priorityCounts: CountItem[];
  assigneeCounts: CountItem[];
  classCounts: CountItem[];
};

export type CountItem = {
  label: string;
  count: number;
};
