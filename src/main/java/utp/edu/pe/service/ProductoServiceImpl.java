package utp.edu.pe.service;

import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.EstadoProducto;
import utp.edu.pe.entity.enums.Genero;
import utp.edu.pe.entity.enums.TipoProducto;
import utp.edu.pe.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

	  private final ProductoRepository productoRepository;
	    
	    public ProductoServiceImpl(ProductoRepository productoRepository) {
	        this.productoRepository = productoRepository;
	    }
	    
	    @Override
	    public Producto save(Producto producto) {
	        return productoRepository.save(producto);
	    }
	    
	    @Override
	    public List<Producto> findAll() {
	        return productoRepository.findAll();
	    }
	    
	    @Override
	    public Page<Producto> findAll(Pageable pageable) {
	        return productoRepository.findAll(pageable);
	    }
	    
	    @Override
	    public Optional<Producto> findById(Long id) {
	        return productoRepository.findById(id);
	    }
	    
	    @Override
	    public Optional<Producto> findBySku(String sku) {
	        return productoRepository.findBySku(sku);
	    }
	    
	    @Override
	    public void deleteById(Long id) {
	        productoRepository.deleteById(id);
	    }
	    
	    @Override
	    public List<Producto> findByNombre(String nombre) {
	        return productoRepository.findByNombreContainingIgnoreCase(nombre);
	    }
	    
	    @Override
	    public List<Producto> findActiveProducts() {
	        return productoRepository.findByEstado(EstadoProducto.ACTIVO);
	    }
	    
	    @Override
	    public Page<Producto> findActiveProducts(Pageable pageable) {
	        return productoRepository.findActiveProducts(pageable);
	    }
	    
	    @Override
	    public List<Producto> findByEstado(EstadoProducto estado) {
	        return productoRepository.findByEstado(estado);
	    }
	    
	    @Override
	    public Page<Producto> findByEstado(EstadoProducto estado, Pageable pageable) {
	        return productoRepository.findByEstado(estado, pageable);
	    }
	    
	    @Override
	    public List<Producto> findByTipo(TipoProducto tipo) {
	        return productoRepository.findByTipo(tipo);
	    }
	    
	    @Override
	    public Page<Producto> findByTipo(TipoProducto tipo, Pageable pageable) {
	        return productoRepository.findByTipo(tipo, pageable);
	    }
	    
	    @Override
	    public List<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero) {
	        return productoRepository.findByTipoAndGenero(tipo, genero);
	    }
	    
	    @Override
	    public Page<Producto> findByTipoAndGenero(TipoProducto tipo, Genero genero, Pageable pageable) {
	        return productoRepository.findByTipoAndGenero(tipo, genero, pageable);
	    }
	    
	    @Override
	    public List<Producto> findByTipoAndMarca(TipoProducto tipo, Marca marca) {
	        return productoRepository.findByTipoAndMarca(tipo, marca);
	    }
	    
	    @Override
	    public List<Producto> findByMarca(Marca marca) {
	        return productoRepository.findByMarca(marca);
	    }
	    
	    @Override
	    public List<Producto> findByCategoria(CategoriaProducto categoria) {
	        return productoRepository.findByCategoria(categoria);
	    }
	    
	    @Override
	    public List<Producto> findWithFilters(TipoProducto tipo, Genero genero, Marca marca, 
	                                        CategoriaProducto categoria, FormaMontura forma, 
	                                        MaterialMontura material, BigDecimal minPrecio, 
	                                        BigDecimal maxPrecio) {
	        return productoRepository.findWithFilters(tipo, genero, marca, categoria, forma, material, minPrecio, maxPrecio);
	    }
	    
	    @Override
	    public List<Producto> searchProducts(String searchTerm) {
	        return productoRepository.searchProducts(searchTerm);
	    }
	    
	    @Override
	    public List<Producto> findLowStockProducts() {
	        return productoRepository.findByStockActualLessThanEqual(5);
	    }
	    
	    @Override
	    public List<Producto> findOutOfStockProducts() {
	        return productoRepository.findByStockActualEquals(0);
	    }
	    
	    @Override
	    public void updateStock(Long productoId, Integer nuevaCantidad) {
	        Producto producto = productoRepository.findById(productoId)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
	        producto.setStockActual(nuevaCantidad);
	        
	        if (nuevaCantidad == 0) {
	            producto.setEstado(EstadoProducto.AGOTADO);
	        } else if (producto.getEstado() == EstadoProducto.AGOTADO) {
	            producto.setEstado(EstadoProducto.ACTIVO);
	        }
	        
	        productoRepository.save(producto);
	    }
	    
	    @Override
	    public void reduceStock(Long productoId, Integer cantidad) {
	        Producto producto = productoRepository.findById(productoId)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
	        
	        if (!producto.tieneStockSuficiente(cantidad)) {
	            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
	        }
	        
	        producto.reducirStock(cantidad);
	        productoRepository.save(producto);
	    }
	    
	    @Override
	    public void increaseStock(Long productoId, Integer cantidad) {
	        Producto producto = productoRepository.findById(productoId)
	            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
	        
	        producto.aumentarStock(cantidad);
	        productoRepository.save(producto);
	    }
	    
	    @Override
	    public boolean existsBySku(String sku) {
	        return productoRepository.existsBySku(sku);
	    }
	    
	    @Override
	    public long countActiveProducts() {
	        return productoRepository.countByEstado(EstadoProducto.ACTIVO);
	    }
	    
	    @Override
	    public long countOutOfStockProducts() {
	        return productoRepository.findByStockActualEquals(0).size();
	    }
	    
	    /*
	    @Override
	    public List<Producto> findTopSellingProducts(Pageable pageable) {
	        return productoRepository.findTopSellingProducts(pageable);
	    }
	*/
	 
	
}
