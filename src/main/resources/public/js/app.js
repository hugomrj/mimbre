// app.js - Punto de entrada de Mimbre
// Este archivo NO define lógica, solo orquesta el arranque.

document.addEventListener('DOMContentLoaded', () => {
    // 1. Preferencias del usuario (tema + paleta)
    const savedTheme = localStorage.getItem('mimbre-theme') || 'dark';
    const savedPalette = localStorage.getItem('mimbre-palette') || 'cyan';
    applyTheme(savedTheme, false);
    applyPalette(savedPalette, false);

    // 2. Sincronizar el menú lateral con la URL actual
    syncActiveMenuWithUrl();

    // 3. Conectar los eventos globales de HTMX
    setupHtmxListeners();

    // 4. Conectar los modales y teclas globales
    setupGlobalListeners();

    console.log('✅ Mimbre inicializado');
});

function setupHtmxListeners() {
    document.body.addEventListener('htmx:afterSettle', function(evt) {
        if (evt.target.querySelector('#formVenta') && typeof VentaManager !== 'undefined') VentaManager.reset();
        if (evt.target.querySelector('#formCompra') && typeof CompraManager !== 'undefined') CompraManager.reset();
        syncActiveMenuWithUrl();
    });

    document.body.addEventListener('htmx:afterRequest', function(evt) {
        if (evt.detail.elt && evt.detail.elt.id === 'btnConfirmarEliminar') {
            cerrarModalEliminacion();
        }
    });
}

function setupGlobalListeners() {
    // Cerrar modales al hacer clic afuera
    document.addEventListener('click', function(e) {
        const modalElim = document.getElementById('modalConfirmarEliminacion');
        if (modalElim && e.target === modalElim) cerrarModalEliminacion();
        const modalConfig = document.getElementById('modalConfiguracion');
        if (modalConfig && e.target === modalConfig) closeSettingsModal();
    });

    // ESC para cerrar todo
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            cerrarModalEliminacion();
            closeSettingsModal();
            if (typeof closePageSizeDropdown === 'function') closePageSizeDropdown();
        }
    });
}