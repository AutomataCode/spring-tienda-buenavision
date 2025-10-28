package utp.edu.pe.service;

import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.EstadoProducto;
import utp.edu.pe.entity.enums.Genero;
import utp.edu.pe.entity.enums.TipoMovimientoInventario;
import utp.edu.pe.entity.enums.TipoProducto;
import utp.edu.pe.repository.InventarioRepository;
import utp.edu.pe.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

	private final ProductoRepository productoRepository;
	
	private final InventarioRepository inventarioRepository;

	private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

	public ProductoServiceImpl(ProductoRepository productoRepository, InventarioRepository inventarioRepository) {
		this.productoRepository = productoRepository;
		this.inventarioRepository= inventarioRepository;
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
	public List<Producto> findWithFilters(TipoProducto tipo, Genero genero, Marca marca, CategoriaProducto categoria,
			FormaMontura forma, MaterialMontura material, BigDecimal minPrecio, BigDecimal maxPrecio) {
		return productoRepository.findWithFilters(tipo, genero, marca, categoria, forma, material, minPrecio,
				maxPrecio);
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

	@Override
	@Transactional
	public void softDelete(Long id) {
		// TODO Auto-generated method stub
		Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + id));
        
        producto.setEstado(EstadoProducto.INACTIVO);
        productoRepository.save(producto);
        log.warn("Producto ID {} marcado como INACTIVO por admin", id);

	}

	@Override
	@Transactional
	public Producto saveAdmin(Producto producto, Usuario adminUsuario) { // 'producto' es el parámetro

		// --- Uso de Google Guava y Apache Commons ---
		Preconditions.checkNotNull(producto, "El producto no puede ser nulo");
		Preconditions.checkNotNull(adminUsuario, "El usuario admin no puede ser nulo");
		if (StringUtils.isBlank(producto.getNombre()) || StringUtils.isBlank(producto.getSku())) {
			throw new IllegalArgumentException("El nombre y SKU son obligatorios");
		}

		// Declaramos una nueva variable para guardar el resultado.
		Producto productoGuardado;

		int stockAntiguo = 0;
		int stockNuevo = (producto.getStockActual() != null) ? producto.getStockActual() : 0;
		String observacion = "";
		TipoMovimientoInventario tipoMovimiento = null;

		if (producto.getIdProducto() != null) {
		
			// La lambda usa 'producto.getIdProducto()', por lo que 'producto' no debe ser
			// reasignado.
			Producto productoEnDB = productoRepository.findById(producto.getIdProducto())
					.orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + producto.getIdProducto()));

			stockAntiguo = productoEnDB.getStockActual() != null ? productoEnDB.getStockActual() : 0;

			if (stockNuevo > stockAntiguo) {
				tipoMovimiento = TipoMovimientoInventario.ENTRADA;
				observacion = "Reposición de stock (Admin CRUD)";
			} else if (stockNuevo < stockAntiguo) {
				tipoMovimiento = TipoMovimientoInventario.AJUSTE;
				observacion = "Ajuste manual de stock (Admin CRUD)";
			}

			// Actualizamos la entidad de la BD (productoEnDB) con los datos del formulario
			// (producto)
			productoEnDB.setNombre(producto.getNombre());
			productoEnDB.setDescripcion(producto.getDescripcion());
			productoEnDB.setCategoria(producto.getCategoria());
			productoEnDB.setMarca(producto.getMarca());
			productoEnDB.setForma(producto.getForma());
			productoEnDB.setMaterial(producto.getMaterial());
			productoEnDB.setTipo(producto.getTipo());
			productoEnDB.setGenero(producto.getGenero());
			productoEnDB.setTalla(producto.getTalla());
			productoEnDB.setColor(producto.getColor());
			productoEnDB.setModelo(producto.getModelo());
			productoEnDB.setPrecioVenta(producto.getPrecioVenta());
			productoEnDB.setPrecioCosto(producto.getPrecioCosto());
			productoEnDB.setStockActual(stockNuevo);
			productoEnDB.setStockMinimo(producto.getStockMinimo());
			productoEnDB.setImagenUrl(producto.getImagenUrl());
			productoEnDB.setEstado(producto.getEstado());

			// Guardamos el resultado en la nueva variable
			productoGuardado = productoRepository.save(productoEnDB);

		} else {
			// --- CREACIÓN ---
			if (stockNuevo > 0) {
				tipoMovimiento = TipoMovimientoInventario.ENTRADA;
				observacion = "Stock inicial de nuevo producto";
			}
			if (producto.getEstado() == null) {
				producto.setEstado(EstadoProducto.ACTIVO);
			}

			// Guardamos el resultado en la NUEVA variable (usando el objeto 'producto' del
			// parámetro)
			productoGuardado = productoRepository.save(producto);
		}

		// --- Registrar en Inventario ---
		if (tipoMovimiento != null) {
			int cantidadMovimiento = stockNuevo - stockAntiguo;
			// Usamos la variable 'productoGuardado'
			crearRegistroInventario(productoGuardado, tipoMovimiento, cantidadMovimiento, stockAntiguo, stockNuevo,
					observacion, adminUsuario);
		}

		// Devolvemos y logueamos la variable 'productoGuardado'
		log.info("Producto guardado por admin {}: ID {}", adminUsuario.getEmail(), productoGuardado.getIdProducto());
		return productoGuardado;
	}

	private void crearRegistroInventario(Producto producto, TipoMovimientoInventario tipo, int cantidadMovimiento,
			int stockPrevio, int stockFinal, String motivo, Usuario usuario) {
		Inventario movimiento = new Inventario();
		movimiento.setProducto(producto);
		movimiento.setTipoMovimiento(tipo);
		movimiento.setCantidad(cantidadMovimiento);
		movimiento.setStockAnterior(stockPrevio);
		movimiento.setStockActual(stockFinal);
		movimiento.setMotivo(motivo);
		movimiento.setUsuario(usuario);
		movimiento.setPedido(null);
		movimiento.setVenta(null);

		inventarioRepository.save(movimiento);
	}

	/*
	 * @Override public List<Producto> findTopSellingProducts(Pageable pageable) {
	 * return productoRepository.findTopSellingProducts(pageable); }
	 */

}
