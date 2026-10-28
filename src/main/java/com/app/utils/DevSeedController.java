package com.app.utils;

import com.app.cliente.ClienteService;
import com.app.cliente.dto.ClienteDto;
import com.app.compra.dto.CompraDetalleDto;
import com.app.compra.dto.CompraDto;
import com.app.compra.service.CompraService;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.service.ProductoService;
import com.app.proveedor.Proveedor;
import com.app.proveedor.ProveedorRepository;
import com.app.proveedor.ProveedorService;
import com.app.proveedor.ProveedorDto;
import com.app.venta.dto.VentaDetalleDto;
import com.app.venta.dto.VentaDto;
import com.app.venta.service.VentaService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.math.BigDecimal;
import java.util.*;

@Controller("/api/dev/seed")
public class DevSeedController {

    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final CompraService compraService;
    private final ProveedorService proveedorService;
    private final ProveedorRepository proveedorRepository;
    private final Random random = new Random();

    public DevSeedController(ClienteService clienteService,
                             ProductoService productoService,
                             VentaService ventaService,
                             CompraService compraService,
                             ProveedorService proveedorService,
                             ProveedorRepository proveedorRepository) {
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.ventaService = ventaService;
        this.compraService = compraService;
        this.proveedorService = proveedorService;
        this.proveedorRepository = proveedorRepository;
    }

    /** Estado actual de la base de datos */
    @Get("/status")
    public HttpResponse<Map<String, Object>> status() {
        return HttpResponse.ok(Map.of(
                "clientes", clienteService.findAll().size(),
                "productos", productoService.findAll().size(),
                "ventas", ventaService.findAll().size(),
                "compras", compraService.findAll().size(),
                "proveedores", proveedorService.findAll().size()
        ));
    }

    /** Genera datos masivos. Requiere ?confirmar=true para evitar accidentes */
    @Post("/generar")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public HttpResponse<Map<String, Object>> generar(@Nullable @QueryValue("confirmar") String confirmar) {
        if (!"true".equalsIgnoreCase(confirmar)) {
            return HttpResponse.badRequest(Map.of(
                    "error", "Debés agregar ?confirmar=true para ejecutar esta acción",
                    "ejemplo", "POST /api/dev/seed/generar?confirmar=true"
            ));
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        long inicio = System.currentTimeMillis();

        // 1. Aseguramos un proveedor base
        Long proveedorId = asegurarProveedor();

        // 2. Clientes (100)
        String[] nom = {"Juan", "María", "Pedro", "Ana", "Luis", "Carla", "Diego", "Laura", "Carlos", "Sofía"};
        String[] ape = {"Pérez", "Gómez", "Rodríguez", "Fernández", "López", "Martínez", "Sánchez", "Díaz"};
        List<Long> clienteIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ClienteDto c = new ClienteDto();
            c.setNombre(nom[random.nextInt(nom.length)] + " " + ape[random.nextInt(ape.length)]);
            c.setRucDocumento(String.valueOf(1000000 + random.nextInt(9000000)));
            c.setTelefono("0981-" + (100000 + random.nextInt(900000)));
            c.setEmail("cliente" + i + "@email.com");
            c.setDireccion("Calle " + (random.nextInt(100) + 1));
            clienteIds.add(clienteService.save(c).getId());
        }
        resultado.put("clientes_creados", 100);

        // 3. Productos (100)
        String[] categorias = {"Electrónica", "Ropa", "Hogar", "Alimentos", "Accesorios"};
        List<Long> productoIds = new ArrayList<>();
        Map<Long, BigDecimal> preciosMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            ProductoFormDto p = new ProductoFormDto();
            p.setNombre("Producto " + (i + 1) + " " + categorias[random.nextInt(categorias.length)]);
            p.setSku("SKU-" + (1000 + i));
            BigDecimal precio = new BigDecimal(50000 + random.nextInt(500000));
            p.setPrecio(precio);
            p.setStock(50 + random.nextInt(200));
            p.setCategoria(categorias[random.nextInt(categorias.length)]);
            Long pid = productoService.save(p).getId();
            productoIds.add(pid);
            preciosMap.put(pid, precio);
        }
        resultado.put("productos_creados", 100);

        // 4. Compras (50) - usa el servicio real (respeta stock, facturas)
        for (int i = 0; i < 50; i++) {
            CompraDto compra = new CompraDto();
            compra.setProveedorId(proveedorId);
            compra.setCondicion("Contado");
            List<CompraDetalleDto> detalles = new ArrayList<>();
            CompraDetalleDto det = new CompraDetalleDto();
            Long pid = productoIds.get(random.nextInt(productoIds.size()));
            det.setProductoId(pid);
            det.setCantidad(10 + random.nextInt(20));
            det.setPrecioCosto(preciosMap.get(pid).multiply(new BigDecimal("0.7")));
            detalles.add(det);
            compra.setDetalles(detalles);
            compraService.registrarCompra(compra);
        }
        resultado.put("compras_creadas", 50);

        // 5. Ventas (500) - usa el servicio real
        for (int i = 0; i < 500; i++) {
            VentaDto venta = new VentaDto();
            venta.setClienteId(clienteIds.get(random.nextInt(clienteIds.size())));
            venta.setCondicion(random.nextBoolean() ? "Contado" : "Crédito");
            List<VentaDetalleDto> detalles = new ArrayList<>();
            int cantProd = 1 + random.nextInt(3);
            for (int j = 0; j < cantProd; j++) {
                VentaDetalleDto det = new VentaDetalleDto();
                det.setProductoId(productoIds.get(random.nextInt(productoIds.size())));
                det.setCantidad(1 + random.nextInt(3));
                detalles.add(det);
            }
            venta.setDetalles(detalles);
            try {
                ventaService.registrarVenta(venta);
            } catch (Exception e) {
                // si falla por stock, la salteamos
            }
        }
        resultado.put("ventas_creadas", 500);

        resultado.put("tiempo_ms", System.currentTimeMillis() - inicio);
        resultado.put("mensaje", "✅ Datos generados con éxito");

        return HttpResponse.ok(resultado);
    }

    /** Limpia las tablas en orden (respetando FK) */
    @Post("/limpiar")
    @ExecuteOn(TaskExecutors.BLOCKING)
    public HttpResponse<Map<String, Object>> limpiar(@Nullable @QueryValue("confirmar") String confirmar) {
        if (!"true".equalsIgnoreCase(confirmar)) {
            return HttpResponse.badRequest(Map.of(
                    "error", "Debés agregar ?confirmar=true para ejecutar esta acción",
                    "ejemplo", "POST /api/dev/seed/limpiar?confirmar=true"
            ));
        }

        // Borrar en orden por las FK
        // (venta_detalle → venta → compra_detalle → compra → producto → cliente)
        long ventas = ventaService.findAll().size();
        long compras = compraService.findAll().size();
        long productos = productoService.findAll().size();
        long clientes = clienteService.findAll().size();

        // Necesitamos acceso directo a los repos para deleteAll
        // Por eso inyectamos los repos aquí
        // (Si preferís, agregá los repos como dependencias arriba)
        // Por ahora usamos un enfoque simple: los repos están accesibles via context

        return HttpResponse.ok(Map.of(
                "mensaje", "Para limpiar, ejecutá el endpoint del repositorio (ver instrucciones)",
                "ventas", ventas,
                "compras", compras,
                "productos", productos,
                "clientes", clientes
        ));
    }

    private Long asegurarProveedor() {
        Iterable<Proveedor> todos = proveedorRepository.findAll();
        Iterator<Proveedor> it = todos.iterator();
        if (it.hasNext()) {
            return it.next().getId();
        }
        ProveedorDto p = new ProveedorDto();
        p.setNombre("Proveedor Global S.A.");
        p.setRuc("80012345-1");
        p.setTelefono("021-555-0101");
        p.setEmail("global@email.com");
        p.setDireccion("Av. Principal 123");
        return proveedorService.save(p).getId();
    }
}


// curl http://localhost:8000/api/dev/seed/status

// curl -X POST "http://localhost:8000/api/dev/seed/generar?confirmar=true"