package com.app;

import com.app.cliente.model.Cliente;
import com.app.cliente.ClienteRepository;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto_categoria.ProductoCategoria;
import com.app.producto_categoria.ProductoCategoriaRepository;
import com.app.proveedor.Proveedor;
import com.app.proveedor.ProveedorRepository;
import jakarta.inject.Singleton;

import java.math.BigDecimal;

@Singleton
public class DemoDataSeeder {

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoCategoriaRepository productoCategoriaRepository;

    public DemoDataSeeder(ClienteRepository clienteRepository,
                          ProductoRepository productoRepository,
                          ProveedorRepository proveedorRepository,
                          ProductoCategoriaRepository productoCategoriaRepository) {
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoCategoriaRepository = productoCategoriaRepository;
    }

    public void seed() {
        if (productoCategoriaRepository.count() == 0) {
            productoCategoriaRepository.save(new ProductoCategoria("Electrónica", "Dispositivos y gadgets electrónicos"));
            productoCategoriaRepository.save(new ProductoCategoria("Ropa", "Vestimenta y prendas"));
            productoCategoriaRepository.save(new ProductoCategoria("Hogar", "Artículos para el hogar y decoración"));
            productoCategoriaRepository.save(new ProductoCategoria("Alimentos", "Productos alimenticios y bebidas"));
            productoCategoriaRepository.save(new ProductoCategoria("Accesorios", "Accesorios variados"));
        }

        if (proveedorRepository.count() == 0) {
            proveedorRepository.save(new Proveedor("Distribuidora Global S.A.", "80012345-1", "021-555-0101", "contacto@global.com", "Av. Principal 123"));
            proveedorRepository.save(new Proveedor("TechImport SRL", "80098765-2", "021-555-0202", "ventas@techimport.com", "Calle Industria 456"));
            proveedorRepository.save(new Proveedor("Comercial del Este", "80045678-3", "021-555-0303", "info@comercialeste.com", "Ruta 2 Km 10"));
        }

        if (productoRepository.count() == 0) {
            productoRepository.save(new Producto("Laptop Pro 15", "LAP-001", new BigDecimal("12000000"), 15, "Electrónica"));
            productoRepository.save(new Producto("Mouse Inalámbrico", "MOU-002", new BigDecimal("150000"), 45, "Electrónica"));
            productoRepository.save(new Producto("Teclado Mecánico RGB", "TEC-003", new BigDecimal("450000"), 20, "Electrónica"));
        }

        if (clienteRepository.count() == 0) {
            clienteRepository.save(new Cliente("Juan Pérez", "4567890-1", "0981-111-222", "juan.perez@email.com", "Av. Mariscal López 123"));
            clienteRepository.save(new Cliente("María González", "3456789-2", "0982-333-444", "maria.gonzalez@email.com", "Calle Palma 456"));
            clienteRepository.save(new Cliente("Empresa San José S.A.", "80055566-7", "021-444-555", "contacto@sanjose.com.py", "Av. España 789"));
        }
    }
}
