// main.js - Lógica global de la aplicación y manejadores de eventos HTMX

document.addEventListener('DOMContentLoaded', () => {
    // Escuchar el evento de HTMX que se dispara después de que el nuevo contenido 
    // se ha inyectado en el DOM y las transiciones han terminado.
    document.body.addEventListener('htmx:afterSettle', function(evt) {
        
        // 1. Inicialización para el Formulario de Ventas
        // Si el contenedor objetivo contiene el formulario de venta, reseteamos el estado.
        if (evt.target.querySelector('#formVenta')) {
            if (typeof window.VentaManager !== 'undefined') {
                window.VentaManager.reset();
            }
        }

        // Aquí puedes agregar inicializaciones para otros módulos en el futuro.
        // if (evt.target.querySelector('#formCotizacion')) { ... }
    });
});

// =========================================================
// FUNCIONES GLOBALES DE UI (Extraídas de main.html)
// =========================================================

function toggleSubMenuAcc(btn) {
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
    e.preventDefault();
    document.querySelectorAll('.nav-item.active, .nav-sub-item.active').forEach(i => i.classList.remove('active'));
    el.classList.add('active');
    const label = el.querySelector('.nav-label, span:last-child');
    if (label) document.getElementById('navbarTitle').textContent = label.textContent;
    if (window.innerWidth <= 768) closeSidebar();
}

function setFilter(el) {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
}

function toggleTheme() {
    const isDark = document.body.getAttribute('data-theme') === 'dark';
    document.body.setAttribute('data-theme', isDark ? 'light' : 'dark');
    document.getElementById('theme-icon').className = isDark ? 'fas fa-sun' : 'fas fa-moon';
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (sidebar.classList.contains('open')) { closeSidebar(); return; }
    sidebar.classList.add('open');
    overlay.style.display = 'block';
    requestAnimationFrame(() => overlay.classList.add('visible'));
}

function closeSidebar() {
    document.getElementById('sidebar').classList.remove('open');
    const overlay = document.getElementById('sidebarOverlay');
    overlay.classList.remove('visible');
    setTimeout(() => overlay.style.display = 'none', 250);
}

function showToast(msg) {
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = msg;
    document.getElementById('toastContainer').appendChild(t);
    requestAnimationFrame(() => { t.style.transform = 'translateX(0)'; t.style.opacity = '1'; });
    setTimeout(() => { 
        t.style.transform = 'translateX(120%)'; 
        t.style.opacity = '0'; 
        setTimeout(() => t.remove(), 350); 
    }, 2500);
}
