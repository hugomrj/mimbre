// Estado del módulo de ventas
const VentaManager = {
    itemsVenta: [],

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

    agregarItemDesdeSelect: function() {
        const sel = document.getElementById('selectProducto');
        const cantInput = document.getElementById('inputCantidad');

        if (!sel.value) {
            this.showToast('Seleccione un producto primero');
            return;
        }

        const opt = sel.options[sel.selectedIndex];
        const id = parseInt(sel.value);
        const nombre = opt.getAttribute('data-nombre');
        const precio = parseFloat(opt.getAttribute('data-precio'));
        const stock = parseInt(opt.getAttribute('data-stock'));
        const cantidad = parseInt(cantInput.value) || 1;

        if (cantidad <= 0) {
            this.showToast('Ingrese una cantidad mayor a 0');
            return;
        }

        if (cantidad > stock) {
            this.showToast('Stock insuficiente. Disponible: ' + stock);
            return;
        }

        const existente = this.itemsVenta.find(i => i.id === id);
        if (existente) {
            if (existente.cantidad + cantidad > stock) {
                this.showToast('Supera el stock disponible (' + stock + ')');
                return;
            }
            existente.cantidad += cantidad;
            existente.subtotal = existente.cantidad * existente.precio;
        } else {
            this.itemsVenta.push({
                id: id,
                nombre: nombre,
                precio: precio,
                cantidad: cantidad,
                subtotal: cantidad * precio
            });
        }

        this.renderizarTablaItems();
        cantInput.value = 1;
        sel.value = "";
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
        if (!document.getElementById('selectCliente').value) {
            this.showToast('Seleccione un cliente válido de la lista');
            return false;
        }
        if (this.itemsVenta.length === 0) {
            this.showToast('Agregue al menos un producto a la venta');
            return false;
        }
        return true;
    },

    showToast: function(message) {
        // Asumiendo que existe una función global showToast en tu aplicación (común en este tipo de plantillas)
        // Si no existe, podemos reemplazarla por un alert básico por ahora, pero usaremos la global si existe
        if (typeof window.showToast === 'function') {
            window.showToast(message);
        } else {
            alert(message);
        }
    },
    
    // Método para inicializar/limpiar el estado cuando se carga el formulario de nuevo
    reset: function() {
        this.itemsVenta = [];
    }
};

// Configurar los event listeners globales necesarios
document.addEventListener('DOMContentLoaded', () => {
    // Ocultar resultados de cliente si se hace clic fuera
    document.addEventListener('click', function(e) {
        const container = document.getElementById('clienteResultados');
        const input = document.getElementById('buscarClienteInput');
        if (container && input && !container.contains(e.target) && e.target !== input) {
            container.innerHTML = '';
        }
    });
});
