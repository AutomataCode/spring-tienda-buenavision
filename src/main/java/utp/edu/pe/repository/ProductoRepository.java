package utp.edu.pe.repository;

import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Búsquedas básicas

    Optional<Producto> findBySku(String sku);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByEstado(EstadoProducto estado);
    Page<Producto> findByEstado(EstadoProducto estado, Pageable pageable);

    // Relaciones

    List<Producto> findByCategoria(CategoriaProducto categoria);
    List<Producto> findByMarca(Marca marca);
    List<Producto> findByForma(FormaMontura forma);
    List<Producto> findByMaterial(MaterialMontura material);
    List<Producto> findByTipo(TipoProducto tipo);
    Page<Producto> findByTipo(TipoProducto tipo, Pageable pageable);
    List<Producto> findByGenero(Genero genero);
    List<Producto> findByTalla(TallaMontura talla);

    // Filtros combinados

    List<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero);
    Page<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero, Pageable pageable);
    List<Producto> findByTipoAndMarca(TipoProducto tipo, Marca marca);
    List<Producto> findByTipoAndForma(TipoProducto tipo, FormaMontura forma);
 
    // Filtros por precio
 
    List<Producto> findByPrecioVentaBetween(BigDecimal minPrecio, BigDecimal maxPrecio);
    List<Producto> findByTipoAndPrecioVentaBetween(TipoProducto tipo, BigDecimal minPrecio, BigDecimal maxPrecio);

 
    // Control de stock
 
    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);
    List<Producto> findByStockActualEquals(Integer stock);

 
    // Consulta con filtros dinámicos
 
    @Query("""
           SELECT p FROM Producto p 
           WHERE (:tipo IS NULL OR p.tipo = :tipo)
             AND (:genero IS NULL OR p.genero = :genero)
             AND (:marca IS NULL OR p.marca = :marca)
             AND (:categoria IS NULL OR p.categoria = :categoria)
             AND (:forma IS NULL OR p.forma = :forma)
             AND (:material IS NULL OR p.material = :material)
             AND (:minPrecio IS NULL OR p.precioVenta >= :minPrecio)
             AND (:maxPrecio IS NULL OR p.precioVenta <= :maxPrecio)
             AND p.estado = 'Activo'
           """)
    List<Producto> findWithFilters(
            @Param("tipo") TipoProducto tipo,
            @Param("genero") Genero genero,
            @Param("marca") Marca marca,
            @Param("categoria") CategoriaProducto categoria,
            @Param("forma") FormaMontura forma,
            @Param("material") MaterialMontura material,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio);

 
    // Productos activos paginados
 
    @Query("SELECT p FROM Producto p WHERE p.estado = 'Activo'")
    Page<Producto> findActiveProducts(Pageable pageable);

 
    // Búsqueda general
 
    @Query("""
           SELECT p FROM Producto p 
           WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
               OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
               OR LOWER(p.marca.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
               OR LOWER(p.color) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
             AND p.estado = 'Activo'
           """)
    List<Producto> searchProducts(@Param("searchTerm") String searchTerm);

    
    // Validaciones
 
    boolean existsBySku(String sku);
    long countByEstado(EstadoProducto estado);
}
