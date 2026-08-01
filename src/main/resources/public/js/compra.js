// Estado del módulo de compras
const CompraManager = {
itemsCompra: [],
productoTemporal: null,

seleccionarProveedor: function(id, label) {
const selectProveedor = document.getElementById('selectProveedor');
const buscarProveedorInput = document.getElementById('buscarProveedorInput');
const proveedorResultados = document.getElementById('proveedorResultados');

if (selectProveedor && buscarProveedorInput && proveedorResultados) {
selectProveedor.value = id;
buscarProveedorInput.value = label;
proveedorResultados.innerHTML = '';
}
},

seleccionarProducto: function(id, nombre, precio, stock) {
const buscarProductoInput = document.getElementById('buscarProductoInputCompra');
const productoResultados = document.getElementById('productoResultadosCompra');

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
const modal = document.getElementById('modalProductoCompra');
if (modal) {
modal.classList.add('active');
const input = document.getElementById('buscarProductoInputCompra');
if (input) {
setTimeout(() => input.focus(), 150);
}
}
},

cerrarModalProducto: function() {
const modal = document.getElementById('modalProductoCompra');
if (modal) {
modal.classList.remove('active');
}
const buscarInput = document.getElementById('buscarProductoInputCompra');
const prodResultados = document.getElementById('productoResultadosCompra');
const cantInput = document.getElementById('inputCantidadCompra');

if (buscarInput) buscarInput.value = '';
if (prodResultados) prodResultados.innerHTML = '';
if (cantInput) cantInput.value = '1';
this.productoTemporal = null;
},

agregarItemDesdeInput: function(continuar = false) {
const cantInput = document.getElementById('inputCantidadCompra');

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

const existente = this.itemsCompra.find(i => i.id === prod.id);
if (existente) {
existente.cantidad += cantidad;
existente.subtotal = existente.cantidad * existente.precio;
} else {
this.itemsCompra.push({
id: prod.id,
nombre: prod.nombre,
precio: prod.precio,
cantidad: cantidad,
subtotal: cantidad * prod.precio
});
}

this.renderizarTablaItems();

// Limpiar para el siguiente producto
if (cantInput) cantInput.value = 1;
const buscarInput = document.getElementById('buscarProductoInputCompra');
if (buscarInput) buscarInput.value = '';
this.productoTemporal = null;

if (continuar) {
if (buscarInput) buscarInput.focus();
} else {
this.cerrarModalProducto();
}
},

eliminarItem: function(index) {
this.itemsCompra.splice(index, 1);
this.renderizarTablaItems();
},

renderizarTablaItems: function() {
const tbody = document.getElementById('tbodyItemsCompra');
const inputsDiv = document.getElementById('inputsOcultosDetalle');
const totalLabel = document.getElementById('labelTotalCompra');

if (!tbody || !inputsDiv || !totalLabel) return;

tbody.innerHTML = '';
inputsDiv.innerHTML = '';

if (this.itemsCompra.length === 0) {
tbody.innerHTML = '<tr id="rowVacia"><td colspan="5" style="text-align: center; padding: 24px; color: var(--dark-text-muted);">No se han agregado productos a la compra.</td></tr>';
totalLabel.textContent = '₲ 0';
return;
}

let total = 0;

this.itemsCompra.forEach((item, idx) => {
total += item.subtotal;

const tr = document.createElement('tr');
tr.innerHTML = `
<td><strong>${item.nombre}</strong></td>
<td style="text-align: center;">${item.cantidad}</td>
<td style="text-align: right;">₲ ${item.precio.toLocaleString('es-PY')}</td>
<td style="text-align: right;"><strong>₲ ${item.subtotal.toLocaleString('es-PY')}</strong></td>
<td style="text-align: center;">
<button type="button" class="row-action-btn" onclick="CompraManager.eliminarItem(${idx})">
<i class="fas fa-trash-alt" style="color: #F43F5E;"></i>
</button>
</td>
`;
tbody.appendChild(tr);

inputsDiv.innerHTML += `
<input type="hidden" name="productoId" value="${item.id}">
<input type="hidden" name="cantidad" value="${item.cantidad}">
<input type="hidden" name="precioCosto" value="${item.precio}">
`;
});

totalLabel.textContent = '₲ ' + total.toLocaleString('es-PY');
},

validarCompra: function() {
const selectProveedor = document.getElementById('selectProveedor');
const proveedorId = selectProveedor ? selectProveedor.value : '';

if (!proveedorId) {
this.showToast('Seleccione un proveedor válido de la lista de sugerencias', 'warning');
return false;
}

if (this.itemsCompra.length === 0) {
this.showToast('Agregue al menos un producto a la compra', 'warning');
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
this.itemsCompra = [];
this.productoTemporal = null;
this.cerrarModalProducto();
}
};

// Configurar los event listeners globales necesarios
document.addEventListener('DOMContentLoaded', () => {
// Interceptar el envío del formulario de compras por HTMX antes de hacer el POST
document.body.addEventListener('htmx:beforeRequest', function(evt) {
if (evt.target && evt.target.id === 'formCompra') {
if (typeof CompraManager !== 'undefined' && !CompraManager.validarCompra()) {
evt.preventDefault();
}
}
});

// Ocultar resultados si se hace clic fuera (para proveedores y productos)
document.addEventListener('click', function(e) {
// Proveedores
const contProveedor = document.getElementById('proveedorResultados');
const inputProveedor = document.getElementById('buscarProveedorInput');
if (contProveedor && inputProveedor && !contProveedor.contains(e.target) && e.target !== inputProveedor) {
contProveedor.innerHTML = '';
}

// Productos (en el modal)
const contProd = document.getElementById('productoResultadosCompra');
const inputProd = document.getElementById('buscarProductoInputCompra');
if (contProd && inputProd && !contProd.contains(e.target) && e.target !== inputProd) {
contProd.innerHTML = '';
}

// Clic fuera del contenedor del modal para cerrarlo
const modal = document.getElementById('modalProductoCompra');
if (modal && e.target === modal) {
CompraManager.cerrarModalProducto();
}
});

// Tecla ESC para cerrar modal
document.addEventListener('keydown', function(e) {
if (e.key === 'Escape') {
const modal = document.getElementById('modalProductoCompra');
if (modal && modal.classList.contains('active')) {
CompraManager.cerrarModalProducto();
}
}
});
});
