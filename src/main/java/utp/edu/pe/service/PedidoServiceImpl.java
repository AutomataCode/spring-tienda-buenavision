package utp.edu.pe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.dto.Carrito;
import utp.edu.pe.dto.CarritoItem;
import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.DetallePedido;
import utp.edu.pe.entity.Inventario;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.Venta;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.entity.enums.EstadoVenta;
import utp.edu.pe.entity.enums.MetodoPago;
import utp.edu.pe.entity.enums.TipoMovimientoInventario;
import utp.edu.pe.exception.StockInsuficienteException;

import utp.edu.pe.repository.ClienteRepository;
import utp.edu.pe.repository.DetallePedidoRepository;
import utp.edu.pe.repository.InventarioRepository;
import utp.edu.pe.repository.PedidoRepository;
import utp.edu.pe.repository.ProductoRepository;
import utp.edu.pe.repository.VentaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Para generar número de pedido

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18"); // Tasa de IGV (18%)

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository; 
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired	
    private InventarioRepository inventarioRepository;
    @Autowired
    private ClienteRepository clienteRepository; // Para obtener el cliente
    @Autowired
    private CarritoService carritoService; // Para obtener el carrito de la sesión

    @Override
    @Transactional 
    public Pedido crearPedidoDesdeCarrito(Usuario usuario, String direccionEntrega, String observacionesCliente)
            throws StockInsuficienteException, IllegalStateException {

        Carrito carrito = carritoService.getCarrito();
        if (carrito.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        // Obtener el cliente asociado al usuario
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("No se encontró cliente asociado al usuario ID: " + usuario.getIdUsuario()));

        // Crear la cabecera del Pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setNumeroPedido(generarNumeroPedidoUnico()); // Método para generar número
        pedido.setDireccionEntrega(direccionEntrega);
        pedido.setObservacionesCliente(observacionesCliente);
        pedido.setEstado(EstadoPedido.PENDIENTE); // Estado inicial

        BigDecimal subtotalCalculado = BigDecimal.ZERO;

        //  Crear los Detalles del Pedido y Validar/Actualizar Stock
        for (CarritoItem item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProducto().getIdProducto())
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado durante la creación del pedido: ID " + item.getProducto().getIdProducto()));

            int cantidadPedida = item.getCantidad();
            int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;

            if (stockActual < cantidadPedida) {
                logger.warn("Stock insuficiente al crear pedido. Producto ID: {}, Solicitado: {}, Disponible: {}",
                        producto.getIdProducto(), cantidadPedida, stockActual);
                // Lanzar excepción específica para que el controlador la maneje
                throw new StockInsuficienteException("Stock insuficiente para: " + producto.getNombre() + ". Disponibles: " + stockActual);
            }

            // Crear detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidadPedida);
            detalle.setPrecioUnitario(item.getPrecioUnitario()); // Usa el precio guardado en el carrito
            // detalle.setSubtotal(item.getSubtotal()); // El subtotal se calcula en la BD (STORED)

            pedido.addDetalle(detalle); // Añade a la lista y establece la relación bidireccional

            // Actualizar stock del producto
            int nuevoStock = stockActual - cantidadPedida;
            producto.setStockActual(nuevoStock);
            productoRepository.save(producto); // Guarda el stock actualizado

            // Crear registro de inventario (SALIDA por pedido creado)
            Inventario movimiento = new Inventario(
                    producto,
                    TipoMovimientoInventario.SALIDA, // O RESERVA si prefieres
                    cantidadPedida * -1, // Cantidad negativa para salida
                    stockActual,
                    nuevoStock,
                    pedido, // Asocia al pedido
                    null,   // Sin venta aún
                    "Salida por Pedido #" + pedido.getNumeroPedido(),
                    usuario // Usuario que generó el movimiento (el cliente)
            );
            inventarioRepository.save(movimiento);

            subtotalCalculado = subtotalCalculado.add(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidadPedida)));
        }

        //  Calcular Totales y Asignar al Pedido
        pedido.setSubtotal(subtotalCalculado);
        // Calcular IGV sobre el subtotal (ajusta si el descuento aplica antes)
        BigDecimal igvCalculado = subtotalCalculado.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        pedido.setIgv(igvCalculado);
        // Asumiendo descuento 0 por ahora, puedes añadir lógica para cupones etc.
        BigDecimal descuento = pedido.getDescuento() != null ? pedido.getDescuento() : BigDecimal.ZERO;
        BigDecimal totalCalculado = subtotalCalculado.add(igvCalculado).subtract(descuento);
        pedido.setTotal(totalCalculado);

        // Guardar el Pedido (y sus detalles en cascada)
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        logger.info("Pedido #{} creado exitosamente para cliente ID {}", pedidoGuardado.getNumeroPedido(), cliente.getIdCliente());

        // Vaciar el carrito de la sesión
        carritoService.vaciarCarrito();

        return pedidoGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long idPedido) {
        return pedidoRepository.findById(idPedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> findByClienteId(Integer idCliente) {
        // Necesitas un Cliente objeto o buscar por ID 
         Cliente cliente = new Cliente();
         cliente.setIdCliente(idCliente);

         return pedidoRepository.findByCliente(cliente); 
    }


    // Método simple para generar un número único  
    private String generarNumeroPedidoUnico() {
    
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

	@Override
	@Transactional(readOnly = true)
	public List<Pedido> findByUsuarioAndFechas(Usuario usuario, LocalDate fechaInicio, LocalDate fechaFin) {
		//  Obtener Cliente desde Usuario
        Cliente cliente = clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("Cliente no asociado a usuario ID: " + usuario.getIdUsuario()));

        //Convertir fechas
        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        // Llamar al repositorio con el ID del cliente 
        return pedidoRepository.findByClienteIdAndFechas(cliente.getIdCliente(), inicio, fin);
	}

	@Override
	public Optional<Pedido> findByIdAndUsuario(Long pedidoId, Usuario usuario) {
		//  Obtener Cliente desde Usuario
        Optional<Cliente> clienteOpt = clienteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());


        if (clienteOpt.isEmpty()) {
            return Optional.empty();
        }
        
    
        return pedidoRepository.findByIdAndClienteId(pedidoId, clienteOpt.get().getIdCliente());
	}

	@Override
	public Page<Pedido> findAllPedidosAdmin(Pageable pageable) {
		return pedidoRepository.findAll(pageable);
	}

	@Override
	public Optional<Pedido> findByIdAdmin(Long pedidoId) {

		return pedidoRepository.findById(pedidoId);
	}

	@Override
	public Pedido actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado, Usuario adminUsuario)
			throws IllegalStateException {
		logger.info("Admin {} actualizando pedido ID {} al estado {}", adminUsuario.getEmail(), pedidoId, nuevoEstado);

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalStateException("Pedido no encontrado con ID: " + pedidoId));

        //  No se puede modificar un pedido finalizado
        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            logger.warn("Intento de modificar pedido ya finalizado (ID: {})", pedidoId);
            throw new IllegalStateException("El pedido ya está finalizado (ENTREGADO o CANCELADO) y no se puede modificar.");
        }
        
        // No se puede cambiar al mismo estado
        if (pedido.getEstado() == nuevoEstado) {
            return pedido; // No hay nada que hacer
        }


        //  Pedido se marca como ENTREGADO
        if (nuevoEstado == EstadoPedido.ENTREGADO) {
            crearVentaDesdePedido(pedido, adminUsuario);
        }

        //  Pedido se marca como CANCELADO
        if (nuevoEstado == EstadoPedido.CANCELADO) {
            reponerStockPorCancelacion(pedido, adminUsuario);
        }

        // Actualizar y guardar el estado del pedido
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
	}
	
	
	private Venta crearVentaDesdePedido(Pedido pedido, Usuario adminUsuario) {
        Venta venta = new Venta();
        venta.setPedido(pedido);
       
        venta.setVendedor(adminUsuario);  
        
        venta.setNumeroVenta(generarNumeroVentaUnico());
        
        // Copiamos los totales del pedido
        venta.setSubtotal(pedido.getSubtotal());
        venta.setIgv(pedido.getIgv());
        venta.setDescuento(pedido.getDescuento());
        venta.setTotal(pedido.getTotal());
        
        venta.setEstado(EstadoVenta.COMPLETADA); // Estado final
        
      
        // Asignamos uno por defecto. 
        venta.setMetodoPago(MetodoPago.EFECTIVO); // O EFECTIVO, o POR_DEFINIR
        
        // Guardamos la Venta
        Venta ventaGuardada = ventaRepository.save(venta);
        
        // Opcional: Actualizamos los registros de inventario (SALIDA)
        // para vincularlos a esta venta.
        List<Inventario> movimientos = inventarioRepository.findByPedido(pedido);
        for (Inventario mov : movimientos) {
            mov.setVenta(ventaGuardada);
            inventarioRepository.save(mov);
        }
        
        logger.info("Venta ID {} creada desde Pedido ID {}", ventaGuardada.getIdVenta(), pedido.getIdPedido());
        return ventaGuardada;
    }
	
	private void reponerStockPorCancelacion(Pedido pedido, Usuario adminUsuario) {
        logger.warn("Cancelando pedido ID {} y reponiendo stock.", pedido.getIdPedido());
        
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidadDevuelta = detalle.getCantidad();
            
            int stockAntiguo = producto.getStockActual() != null ? producto.getStockActual() : 0;
            int stockNuevo = stockAntiguo + cantidadDevuelta;
            
           //Devolver el stock al producto
            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);
            
           // Registrar la ENTRADA en inventario
            Inventario movimiento = new Inventario(
                producto,
                TipoMovimientoInventario.ENTRADA, // Entrada por devolución
                cantidadDevuelta, // Positivo
                stockAntiguo,
                stockNuevo,
                pedido, // Vinculado al pedido cancelado
                null,   // Sin venta
                "Devolución por Pedido #" + pedido.getNumeroPedido() + " CANCELADO",
                adminUsuario 
            );
            inventarioRepository.save(movimiento);
            
            logger.info("Stock repuesto para Producto ID {}: {} unidades. Nuevo stock: {}", 
                        producto.getIdProducto(), cantidadDevuelta, stockNuevo);
        }
    }
	
	private String generarNumeroVentaUnico() {
    
        return "VT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
	
	
	

}
