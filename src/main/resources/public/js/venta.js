// Estado del módulo de ventas
const VentaManager = {
    itemsVenta: [],
    productoTemporal: null,

    seleccionarCliente: function(id, label) {
        const selectCliente = document.getElementById('selectCliente');
        const buscarClienteInput = document.getElementById('buscarClienteInput');
        const clienteResultados = document.getElementById('clienteResultados');

        if (selectCliente && buscarClienteInput && clienteResultados) {
            selectCliente.value = id;
            buscarClienteInput.value = label;
            clienteResultados.innerHTML = '';
        }
    },

    seleccionarProductoDesdeSelect: function(select) {
        const opt = select.options[select.selectedIndex];
        if (!opt || !opt.value) {
            this.productoTemporal = null;
            return;
        }
        this.productoTemporal = {
            id: Number(opt.value),
            nombre: opt.dataset.nombre,
            precio: Number(opt.dataset.precioVenta),
            stock: Number(opt.dataset.stock)
        };
    },

    abrirModalProducto: function() {
        const modal = document.getElementById('modalProducto');
        if (modal) {
            modal.classList.add('active');
        }
    },

    cerrarModalProducto: function() {
        const modal = document.getElementById('modalProducto');
        if (modal) {
            modal.classList.remove('active');
        }
        const cantInput = document.getElementById('inputCantidad');

        if (cantInput) cantInput.value = '1';
        if (typeof resetVanillaSelect === 'function') resetVanillaSelect('selectProductoVenta');
        this.productoTemporal = null;
    },

    agregarItemDesdeInput: function(continuar = false) {
        const cantInput = document.getElementById('inputCantidad');

        if (!this.productoTemporal) {
            this.showToast('Busque y seleccione un producto de la lista primero', 'warning');
            return;
        }

        const cantidad = parseInt(cantInput.value) || 1;
        const prod = this.productoTemporal;

        if (cantidad <= 0) {
            this.showToast('Ingrese una cantidad mayor a 0', 'error');
            return;
        }

        if (cantidad > prod.stock) {
            this.showToast(`Stock insuficiente. Disponible: ${prod.stock}`, 'error');
            return;
        }

        const existente = this.itemsVenta.find(i => i.id === prod.id);
        if (existente) {
            if (existente.cantidad + cantidad > prod.stock) {
                this.showToast(`Supera el stock disponible (${prod.stock})`, 'error');
                return;
            }
            existente.cantidad += cantidad;
            existente.subtotal = existente.cantidad * existente.precio;
        } else {
            this.itemsVenta.push({
                id: prod.id,
                nombre: prod.nombre,
                precio: prod.precio,
                cantidad: cantidad,
                subtotal: cantidad * prod.precio
            });
        }

        this.renderizarTablaItems();
        this.showToast(`"${prod.nombre}" agregado a la venta`, 'success');

        if (cantInput) cantInput.value = 1;
        if (continuar) {
            // Se mantiene el producto elegido para seguir agregando.
        } else {
            this.cerrarModalProducto();
        }
    },

    eliminarItem: function(index) {
        this.itemsVenta.splice(index, 1);
        this.renderizarTablaItems();
    },

    renderizarTablaItems: function() {
        const tbody = document.getElementById('tbodyItemsVenta');
        const inputsDiv = document.getElementById('inputsOcultosDetalle');
        const totalLabel = document.getElementById('labelTotalVenta');

        if (!tbody || !inputsDiv || !totalLabel) return;

        tbody.innerHTML = '';
        inputsDiv.innerHTML = '';

        if (this.itemsVenta.length === 0) {
            tbody.innerHTML = '<tr id="rowVacia"><td colspan="5" style="text-align: center; padding: 24px; color: var(--dark-text-muted);">No se han agregado productos a la venta.</td></tr>';
            totalLabel.textContent = '0';
            return;
        }

        this.itemsVenta.forEach((item, idx) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${item.nombre}</strong></td>
                <td style="text-align: center;">${item.cantidad}</td>
                <td style="text-align: right;">
                    <input type="number" name="precioUnitario" value="${item.precio}" required
                           min="0" step="0.01" class="form-control"
                           style="width: 130px; text-align: right; padding: 6px 8px; margin-left: auto;"
                           oninput="VentaManager.cambiarPrecio(${idx}, this.value)">
                </td>
                <td style="text-align: right;" id="subtotalVenta-${idx}"><strong>${this.formatearMiles(item.subtotal)}</strong></td>
                <td style="text-align: center;">
                    <button type="button" class="row-action-btn" onclick="VentaManager.eliminarItem(${idx})">
                        <i class="fas fa-trash-alt" style="color: #F43F5E;"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);

            inputsDiv.innerHTML += `
                <input type="hidden" name="productoId" value="${item.id}">
                <input type="hidden" name="cantidad" value="${item.cantidad}">
            `;
        });

        this.actualizarTotal();
    },

    cambiarPrecio: function(index, valor) {
        const item = this.itemsVenta[index];
        if (!item) return;
        const precio = Number(valor);
        item.precio = Number.isFinite(precio) ? precio : 0;
        item.subtotal = item.cantidad * item.precio;
        const celdaSubtotal = document.getElementById(`subtotalVenta-${index}`);
        if (celdaSubtotal) {
            celdaSubtotal.innerHTML = '<strong>' + this.formatearMiles(item.subtotal) + '</strong>';
        }
        this.actualizarTotal();
    },

    formatearMiles: function(valor) {
        return Math.round(Number(valor) || 0).toLocaleString('es-PY');
    },

    actualizarTotal: function() {
        const total = this.itemsVenta.reduce((suma, item) => suma + item.subtotal, 0);
        const label = document.getElementById('labelTotalVenta');
        if (label) label.textContent = this.formatearMiles(total);
    },

    validarVenta: function() {
        const selectCliente = document.getElementById('selectCliente');
        const clienteId = selectCliente ? selectCliente.value : '';

        if (!clienteId) {
            this.showToast('Seleccione un cliente', 'warning');
            return false;
        }

        if (this.itemsVenta.length === 0) {
            this.showToast('Agregue al menos un producto a la venta', 'warning');
            return false;
        }

        return true;
    },

    showToast: function(message, type = 'error') {
        if (typeof window.showToast === 'function') {
            window.showToast(message, type);
        } else {
            alert(message);
        }
    },
    
    // Método para inicializar/limpiar el estado cuando se carga el formulario de nuevo
    reset: function() {
        this.itemsVenta = [];
        this.productoTemporal = null;
        this.cerrarModalProducto();
    }
};

// Configurar los event listeners globales necesarios
document.addEventListener('DOMContentLoaded', () => {
    // Interceptar el envío del formulario de ventas por HTMX antes de hacer el POST
    document.body.addEventListener('htmx:beforeRequest', function(evt) {
        if (evt.target && evt.target.id === 'formVenta') {
            if (typeof VentaManager !== 'undefined' && !VentaManager.validarVenta()) {
                evt.preventDefault();
            }
        }
    });

    // Ocultar resultados si se hace clic fuera (para clientes y productos)
    document.addEventListener('click', function(e) {
        // Clientes
        const contCliente = document.getElementById('clienteResultados');
        const inputCliente = document.getElementById('buscarClienteInput');
        if (contCliente && inputCliente && !contCliente.contains(e.target) && e.target !== inputCliente) {
            contCliente.innerHTML = '';
        }
        
        // Productos (en el modal)
        const contProd = document.getElementById('productoResultados');
        const inputProd = document.getElementById('buscarProductoInput');
        if (contProd && inputProd && !contProd.contains(e.target) && e.target !== inputProd) {
            contProd.innerHTML = '';
        }

        // Clic fuera del contenedor del modal para cerrarlo
        const modal = document.getElementById('modalProducto');
        if (modal && e.target === modal) {
            VentaManager.cerrarModalProducto();
        }
    });

    // Tecla ESC para cerrar modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const modal = document.getElementById('modalProducto');
            if (modal && modal.classList.contains('active')) {
                VentaManager.cerrarModalProducto();
            }
        }
    });
});
