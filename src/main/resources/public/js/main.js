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

    // Cerrar otros submenús abiertos
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
    // Nota: Ya no hacemos e.preventDefault() porque HTMX maneja el link y la URL.
    // Solo actualizamos el título del navbar de forma inmediata (fallback).
    const label = el.querySelector('.nav-label, span:last-child');
    if (label) {
        const titleElement = document.getElementById('navbarTitle');
        if (titleElement) titleElement.textContent = label.textContent;
    }

    // Cerrar sidebar en móvil
    if (window.innerWidth <= 768 && typeof closeSidebar === 'function') {
        closeSidebar();
    }
}

// ✅ Sincroniza el menú con la URL actual (Soluciona el bug del F5)
function syncActiveMenuWithUrl() {
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll('.nav-item, .nav-sub-item');

    // Remover active de todos
    menuLinks.forEach(link => link.classList.remove('active'));

    // Encontrar el link que coincide con la URL
    let activeLink = null;
    menuLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && href !== '#' && currentPath.startsWith(href)) {
            link.classList.add('active');
            activeLink = link;
        }
    });

    // Si es un sub-item, marcar también el padre como activo
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