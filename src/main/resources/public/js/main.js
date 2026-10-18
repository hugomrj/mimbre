// main.js - Lógica global de la aplicación y manejadores de eventos HTMX

document.addEventListener('DOMContentLoaded', () => {
    // 1. Cargar preferencias guardadas de Tema y Color
    const savedTheme = localStorage.getItem('mimbre-theme') || 'dark';
    const savedColor = localStorage.getItem('mimbre-primary-color') || '#06B6D4';

    applyTheme(savedTheme, false);
    applyPrimaryColor(savedColor, false);
    renderColorPresets();

    // Escuchar el evento de HTMX que se dispara después de que el nuevo contenido
    // se ha inyectado en el DOM y las transiciones han terminado.
    document.body.addEventListener('htmx:afterSettle', function(evt) {
        if (evt.target.querySelector('#formVenta')) {
            if (typeof window.VentaManager !== 'undefined') window.VentaManager.reset();
        }
        if (evt.target.querySelector('#formCompra')) {
            if (typeof window.CompraManager !== 'undefined') window.CompraManager.reset();
        }
    });

    // Clic fuera del contenedor de los modales para cerrarlos
    document.addEventListener('click', function(e) {
        const modalElim = document.getElementById('modalConfirmarEliminacion');
        if (modalElim && e.target === modalElim) cerrarModalEliminacion();

        const modalConfig = document.getElementById('modalConfiguracion');
        if (modalConfig && e.target === modalConfig) closeSettingsModal();
    });

    // Tecla ESC para cerrar modales
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            cerrarModalEliminacion();
            closeSettingsModal();
        }
    });
});

// =========================================================
// FUNCIONES DE TEMA Y COLOR PRIMARIO (CON PERSISTENCIA)
// =========================================================
function applyTheme(theme, save = true) {
    document.body.setAttribute('data-theme', theme);
    const icon = document.getElementById('theme-icon');
    if (icon) {
        icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
    if (save) localStorage.setItem('mimbre-theme', theme);
}

function toggleTheme() {
    const currentTheme = document.body.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    applyTheme(newTheme);
}

function applyPrimaryColor(color, save = true) {
    // Cambia la variable CSS global instantáneamente
    document.documentElement.style.setProperty('--primary', color);

    // Sincroniza el input de color del modal si existe
    const colorInput = document.getElementById('primaryColorPicker');
    if (colorInput) colorInput.value = color;

    if (save) localStorage.setItem('mimbre-primary-color', color);
}

function openSettingsModal() {
    const modal = document.getElementById('modalConfiguracion');
    if (modal) modal.classList.add('active');
}

function closeSettingsModal() {
    const modal = document.getElementById('modalConfiguracion');
    if (modal) modal.classList.remove('active');
}

// Colores predefinidos para el selector rápido
const presetColors = ['#06B6D4', '#0EA5E9', '#8B5CF6', '#EC4899', '#10B981', '#F59E0B', '#EF4444', '#6366F1'];

function renderColorPresets() {
    const container = document.getElementById('colorPresetsContainer');
    if (!container) return;

    container.innerHTML = presetColors.map(color => `
        <button class="color-preset-btn" style="background-color: ${color};"
                onclick="applyPrimaryColor('${color}')" title="${color}">
        </button>
    `).join('');
}

// =========================================================
// FUNCIONES GLOBALES DE UI
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
    e.preventDefault();

    // Remover active de todos los items
    document.querySelectorAll('.nav-item.active, .nav-sub-item.active').forEach(i => {
        i.classList.remove('active');
    });

    // Agregar active al elemento clickeado
    el.classList.add('active');

    // ✨ NUEVO: Si es un sub-item, también marcar su padre como activo
    if (el.classList.contains('nav-sub-item')) {
        const navSub = el.closest('.nav-sub');
        if (navSub) {
            const parentNav = navSub.previousElementSibling;
            if (parentNav && parentNav.classList.contains('nav-item')) {
                parentNav.classList.add('active');
            }
        }
    }

    // Actualizar título del navbar
    const label = el.querySelector('.nav-label, span:last-child');
    if (label) document.getElementById('navbarTitle').textContent = label.textContent;

    // Cerrar sidebar en móvil
    if (window.innerWidth <= 768) closeSidebar();
}

function setFilter(el) {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
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

function showToast(msg, type = 'info') {
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    let iconClass = 'fas fa-info-circle';
    if (type === 'success') iconClass = 'fas fa-check-circle';
    if (type === 'error') iconClass = 'fas fa-times-circle';
    if (type === 'warning') iconClass = 'fas fa-exclamation-triangle';
    t.innerHTML = `<i class="${iconClass}"></i> <span>${msg}</span>`;
    document.getElementById('toastContainer').appendChild(t);
    requestAnimationFrame(() => { t.style.transform = 'translateX(0)'; t.style.opacity = '1'; });
    setTimeout(() => {
        t.style.transform = 'translateX(120%)';
        t.style.opacity = '0';
        setTimeout(() => t.remove(), 350);
    }, 3000);
}

// =========================================================
// MODAL GLOBAL DE CONFIRMACIÓN DE ELIMINACIÓN Y ANULACIÓN
// =========================================================
function abrirModalEliminacion(nombreRegistro, urlDelete) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const texto = document.getElementById('modalConfirmarTexto');
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (modal && texto && btnConfirmar) {
        if (titulo) titulo.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ¿Confirmar Eliminación?`;
        texto.innerHTML = `¿Está seguro de que desea eliminar <strong>${nombreRegistro}</strong>? Esta acción no se puede deshacer.`;
        btnConfirmar.innerHTML = `<i class="fas fa-trash-alt"></i> Confirmar y Eliminar`;
        btnConfirmar.setAttribute('hx-post', urlDelete);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        if (typeof htmx !== 'undefined') htmx.process(btnConfirmar);
        modal.classList.add('active');
    }
}

function abrirModalAnularVenta(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const texto = document.getElementById('modalConfirmarTexto');
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (modal && texto && btnConfirmar) {
        if (titulo) titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Venta / Factura?`;
        texto.innerHTML = `¿Está seguro de anular la factura <strong>${nroFactura}</strong>? Se repondrá automáticamente el stock de los productos.`;
        btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
        btnConfirmar.setAttribute('hx-post', urlAnular);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        if (typeof htmx !== 'undefined') htmx.process(btnConfirmar);
        modal.classList.add('active');
    }
}

function abrirModalAnularCompra(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const texto = document.getElementById('modalConfirmarTexto');
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (modal && texto && btnConfirmar) {
        if (titulo) titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Compra / Factura?`;
        texto.innerHTML = `¿Está seguro de anular la compra <strong>${nroFactura}</strong>? El stock de los productos sera devuelto al inventario.`;
        btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
        btnConfirmar.setAttribute('hx-post', urlAnular);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        if (typeof htmx !== 'undefined') htmx.process(btnConfirmar);
        modal.classList.add('active');
    }
}

function cerrarModalEliminacion() {
    const modal = document.getElementById('modalConfirmarEliminacion');
    if (modal) modal.classList.remove('active');
}