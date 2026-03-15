document.addEventListener('DOMContentLoaded', () => {

    const tbody = document.getElementById('todo-table-body');
    const searchInputs = document.querySelectorAll('.search-input');

    let currentSort = null;
    let currentDirection = 'asc';

    // sort
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

    // 検索（Enter押下）
    searchInputs.forEach(input => {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchTodos();
            }
        });
    });

    // API呼び出し
    function fetchTodos() {
        const params = new URLSearchParams();
        searchInputs.forEach(input => {
            const value = input.value.trim();
            if (input.dataset.field && value) {
                params.set(input.dataset.field, value);
            }
    });

    // メンバー詳細からの遷移時、assigneeIdを引き継ぐ
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

    // テーブル描画
    function renderTable(todos) {
        tbody.innerHTML = '';
        todos.forEach(todo => {
            const assigneeText = (!todo.assignees || todo.assignees.length === 0)
                ? '未定'
                : todo.assignees.map(a => a.username).join(', ');

            const row = document.createElement('tr');
            row.innerHTML = `
                <td><a href="/todos/${todo.id}">${String(todo.id).padStart(10, '0')}</a></td>
                <td>${todo.title || ''}</td>
                <td>${assigneeText}</td>
                <td>${todo.priority ? todo.priority.label : ''}</td>
                <td>${todo.dueDate || ''}</td>
                <td>${todo.classification ? todo.classification.label : ''}</td>
                <td>${todo.description || ''}</td>
            `;
            tbody.appendChild(row);
        });
    }

    // ソートマーク
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
