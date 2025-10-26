package utp.edu.pe.service;

import utp.edu.pe.dto.Carrito;
import utp.edu.pe.dto.CarritoItem;
import utp.edu.pe.entity.Producto;

import utp.edu.pe.repository.ProductoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

// Usamos Guava para Preconditions
import com.google.common.base.Preconditions;
// Usamos Apache Commons Collections para chequeos de colecciones
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Optional;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CarritoService implements Serializable {
	private static final long serialVersionUID = 1L;
    // Usamos Logback (a través de SLF4J), ya incluido por Spring Boot
    private static final Logger logger = LoggerFactory.getLogger(CarritoService.class);

    private final Carrito carrito = new Carrito();
    private final ProductoRepository productoRepository;

    @Autowired
    public CarritoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
        logger.info("Nueva instancia de CarritoService creada para la sesión.");
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void agregarItem(Long productoId, int cantidad) {
        // Validación con Guava
        Preconditions.checkArgument(cantidad > 0, "La cantidad debe ser positiva.");

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoId));

        // Validación de Stock
        if (producto.getStockActual() == null || producto.getStockActual() < cantidad) {
            logger.warn("Stock insuficiente. ID: {}, Solicitado: {}, Disponible: {}",
                      productoId, cantidad, producto.getStockActual());
            throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
        }

        Optional<CarritoItem> itemExistente = findItemByProductoId(productoId);

        if (itemExistente.isPresent()) {
            CarritoItem item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            // Validar stock total en carrito
             if (nuevaCantidad > producto.getStockActual()) {
                 logger.warn("Stock insuficiente al incrementar. ID: {}, Solicitado total: {}, Disp: {}",
                           productoId, nuevaCantidad, producto.getStockActual());
                 throw new IllegalArgumentException("No puedes agregar más unidades, stock insuficiente.");
             }
            item.setCantidad(nuevaCantidad);
            logger.debug("Cantidad actualizada ID {}: {}", productoId, nuevaCantidad);
        } else {
            carrito.getItems().add(new CarritoItem(producto, cantidad));
            logger.info("Producto ID {} agregado ({} unidades)", productoId, cantidad);
        }
    }

    public void actualizarCantidad(Long productoId, int cantidad) {
        Preconditions.checkArgument(cantidad > 0, "La cantidad debe ser positiva.");

        CarritoItem item = findItemByProductoId(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Item no en carrito ID: " + productoId));

        Producto producto = productoRepository.findById(productoId) // Re-chequear stock
                 .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado ID: " + productoId));

        if (cantidad > producto.getStockActual()) {
             logger.warn("Stock insuficiente al actualizar. ID: {}, Solicitado: {}, Disp: {}",
                       productoId, cantidad, producto.getStockActual());
            throw new IllegalArgumentException("Stock insuficiente para la cantidad solicitada.");
        }

        item.setCantidad(cantidad);
        logger.debug("Cantidad actualizada ID {} a: {}", productoId, cantidad);
    }

    public void eliminarItem(Long productoId) {
        boolean removed = carrito.getItems().removeIf(item -> item.getProducto().getIdProducto().equals(productoId));
        if (removed) {
            logger.info("Producto ID {} eliminado.", productoId);
        } else {
             logger.warn("Intento de eliminar ID {} no encontrado.", productoId);
        }
    }

    public void vaciarCarrito() {
        // Usamos Apache Commons Collections
        if (CollectionUtils.isNotEmpty(carrito.getItems())) {
            carrito.getItems().clear();
            logger.info("Carrito vaciado.");
        }
    }

    private Optional<CarritoItem> findItemByProductoId(Long productoId) {
        return carrito.getItems().stream()
                .filter(item -> item.getProducto().getIdProducto().equals(productoId))
                .findFirst();
    }

}
