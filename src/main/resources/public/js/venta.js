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

    seleccionarProducto: function(id, nombre, precio, stock) {
        const buscarProductoInput = document.getElementById('buscarProductoInput');
        const productoResultados = document.getElementById('productoResultados');
        
        if (buscarProductoInput && productoResultados) {
            buscarProductoInput.value = nombre;
            productoResultados.innerHTML = '';
            
            // Guardamos el producto temporalmente hasta que le dé a "Agregar"
            this.productoTemporal = {
                id: id,
                nombre: nombre,
                precio: precio,
                stock: stock
            };
        }
    },

    abrirModalProducto: function() {
        const modal = document.getElementById('modalProducto');
        if (modal) {
            modal.classList.add('active');
            const input = document.getElementById('buscarProductoInput');
            if (input) {
                setTimeout(() => input.focus(), 150);
            }
        }
    },

    cerrarModalProducto: function() {
        const modal = document.getElementById('modalProducto');
        if (modal) {
            modal.classList.remove('active');
        }
        const buscarInput = document.getElementById('buscarProductoInput');
        const prodResultados = document.getElementById('productoResultados');
        const cantInput = document.getElementById('inputCantidad');

        if (buscarInput) buscarInput.value = '';
        if (prodResultados) prodResultados.innerHTML = '';
        if (cantInput) cantInput.value = '1';
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
        
        // Limpiar para el siguiente producto
        if (cantInput) cantInput.value = 1;
        const buscarInput = document.getElementById('buscarProductoInput');
        if (buscarInput) buscarInput.value = '';
        this.productoTemporal = null;

        if (continuar) {
            if (buscarInput) buscarInput.focus();
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
            totalLabel.textContent = '₲ 0';
            return;
        }

        let total = 0;

        this.itemsVenta.forEach((item, idx) => {
            total += item.subtotal;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${item.nombre}</strong></td>
                <td style="text-align: center;">${item.cantidad}</td>
                <td style="text-align: right;">₲ ${item.precio.toLocaleString('es-PY')}</td>
                <td style="text-align: right;"><strong>₲ ${item.subtotal.toLocaleString('es-PY')}</strong></td>
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
                <input type="hidden" name="precioUnitario" value="${item.precio}">
            `;
        });

        totalLabel.textContent = '₲ ' + total.toLocaleString('es-PY');
    },

    validarVenta: function() {
        const selectCliente = document.getElementById('selectCliente');
        const clienteId = selectCliente ? selectCliente.value : '';

        if (!clienteId) {
            this.showToast('Seleccione un cliente válido de la lista de sugerencias', 'warning');
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
