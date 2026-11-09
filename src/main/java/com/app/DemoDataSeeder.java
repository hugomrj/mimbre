package com.app;

import com.app.cliente.model.Cliente;
import com.app.cliente.repository.ClienteRepository;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto_categoria.model.ProductoCategoria;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
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
            productoCategoriaRepository.save(new ProductoCategoria("Ropa", "Vestimenta y prendas de vestir"));
            productoCategoriaRepository.save(new ProductoCategoria("Hogar", "Artículos para el hogar y decoración"));
            productoCategoriaRepository.save(new ProductoCategoria("Alimentos", "Productos alimenticios y bebidas"));
            productoCategoriaRepository.save(new ProductoCategoria("Accesorios", "Accesorios variados"));
            productoCategoriaRepository.save(new ProductoCategoria("Deportes", "Equipamiento y ropa deportiva"));
            productoCategoriaRepository.save(new ProductoCategoria("Juguetes", "Juguetes y juegos para niños"));
            productoCategoriaRepository.save(new ProductoCategoria("Libros", "Libros, revistas y material educativo"));
            productoCategoriaRepository.save(new ProductoCategoria("Mascotas", "Alimentos y accesorios para mascotas"));
            productoCategoriaRepository.save(new ProductoCategoria("Belleza", "Cuidado personal y cosméticos"));
        }

        if (proveedorRepository.count() == 0) {
            proveedorRepository.save(new Proveedor("Distribuidora Global S.A.", "80012345-1", "021-555-0101", "contacto@global.com", "Av. Principal 123"));
            proveedorRepository.save(new Proveedor("TechImport SRL", "80098765-2", "021-555-0202", "ventas@techimport.com", "Calle Industria 456"));
            proveedorRepository.save(new Proveedor("Comercial del Este", "80045678-3", "021-555-0303", "info@comercialeste.com", "Ruta 2 Km 10"));
            proveedorRepository.save(new Proveedor("Distribuidora del Sur", "80011223-4", "071-555-0404", "contacto@delsur.com.py", "Av. Artigas 789"));
            proveedorRepository.save(new Proveedor("Importadora Central", "80044556-5", "021-555-0505", "compras@central.com", "Palma 101"));
            proveedorRepository.save(new Proveedor("Textiles Paraguay", "80077889-6", "051-555-0606", "ventas@textiles.py", "Ruta 1 Km 25"));
            proveedorRepository.save(new Proveedor("Alimentos del Campo", "80033445-7", "021-555-0707", "pedidos@campo.com", "Av. Eusebio Ayala 1234"));
            proveedorRepository.save(new Proveedor("Logística Express", "80055667-8", "021-555-0808", "logistica@express.com", "Calle Colón 567"));
            proveedorRepository.save(new Proveedor("Belleza Total", "80088990-9", "021-555-0909", "info@bellezatotal.com", "Shopping del Sol"));
            proveedorRepository.save(new Proveedor("Juguetes Mundo", "80022334-0", "021-555-1010", "ventas@juguetesmundo.com", "Av. Mcal. López 890"));
        }

        if (clienteRepository.count() == 0) {
            clienteRepository.save(new Cliente("Juan Pérez", "4567890-1", "0981-111-222", "juan.perez@email.com", "Av. Mariscal López 123"));
            clienteRepository.save(new Cliente("María González", "3456789-2", "0982-333-444", "maria.gonzalez@email.com", "Calle Palma 456"));
            clienteRepository.save(new Cliente("Empresa San José S.A.", "80055566-7", "021-444-555", "contacto@sanjose.com.py", "Av. España 789"));
            clienteRepository.save(new Cliente("Carlos López", "4123456-3", "0983-555-666", "carlos.lopez@email.com", "Av. Brasil 101"));
            clienteRepository.save(new Cliente("Ana Benítez", "3987654-4", "0984-777-888", "ana.benitez@email.com", "Calle Chile 202"));
            clienteRepository.save(new Cliente("Pedro Ramírez", "4234567-5", "0985-999-000", "pedro.ramirez@email.com", "Av. Perú 303"));
            clienteRepository.save(new Cliente("Lucía Fernández", "3876543-6", "0986-111-333", "lucia.fernandez@email.com", "Calle México 404"));
            clienteRepository.save(new Cliente("Roberto Giménez", "4345678-7", "0987-222-444", "roberto.gimenez@email.com", "Av. Argentina 505"));
            clienteRepository.save(new Cliente("Sofía Ortiz", "3765432-8", "0988-333-555", "sofia.ortiz@email.com", "Calle Uruguay 606"));
            clienteRepository.save(new Cliente("Diego Martínez", "4456789-9", "0989-444-666", "diego.martinez@email.com", "Av. Colombia 707"));
            clienteRepository.save(new Cliente("Valeria Rojas", "3654321-0", "0991-555-777", "valeria.rojas@email.com", "Calle Venezuela 808"));
            clienteRepository.save(new Cliente("Fernando Cáceres", "4567891-1", "0992-666-888", "fernando.caceres@email.com", "Av. Bolivia 909"));
            clienteRepository.save(new Cliente("Gabriela Mendoza", "3543210-2", "0993-777-999", "gabriela.mendoza@email.com", "Calle Ecuador 111"));
            clienteRepository.save(new Cliente("Oscar Villalba", "4678901-3", "0994-888-000", "oscar.villalba@email.com", "Av. México 222"));
            clienteRepository.save(new Cliente("Natalia Aguilera", "3432109-4", "0995-999-111", "natalia.aguilera@email.com", "Calle Perú 333"));
        }

        if (productoRepository.count() == 0) {
            // --- Electrónica ---
            productoRepository.save(new Producto("Laptop Pro 15", "LAP-001", new BigDecimal("12000000"), 15, "Electrónica"));
            productoRepository.save(new Producto("Mouse Inalámbrico", "MOU-002", new BigDecimal("150000"), 45, "Electrónica"));
            productoRepository.save(new Producto("Teclado Mecánico RGB", "TEC-003", new BigDecimal("450000"), 20, "Electrónica"));
            productoRepository.save(new Producto("Monitor 4K 27\"", "MON-004", new BigDecimal("3500000"), 10, "Electrónica"));
            productoRepository.save(new Producto("Auriculares Bluetooth", "AUD-005", new BigDecimal("350000"), 30, "Electrónica"));
            productoRepository.save(new Producto("Disco SSD 1TB", "SSD-006", new BigDecimal("800000"), 25, "Electrónica"));
            productoRepository.save(new Producto("Smartphone Galaxy S23", "SMA-007", new BigDecimal("5500000"), 8, "Electrónica"));
            productoRepository.save(new Producto("Smart TV 55\"", "TVS-008", new BigDecimal("4200000"), 5, "Electrónica"));
            productoRepository.save(new Producto("Tablet 10\"", "TAB-009", new BigDecimal("2500000"), 12, "Electrónica"));
            productoRepository.save(new Producto("Cargador Rápido USB-C", "CAR-010", new BigDecimal("120000"), 50, "Electrónica"));
            productoRepository.save(new Producto("Cámara Web HD", "CAM-011", new BigDecimal("280000"), 18, "Electrónica"));

            // --- Ropa ---
            productoRepository.save(new Producto("Camisa de Algodón", "CAM-012", new BigDecimal("120000"), 50, "Ropa"));
            productoRepository.save(new Producto("Pantalón Jeans", "PAN-013", new BigDecimal("180000"), 40, "Ropa"));
            productoRepository.save(new Producto("Zapatillas Deportivas", "ZAP-014", new BigDecimal("450000"), 20, "Ropa"));
            productoRepository.save(new Producto("Chaqueta de Cuero", "CHA-015", new BigDecimal("650000"), 10, "Ropa"));
            productoRepository.save(new Producto("Vestido de Verano", "VES-016", new BigDecimal("220000"), 25, "Ropa"));
            productoRepository.save(new Producto("Short Deportivo", "SHO-017", new BigDecimal("85000"), 35, "Ropa"));
            productoRepository.save(new Producto("Gorra Urbana", "GOR-018", new BigDecimal("65000"), 40, "Ropa"));

            // --- Hogar ---
            productoRepository.save(new Producto("Juego de Sábanas", "SAB-019", new BigDecimal("250000"), 15, "Hogar"));
            productoRepository.save(new Producto("Licuadora", "LIC-020", new BigDecimal("320000"), 12, "Hogar"));
            productoRepository.save(new Producto("Microondas", "MIC-021", new BigDecimal("950000"), 7, "Hogar"));
            productoRepository.save(new Producto("Lámpara de Escritorio", "LAM-022", new BigDecimal("80000"), 25, "Hogar"));
            productoRepository.save(new Producto("Set de Ollas Antiadherentes", "OLL-023", new BigDecimal("550000"), 10, "Hogar"));
            productoRepository.save(new Producto("Aspiradora", "ASP-024", new BigDecimal("1200000"), 6, "Hogar"));
            productoRepository.save(new Producto("Cafetera", "CAF-025", new BigDecimal("780000"), 9, "Hogar"));

            // --- Alimentos ---
            productoRepository.save(new Producto("Café Molido 500g", "CAF-026", new BigDecimal("45000"), 100, "Alimentos"));
            productoRepository.save(new Producto("Caja de Té Verde", "TEV-027", new BigDecimal("25000"), 80, "Alimentos"));
            productoRepository.save(new Producto("Pack de Agua Mineral", "AGU-028", new BigDecimal("15000"), 200, "Alimentos"));

            // --- Accesorios ---
            productoRepository.save(new Producto("Reloj Inteligente", "REL-029", new BigDecimal("450000"), 15, "Accesorios"));
            productoRepository.save(new Producto("Mochila para Laptop", "MOC-030", new BigDecimal("220000"), 30, "Accesorios"));
            productoRepository.save(new Producto("Billetera de Cuero", "BIL-031", new BigDecimal("85000"), 25, "Accesorios"));

            // --- Deportes ---
            productoRepository.save(new Producto("Balón de Fútbol", "BAL-032", new BigDecimal("150000"), 30, "Deportes"));
            productoRepository.save(new Producto("Raqueta de Tenis", "RAQ-033", new BigDecimal("650000"), 8, "Deportes"));
            productoRepository.save(new Producto("Mancuernas 5kg", "MAN-034", new BigDecimal("180000"), 15, "Deportes"));
            productoRepository.save(new Producto("Bicicleta MTB", "BIC-035", new BigDecimal("3500000"), 4, "Deportes"));

            // --- Juguetes ---
            productoRepository.save(new Producto("Lego City", "LEG-036", new BigDecimal("350000"), 12, "Juguetes"));
            productoRepository.save(new Producto("Muñeca Barbie", "MUN-037", new BigDecimal("180000"), 20, "Juguetes"));
            productoRepository.save(new Producto("Carro a Control Remoto", "CAR-038", new BigDecimal("250000"), 10, "Juguetes"));
            productoRepository.save(new Producto("Rompecabezas 1000 pzs", "ROM-039", new BigDecimal("120000"), 25, "Juguetes"));

            // --- Libros ---
            productoRepository.save(new Producto("Cien Años de Soledad", "LIB-040", new BigDecimal("85000"), 15, "Libros"));
            productoRepository.save(new Producto("El Principito", "LIB-041", new BigDecimal("55000"), 30, "Libros"));
            productoRepository.save(new Producto("Hábitos Atómicos", "LIB-042", new BigDecimal("110000"), 20, "Libros"));
            productoRepository.save(new Producto("Clean Code", "LIB-043", new BigDecimal("250000"), 5, "Libros"));

            // --- Mascotas ---
            productoRepository.save(new Producto("Alimento Perro 10kg", "MAS-044", new BigDecimal("280000"), 40, "Mascotas"));
            productoRepository.save(new Producto("Arena para Gato 5kg", "MAS-045", new BigDecimal("75000"), 50, "Mascotas"));
            productoRepository.save(new Producto("Juguete Mordedor", "MAS-046", new BigDecimal("35000"), 60, "Mascotas"));

            // --- Belleza ---
            productoRepository.save(new Producto("Crema Hidratante", "BEL-047", new BigDecimal("95000"), 35, "Belleza"));
            productoRepository.save(new Producto("Perfume Floral", "BEL-048", new BigDecimal("450000"), 10, "Belleza"));
            productoRepository.save(new Producto("Set de Maquillaje", "BEL-049", new BigDecimal("320000"), 15, "Belleza"));
        }
    }
}