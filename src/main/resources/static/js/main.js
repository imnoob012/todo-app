document.addEventListener('DOMContentLoaded', () => {
    const url = new URL(window.location.href);
    const currentSort = url.searchParams.get('sort');

    const currentDir = url.searchParams.get('direction') || 'asc';

    document.querySelectorAll('th[data-sort]').forEach(th => {
		
        th.style.cursor = 'pointer';
		
		if (th.dataset.sort === currentSort) {
		    const mark = currentDir === 'asc' ? '▲' : '▼';
		    const cleanText = th.textContent.trim(); 
		    th.innerHTML = `${cleanText}<span class="sort-mark">${mark}</span>`;
		}

        th.addEventListener('click', () => {
            const sort = th.dataset.sort;
            const nextDir = (currentSort === sort && currentDir === 'asc') ? 'desc' : 'asc';
            
            url.searchParams.set('sort', sort);
            url.searchParams.set('direction', nextDir);
            
            window.location.href = url.toString();
        });
    });
});