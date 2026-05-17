document.addEventListener('DOMContentLoaded', () => {
    const tbody = document.getElementById('member-table-body');
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
            fetchMembers();
        });
    });

    searchInputs.forEach(input => {
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchMembers();
            }
        });
    });

    function fetchMembers() {
        const params = new URLSearchParams();
        searchInputs.forEach(input => {
            const value = input.value.trim();
            if (input.dataset.field && value) {
                params.set(input.dataset.field, value);
            }
        });
        if (currentSort) {
            params.set('sort', currentSort);
            params.set('direction', currentDirection);
        }

        fetch('/api/members?' + params.toString())
            .then(res => res.json())
            .then(members => renderTable(members))
            .catch(err => console.error('検索エラー:', err));
    }

    function renderTable(members) {
        tbody.replaceChildren();
        members.forEach(member => {
            const row = document.createElement('tr');

            const idCell = document.createElement('td');
            const detailLink = document.createElement('a');
            detailLink.href = '/members/' + encodeURIComponent(member.id);
            detailLink.textContent = String(member.id ?? '').padStart(10, '0');
            idCell.appendChild(detailLink);
            row.appendChild(idCell);

            appendTextCell(row, member.username || '');
            appendTextCell(row, member.email || '');
            appendTextCell(row, member.role ? member.role.label : '');
            appendTextCell(row, member.remarks || '');

            tbody.appendChild(row);
        });
    }

    function appendTextCell(row, value) {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.appendChild(cell);
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
