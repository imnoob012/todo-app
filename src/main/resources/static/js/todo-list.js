document.addEventListener('DOMContentLoaded', () => {
    const tbody = document.getElementById('todo-table-body');
    const searchInputs = document.querySelectorAll('.search-input');

    let currentSort = null;
    let currentDirection = 'asc';

    document.querySelectorAll('th[data-sort]').forEach(th => {
        th.style.cursor = 'pointer';
        th.addEventListener('click', () => {
            const sort = th.dataset.sort;
            if (currentSort === sort) {
                currentDirection = currentDirection === 'asc' ? 'desc' : 'asc';
            } else {
                currentSort = sort;
                currentDirection = 'asc';
            }
            updateSortMarks();
            fetchTodos();
        });
    });

    searchInputs.forEach(input => {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchTodos();
            }
        });
    });

    function fetchTodos() {
        const params = new URLSearchParams();
        searchInputs.forEach(input => {
            const value = input.value.trim();
            if (input.dataset.field && value) {
                params.set(input.dataset.field, value);
            }
        });

        const urlParams = new URLSearchParams(window.location.search);
        const assigneeId = urlParams.get('assigneeId');
        if (assigneeId) {
            params.set('assigneeId', assigneeId);
        }

        if (currentSort) {
            params.set('sort', currentSort);
            params.set('direction', currentDirection);
        }

        fetch('/api/todos?' + params.toString())
            .then(res => res.json())
            .then(todos => renderTable(todos))
            .catch(err => console.error('検索エラー:', err));
    }

    function renderTable(todos) {
        tbody.replaceChildren();
        todos.forEach(todo => {
            const row = document.createElement('tr');

            const idCell = document.createElement('td');
            const detailLink = document.createElement('a');
            detailLink.href = '/todos/' + encodeURIComponent(todo.id);
            detailLink.textContent = String(todo.id ?? '').padStart(10, '0');
            idCell.appendChild(detailLink);
            row.appendChild(idCell);

            appendTextCell(row, todo.title || '');
            appendTextCell(row, assigneeText(todo.assignees));
            appendTextCell(row, todo.status ? todo.status.label : '');
            appendTextCell(row, todo.priority ? todo.priority.label : '');
            appendTextCell(row, todo.dueDate || '');
            appendTextCell(row, todo.classification ? todo.classification.label : '');
            appendTextCell(row, todo.description || '');

            tbody.appendChild(row);
        });
    }

    function appendTextCell(row, value) {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
    }

    function assigneeText(assignees) {
        if (!assignees || assignees.length === 0) {
            return '未定';
        }
        return assignees.map(assignee => assignee.username).join(', ');
    }

    function updateSortMarks() {
        document.querySelectorAll('th[data-sort]').forEach(th => {
            const mark = th.querySelector('.sort-mark');
            if (mark) mark.remove();
            if (th.dataset.sort === currentSort) {
                const span = document.createElement('span');
                span.className = 'sort-mark';
                span.textContent = currentDirection === 'asc' ? '▲' : '▼';
                th.appendChild(span);
            }
        });
    }
});
