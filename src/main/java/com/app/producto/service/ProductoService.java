package com.app.producto.service;

import com.app.exception.BusinessException;
import com.app.movimiento_stock.dto.MovimientoStockDto;
import com.app.movimiento_stock.service.MovimientoStockService;
import com.app.producto.dto.ProductoDto;
import com.app.producto.dto.ProductoFormDto;
import com.app.producto.model.Producto;
import com.app.producto.repository.ProductoRepository;
import com.app.producto_categoria.model.ProductoCategoria;
import com.app.producto_categoria.repository.ProductoCategoriaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoCategoriaRepository productoCategoriaRepository;
    private final MovimientoStockService movimientoStockService;

    public ProductoService(ProductoRepository productoRepository,
                           ProductoCategoriaRepository productoCategoriaRepository,
                           MovimientoStockService movimientoStockService) {
        this.productoRepository = productoRepository;
        this.productoCategoriaRepository = productoCategoriaRepository;
        this.movimientoStockService = movimientoStockService;
    }

    public List<ProductoDto> findAll() {
        return StreamSupport.stream(productoRepository.findAllByActivoTrue().spliterator(), false)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Page<ProductoDto> findPage(int page, int size) {
        Pageable pageable = Pageable.from(page, size, Sort.of(Sort.Order.desc("id")));
        Page<Producto> pagina = productoRepository.findAllByActivoTrue(pageable);
        return pagina.map(this::mapToDto);
    }

    public List<ProductoDto> buscar(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll().stream().limit(10).collect(Collectors.toList());
        }
        String pattern = "%" + query.trim() + "%";
        return productoRepository.buscar(pattern).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<ProductoDto> findById(Long id) {
        return productoRepository.findById(id).map(this::mapToDto);
    }

    public List<MovimientoStockDto> findMovimientos(Long productoId) {
        return movimientoStockService.findByProducto(productoId);
    }

    @Transactional
    public ProductoDto save(ProductoFormDto formDto) {
        ProductoCategoria categoria = resolverCategoria(formDto);
        Producto producto = mapFormToEntity(formDto, categoria);

        int stockInicial = producto.getStock() != null ? producto.getStock() : 0;
        producto.setStock(0);
        Producto saved = productoRepository.save(producto);

        if (stockInicial != 0) {
            movimientoStockService.registrar(saved, stockInicial, "INICIAL", "PRODUCTO",
                    saved.getId(), "Stock inicial", saved.getCostoActual());
        }
        return mapToDto(saved);
    }

    @Transactional
    public Optional<ProductoDto> update(Long id, ProductoFormDto formDto) {
        Optional<Producto> actualOpt = productoRepository.findById(id);
        if (actualOpt.isEmpty()) {
            return Optional.empty();
        }

        Producto actual = actualOpt.get();
        ProductoCategoria categoria = resolverCategoria(formDto);
        Producto producto = mapFormToEntity(formDto, categoria);

        int stockActual = actual.getStock() != null ? actual.getStock() : 0;
        int stockNuevo = formDto.getStock() != null ? formDto.getStock() : stockActual;

        producto.setId(id);
        producto.setStock(stockActual);
        producto.setActivo(actual.isActivo());
        Producto updated = productoRepository.update(producto);

        int diferencia = stockNuevo - stockActual;
        if (diferencia != 0) {
            movimientoStockService.registrar(updated, diferencia, "AJUSTE", "PRODUCTO",
                    id, "Ajuste manual de stock", updated.getCostoActual());
        }
        return Optional.of(mapToDto(updated));
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Producto> prodOpt = productoRepository.findById(id);
        if (prodOpt.isPresent()) {
            Producto prod = prodOpt.get();
            prod.setActivo(false);
            productoRepository.update(prod);
            return true;
        }
        return false;
    }

    private ProductoDto mapToDto(Producto producto) {
        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setSku(producto.getSku());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCostoActual(producto.getCostoActual());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setCategoriaId(producto.getCategoriaId());
        dto.setCategoria(producto.getCategoria());
        return dto;
    }

    private Producto mapFormToEntity(ProductoFormDto formDto, ProductoCategoria categoria) {
        Producto producto = new Producto();
        producto.setId(formDto.getId());
        producto.setNombre(formDto.getNombre());
        producto.setSku(formDto.getSku());
        producto.setPrecio(formDto.getPrecio());
        producto.setStock(formDto.getStock());
        producto.setCostoActual(formDto.getCostoActual() != null ? formDto.getCostoActual() : BigDecimal.ZERO);
        producto.setStockMinimo(formDto.getStockMinimo() != null ? formDto.getStockMinimo() : 5);
        producto.setCategoriaId(categoria.getId());
        producto.setCategoria(categoria.getNombre());
        return producto;
    }

    private ProductoCategoria resolverCategoria(ProductoFormDto formDto) {
        ProductoCategoria categoria = null;
        if (formDto.getCategoriaId() != null) {
            categoria = productoCategoriaRepository.findById(formDto.getCategoriaId())
                    .orElseThrow(() -> new BusinessException("La categoría seleccionada no existe."));
        } else if (formDto.getCategoria() != null && !formDto.getCategoria().trim().isEmpty()) {
            String nombre = formDto.getCategoria().trim();
            categoria = productoCategoriaRepository.findFirstByNombreIgnoreCase(nombre)
                    .orElseThrow(() -> new BusinessException("La categoría '" + nombre + "' no existe."));
        }

        if (categoria == null) {
            throw new BusinessException("La categoría es obligatoria.");
        }
        if (!categoria.isActivo()) {
            throw new BusinessException("La categoría '" + categoria.getNombre() + "' está inactiva.");
        }
        return categoria;
    }
}
