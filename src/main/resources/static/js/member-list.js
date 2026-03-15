document.addEventListener('DOMContentLoaded', () => {
	
	const tbody = document.getElementById('member-table-body');
	const searchInputs = document.querySelectorAll('.search-input');
	
	// パスワード列の表示制御に使用する為、ログインユーザーがADMIN権限かどうかを判定（<body data-is-admin="true"> から取得）
	const isAdmin = document.body.dataset.isAdmin === 'true';
	
	let currentSort = null;
	let currentDirection = 'asc';
	
	// sort
	document.querySelectorAll('th[data-sort]').forEach(th => {
			// マウスカーソルを指マークにする
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
	
	// 検索（Enter押下）
	searchInputs.forEach(input => {
	    input.addEventListener('keydown', (e) => {
	        if (e.key === 'Enter') {
	            e.preventDefault();
	            fetchMembers();
	        }
	    });
	});

	// API呼び出し
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

	// テーブル描画
	function renderTable(members) {
	    tbody.innerHTML = '';
	    members.forEach(member => {
	        const row = document.createElement('tr');
	        row.innerHTML = `
	            <td><a href="/members/${member.id}">${String(member.id).padStart(10, '0')}</a></td>
	            <td>${member.username || ''}</td>
	            <td>${member.email || ''}</td>
	            <td>${isAdmin ? (member.password || '') : '*******'}</td>
	            <td>${member.role ? member.role.label : ''}</td>
	            <td>${member.remarks || ''}</td>
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