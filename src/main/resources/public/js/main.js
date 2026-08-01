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

        // 1.b. Inicialización para el Formulario de Compras
        if (evt.target.querySelector('#formCompra')) {
            if (typeof window.CompraManager !== 'undefined') {
                window.CompraManager.reset();
            }
        }
    });

    // Clic fuera del contenedor del modal de eliminación para cerrarlo
    document.addEventListener('click', function(e) {
        const modal = document.getElementById('modalConfirmarEliminacion');
        if (modal && e.target === modal) {
            cerrarModalEliminacion();
        }
    });

    // Tecla ESC para cerrar modal de eliminación
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            cerrarModalEliminacion();
        }
    });
});

// =========================================================
// FUNCIONES GLOBALES DE UI (Extraídas de main.html)
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

function showToast(msg, type = 'info') {
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    
    // Determinar el icono según el tipo
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
    }, 3000); // Aumenté el tiempo visible a 3 segundos para que se lean mejor
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
        if (titulo) {
            titulo.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ¿Confirmar Eliminación?`;
        }
        texto.innerHTML = `¿Está seguro de que desea eliminar <strong>${nombreRegistro}</strong>? Esta acción no se puede deshacer.`;
        
        btnConfirmar.innerHTML = `<i class="fas fa-trash-alt"></i> Confirmar y Eliminar`;
        btnConfirmar.setAttribute('hx-post', urlDelete);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        
        // Procesar el botón con HTMX para que reconozca hx-post
        if (typeof htmx !== 'undefined') {
            htmx.process(btnConfirmar);
        }

        modal.classList.add('active');
    }
}

function abrirModalAnularVenta(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const texto = document.getElementById('modalConfirmarTexto');
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');

    if (modal && texto && btnConfirmar) {
        if (titulo) {
            titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Venta / Factura?`;
        }
        texto.innerHTML = `¿Está seguro de anular la factura <strong>${nroFactura}</strong>? Se repondrá automáticamente el stock de los productos.`;
        
        btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
        btnConfirmar.setAttribute('hx-post', urlAnular);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        
        if (typeof htmx !== 'undefined') {
            htmx.process(btnConfirmar);
        }

        modal.classList.add('active');
    }
}

function abrirModalAnularCompra(nroFactura, urlAnular) {
    const modal = document.getElementById('modalConfirmarEliminacion');
    const titulo = modal ? modal.querySelector('.modal-title') : null;
    const texto = document.getElementById('modalConfirmarTexto');
    const btnConfirmar = document.getElementById('btnConfirmarEliminar');

    if (modal && texto && btnConfirmar) {
        if (titulo) {
            titulo.innerHTML = `<i class="fas fa-ban"></i> ¿Anular Compra / Factura?`;
        }
        texto.innerHTML = `¿Está seguro de anular la compra <strong>${nroFactura}</strong>? El stock de los productos sera devuelto al inventario.`;
        
        btnConfirmar.innerHTML = `<i class="fas fa-ban"></i> Confirmar y Anular`;
        btnConfirmar.setAttribute('hx-post', urlAnular);
        btnConfirmar.setAttribute('hx-target', '#id-main');
        
        if (typeof htmx !== 'undefined') {
            htmx.process(btnConfirmar);
        }

        modal.classList.add('active');
    }
}

function cerrarModalEliminacion() {
    const modal = document.getElementById('modalConfirmarEliminacion');
    if (modal) {
        modal.classList.remove('active');
    }
}
