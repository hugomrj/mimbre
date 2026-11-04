// main.js - Lógica central (Tema, Paleta, Navegación)

// =========================================================
// 1. TEMA (Oscuro / Claro)
// =========================================================
function applyTheme(theme, save = true) {
    document.body.setAttribute('data-theme', theme);
    const icon = document.getElementById('theme-icon');
    if (icon) icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    if (save) localStorage.setItem('mimbre-theme', theme);
}

function toggleTheme() {
    const currentTheme = document.body.getAttribute('data-theme');
    applyTheme(currentTheme === 'dark' ? 'light' : 'dark');
}

// =========================================================
// 2. PALETA DE COLORES
// =========================================================
function applyPalette(paletteName, save = true) {
    document.body.setAttribute('data-palette', paletteName);
    document.querySelectorAll('.palette-item').forEach(card => {
        card.classList.toggle('active', card.getAttribute('data-palette') === paletteName);
    });
    if (save) localStorage.setItem('mimbre-palette', paletteName);
}

function openSettingsModal() {
    const modal = document.getElementById('modalConfiguracion');
    if (modal) modal.classList.add('active');
}

function closeSettingsModal() {
    const modal = document.getElementById('modalConfiguracion');
    if (modal) modal.classList.remove('active');
}

// =========================================================
// 3. NAVEGACIÓN Y MENÚ LATERAL
// =========================================================
function toggleSub(btn) {
    const isOpen = btn.classList.contains('expanded');
    const sub = btn.nextElementSibling;

    document.querySelectorAll('.nav-item.expanded').forEach(el => {
        if (el !== btn) {
            el.classList.remove('expanded');
            const s = el.nextElementSibling;
            if (s && s.classList.contains('nav-sub')) s.classList.remove('open');
        }
    });

    btn.classList.toggle('expanded', !isOpen);
    if (sub) sub.classList.toggle('open', !isOpen);
}

function navTo(el, e) {
    const label = el.querySelector('.nav-label, span:last-child');
    if (label) {
        const titleElement = document.getElementById('navbarTitle');
        if (titleElement) titleElement.textContent = label.textContent;
    }
    if (window.innerWidth <= 768 && typeof closeSidebar === 'function') {
        closeSidebar();
    }
}

// Compara href del menú con la URL actual (evita que "/" matchee todo)
function pathMatches(currentPath, href) {
    if (!href || href === '#') return false;
    const cleanHref = href.split('?')[0];
    const cleanPath = currentPath.split('?')[0];
    if (cleanHref === '/') return cleanPath === '/';
    return cleanPath === cleanHref || cleanPath.startsWith(cleanHref + '/');
}

// ✅ Sincroniza el menú con la URL actual (Soluciona el bug del F5)
function syncActiveMenuWithUrl() {
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll('.nav-item, .nav-sub-item');

    // 1. Cerrar TODOS los submenús primero (esto es lo que faltaba)
    document.querySelectorAll('.nav-sub').forEach(sub => sub.classList.remove('open'));
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('expanded'));

    // 2. Remover 'active' de todos los links
    menuLinks.forEach(link => link.classList.remove('active'));

    // 3. Encontrar el link que coincide con la URL
    let activeLink = null;
    menuLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (pathMatches(currentPath, href)) {
            link.classList.add('active');
            activeLink = link;
        }
    });

    // 4. Si es un sub-item, abrir su padre
    if (activeLink && activeLink.classList.contains('nav-sub-item')) {
        const navSub = activeLink.closest('.nav-sub');
        if (navSub) {
            const parentNav = navSub.previousElementSibling;
            if (parentNav && parentNav.classList.contains('nav-item')) {
                parentNav.classList.add('active');
                parentNav.classList.add('expanded');
                navSub.classList.add('open');
            }
        }
    }
}

// =========================================================
// 4. UTILIDADES DE FILTROS
// =========================================================
function setFilter(el) {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
}