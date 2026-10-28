// ui.js - Componentes de interfaz (Modales, Toasts, Ripple, Sidebar, Dropdown)

// =========================================================
// 1. WAVE RIPPLE (Efecto de luz)
// =========================================================
function createWaveRipple(e, element) {
    const rect = element.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const size = Math.max(rect.width, rect.height) * 1.2;
    const ripple = document.createElement('div');
    ripple.className = 'wave-ripple';
    ripple.style.width = size + 'px';
    ripple.style.height = size + 'px';
    ripple.style.left = (x - size / 2) + 'px';
    ripple.style.top = (y - size / 2) + 'px';
    element.appendChild(ripple);
    setTimeout(() => ripple.remove(), 600);
}

// =========================================================
// 2. TOASTS (Notificaciones flotantes)
// =========================================================
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
// 3. SIDEBAR (Menú lateral en móviles)
// =========================================================
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (!sidebar || !overlay) return;

    if (sidebar.classList.contains('open')) { closeSidebar(); return; }
    sidebar.classList.add('open');
    overlay.style.display = 'block';
    requestAnimationFrame(() => overlay.classList.add('visible'));
}

function closeSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.remove('open');
    const overlay = document.getElementById('sidebarOverlay');
    if (overlay) {
        overlay.classList.remove('visible');
        setTimeout(() => overlay.style.display = 'none', 250);
    }
}

// =========================================================
// 4. DROPDOWN (Selector de registros por página)
// =========================================================
function togglePageSizeDropdown(event) {
    event.stopPropagation();
    const menu = document.getElementById('pageSizeMenu');
    if (!menu) return;
    menu.classList.toggle('open');
}

function closePageSizeDropdown() {
    const menu = document.getElementById('pageSizeMenu');
    if (menu) menu.classList.remove('open');
}

// =========================================================
// 5. MODALES (Confirmación de eliminación y anulación)
// =========================================================
function configurarBotonConfirmar(btnConfirmar, url) {
    btnConfirmar.removeAttribute('disabled');
    btnConfirmar.setAttribute('hx-post', url);
    btnConfirmar.setAttribute('hx-target', '#id-main');
    btnConfirmar.setAttribute('hx-disabled-elt', 'this');
    if (typeof htmx !== 'undefined') htmx.process(btnConfirmar);
}

function setTextoConfirmacion(prefijo, valor, sufijo) {
    const texto = document.getElementById('modalConfirmarTexto');
    if (!texto) return;
    texto.textContent = '';
    const strong = document.createElement('strong');
    strong.textContent = valor;
    texto.append(prefijo, strong, sufijo);
}

function abrirModalEliminacion(nombreRegistro, urlDelete) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (!modal || !btnConfirmar) return;

    if (titulo) titulo.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ¿Confirmar Eliminación?`;
    setTextoConfirmacion('¿Está seguro de que desea eliminar ', nombreRegistro, '? Esta acción no se puede deshacer.');
    btnConfirmar.innerHTML = `<i class="fas fa-trash-alt"></i> Confirmar y Eliminar`;
    configurarBotonConfirmar(btnConfirmar, urlDelete);
    modal.classList.add('active');
}

function abrirModalAnularVenta(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (!modal || !btnConfirmar) return;

    if (titulo) titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Venta / Factura?`;
    setTextoConfirmacion('¿Está seguro de anular la factura ', nroFactura, '? Se repondrá automáticamente el stock de los productos.');
    btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
    configurarBotonConfirmar(btnConfirmar, urlAnular);
    modal.classList.add('active');
}

function abrirModalAnularCompra(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');
    if (!modal || !btnConfirmar) return;

    if (titulo) titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Compra / Factura?`;
    setTextoConfirmacion('¿Está seguro de anular la compra ', nroFactura, '? El stock de los productos será devuelto al inventario.');
    btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
    configurarBotonConfirmar(btnConfirmar, urlAnular);
    modal.classList.add('active');
}

function cerrarModalEliminacion() {
    const modal = document.getElementById('modalConfirmarEliminacion');
    if (modal) modal.classList.remove('active');
    const btn = document.getElementById('btnConfirmarEliminar');
    if (btn) {
        btn.removeAttribute('disabled');
        btn.removeAttribute('hx-post');
        btn.removeAttribute('hx-disabled-elt');
    }
}

// =========================================================
// 6. EVENTOS GLOBALES (Se registran cuando el DOM está listo)
// =========================================================
document.addEventListener('DOMContentLoaded', function() {
    // Wave ripple en click (aquí estaba el error de la línea 21)
    document.body.addEventListener('click', function(e) {
        const selector = '.nav-item, .nav-sub-item, .sidebar-icon-btn, .btn-primary, .btn-add, .btn-ghost, .navbar-btn, .filter-chip, .row-action-btn';
        const target = e.target.closest(selector);
        if (target) createWaveRipple(e, target);
    });

    // Cerrar dropdown al hacer clic afuera
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.page-size-dropdown')) closePageSizeDropdown();
    });
});