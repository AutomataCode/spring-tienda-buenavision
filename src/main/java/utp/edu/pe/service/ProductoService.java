package utp.edu.pe.service;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
public interface ProductoService {
	
	
	 // CRUD
    Producto save(Producto producto);
    List<Producto> findAll();
    Page<Producto> findAll(Pageable pageable);
    Optional<Producto> findById(Long id);
    Optional<Producto> findBySku(String sku);
    void deleteById(Long id);
    
    // Búsquedas
    List<Producto> findByNombre(String nombre);
    List<Producto> findActiveProducts();
    Page<Producto> findActiveProducts(Pageable pageable);
    List<Producto> findByEstado(EstadoProducto estado);
    Page<Producto> findByEstado(EstadoProducto estado, Pageable pageable);
    
    // Filtros
    List<Producto> findByTipo(TipoProducto tipo);
    Page<Producto> findByTipo(TipoProducto tipo, Pageable pageable);
    List<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero);
    Page<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero, Pageable pageable);
    List<Producto> findByTipoAndMarca(TipoProducto tipo, Marca marca);
    List<Producto> findByMarca(Marca marca);
    List<Producto> findByCategoria(CategoriaProducto categoria);
    
    // Filtros avanzados
    List<Producto> findWithFilters(TipoProducto tipo, Genero genero, Marca marca, 
                                  CategoriaProducto categoria, FormaMontura forma, 
                                  MaterialMontura material, BigDecimal minPrecio, 
                                  BigDecimal maxPrecio);
    
    // Búsqueda
    List<Producto> searchProducts(String searchTerm);
    
    // Stock
    List<Producto> findLowStockProducts();
    List<Producto> findOutOfStockProducts();
    void updateStock(Long productoId, Integer nuevaCantidad);
    void reduceStock(Long productoId, Integer cantidad);
    void increaseStock(Long productoId, Integer cantidad);
    
    // Validaciones
    boolean existsBySku(String sku);
    
    // Dashboard
    long countActiveProducts();
    long countOutOfStockProducts();
    
    /*
    // Más vendidos
    List<Producto> findTopSellingProducts(Pageable pageable);
    
    */
}
