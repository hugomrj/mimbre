// main.js - Lógica central (Tema, Paleta, Navegación)

// =========================================================
// 1. TEMA (Oscuro / Claro)
// =========================================================
function applyTheme(theme, save = true) {
    const efectivo = theme === 'auto'
        ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
        : theme;

    document.body.setAttribute('data-theme', efectivo);
    document.querySelectorAll('[data-theme-option]').forEach(item => {
        item.classList.toggle('active', item.getAttribute('data-theme-option') === theme);
    });
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
    document.querySelectorAll('.palette-grid [data-palette]').forEach(card => {
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
    const alias = {
        '/ui/proveedores/form': '/ui/proveedores/list',
        '/ui/clientes/form': '/ui/clientes/list',
        '/ui/productos/form': '/ui/productos/list',
        '/ui/productos/movimientos': '/ui/productos/list',
        '/ui/categorias/form': '/ui/categorias/list',
    '/ui/movimientos': '/ui/movimientos',
        '/ui/ventas/form': '/ui/ventas/list',
        '/ui/ventas/detalle': '/ui/ventas/list',
        '/ui/compras/form': '/ui/compras/list',
        '/ui/compras/detalle': '/ui/compras/list'
    };
    const currentPath = alias[window.location.pathname] || window.location.pathname;
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
// =========================================================
// 5. BUSCADOR GLOBAL DE LA TABLA ACTUAL
// =========================================================
const TABLA_SEARCH_URLS = {
    '/ui/productos/list': '/ui/productos/list',
    '/ui/productos/form': '/ui/productos/list',
    '/ui/productos/movimientos': '/ui/productos/list',
    '/ui/categorias/list': '/ui/categorias/list',
    '/ui/categorias/form': '/ui/categorias/list',
    '/ui/movimientos': '/ui/movimientos',
    '/ui/proveedores/list': '/ui/proveedores/list',
    '/ui/proveedores/form': '/ui/proveedores/list',
    '/ui/clientes/list': '/ui/clientes/list',
    '/ui/clientes/form': '/ui/clientes/list',
    '/ui/ventas/list': '/ui/ventas/list',
    '/ui/ventas/form': '/ui/ventas/list',
    '/ui/ventas/detalle': '/ui/ventas/list',
    '/ui/compras/list': '/ui/compras/list',
    '/ui/compras/form': '/ui/compras/list',
    '/ui/compras/detalle': '/ui/compras/list'
};

let buscadorTablaTimer = null;
let buscadorTablaSolicitud = 0;

function inicializarBuscadorTabla() {
    const input = document.getElementById('globalSearch');
    if (!input) return;

    const qActual = new URLSearchParams(window.location.search).get('q') || '';
    input.value = qActual;

    const limpiar = document.getElementById('globalSearchClear');
    actualizarBotonLimpiarBuscador();

    input.addEventListener('input', function() {
        actualizarBotonLimpiarBuscador();
        clearTimeout(buscadorTablaTimer);
        buscadorTablaTimer = setTimeout(() => buscarEnTablaActual(input.value.trim()), 300);
    });

    if (limpiar) {
        limpiar.addEventListener('click', function() {
            input.value = '';
            actualizarBotonLimpiarBuscador();
            clearTimeout(buscadorTablaTimer);
            buscarEnTablaActual('');
            input.focus();
        });
    }
}

function actualizarBotonLimpiarBuscador() {
    const input = document.getElementById('globalSearch');
    const boton = document.getElementById('globalSearchClear');
    if (input && boton) {
        boton.hidden = input.value.trim().length === 0;
    }
}

async function buscarEnTablaActual(termino) {
    const urlBase = TABLA_SEARCH_URLS[window.location.pathname];
    if (!urlBase || typeof htmx === 'undefined') return;

    const url = termino ? `${urlBase}?q=${encodeURIComponent(termino)}` : urlBase;
    const solicitud = ++buscadorTablaSolicitud;

    try {
        const respuesta = await fetch(url, { headers: { 'HX-Request': 'true' } });
        if (!respuesta.ok || solicitud !== buscadorTablaSolicitud) return;

        const html = await respuesta.text();
        if (solicitud !== buscadorTablaSolicitud) return;
        const doc = new DOMParser().parseFromString(html, 'text/html');
        const mainNuevo = doc.querySelector('#id-main');
        const mainActual = document.getElementById('id-main');
        if (!mainActual) return;

        // Las tablas completas traen #id-main; ventas/compras devuelven el fragmento.
        mainActual.innerHTML = mainNuevo ? mainNuevo.innerHTML : doc.body.innerHTML;
        const titulo = mainActual.querySelector('[data-page-title]');
        if (titulo) {
            const navbarTitle = document.getElementById('navbarTitle');
            if (navbarTitle) navbarTitle.textContent = titulo.getAttribute('data-page-title');
        }
        htmx.process(mainActual);
        history.replaceState(null, '', url);
    } catch (error) {
        console.error('No se pudo filtrar la tabla', error);
    }
}

document.addEventListener('DOMContentLoaded', inicializarBuscadorTabla);


// =========================================================
// 6. CAMPOS DE PRECIO CON SEPARADOR DE MILES
// =========================================================
function formatearCampoMiles(valor) {
    const digitos = String(valor).replace(/\D/g, '');
    return digitos.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

document.addEventListener('input', function(e) {
    if (e.target.classList && e.target.classList.contains('money-input')) {
        e.target.value = formatearCampoMiles(e.target.value);
    }
});

document.addEventListener('submit', function(e) {
    if (e.target && e.target.id === 'formProducto') {
        e.target.querySelectorAll('.money-input').forEach(input => {
            input.value = input.value.replace(/\D/g, '');
        });
    }
}, true);


// =========================================================
// 7. FILTROS DE MOVIMIENTOS DE STOCK
// =========================================================
function filtrarMovimientos(tipo, boton) {
    document.querySelectorAll('.mov-filtros .filter-chip').forEach(b => b.classList.remove('active'));
    boton.classList.add('active');

    const filas = document.querySelectorAll('#tablaMovimientos tbody tr[data-tipo]');
    let visibles = 0;
    filas.forEach(fila => {
        const mostrar = tipo === 'TODOS' || fila.dataset.tipo === tipo;
        fila.style.display = mostrar ? '' : 'none';
        if (mostrar) visibles++;
    });

    const vacio = document.getElementById('movimientosVacioFiltro');
    if (vacio) vacio.style.display = (filas.length > 0 && visibles === 0) ? '' : 'none';
}


// Si el tema está en automático, sigue los cambios del sistema.
if (window.matchMedia) {
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function() {
        if (localStorage.getItem('mimbre-theme') === 'auto') applyTheme('auto', false);
    });
}


// =========================================================
// 8. AJUSTE DE STOCK
// =========================================================
function abrirModalAjuste(boton) {
    const modal = document.getElementById('modalAjusteStock');
    if (!modal) return;
    document.getElementById('ajusteProductoId').value = boton.dataset.id || '';
    document.getElementById('ajusteProductoNombre').textContent = boton.dataset.nombre || '';
    document.getElementById('ajusteStockActual').textContent = boton.dataset.stock || '0';
    document.getElementById('ajusteCantidad').value = '';
    document.getElementById('ajusteMotivo').value = '';
    modal.classList.add('active');
    setTimeout(() => document.getElementById('ajusteCantidad').focus(), 100);
}

function cerrarModalAjuste() {
    const modal = document.getElementById('modalAjusteStock');
    if (modal) modal.classList.remove('active');
}


// =========================================================
// 9. PRESETS DE FECHA EN REPORTES
// =========================================================
function aplicarPresetReporte(preset) {
    const form = document.getElementById('formFiltroReporte');
    if (!form) return;

    const hoy = new Date();
    const desde = new Date(hoy);
    if (preset === 'semana') desde.setDate(desde.getDate() - 6);
    if (preset === 'mes') desde.setDate(1);
    if (preset === 'ano') desde.setMonth(0, 1);

    const iso = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    form.desde.value = iso(desde);
    form.hasta.value = iso(hoy);
    form.submit();
}


// =========================================================
// 10. MODAL DE BORRADO DE BASE
// =========================================================
function abrirModalBorrarDb() {
    const modal = document.getElementById('modalBorrarDb');
    if (modal) {
        const input = document.getElementById('confirmacionBorrar');
        if (input) input.value = '';
        modal.classList.add('active');
    }
}

function cerrarModalBorrarDb() {
    const modal = document.getElementById('modalBorrarDb');
    if (modal) modal.classList.remove('active');
}
