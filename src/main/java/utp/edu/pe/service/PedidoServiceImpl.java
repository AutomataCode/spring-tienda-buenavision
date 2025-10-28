package utp.edu.pe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.entity.enums.TipoMovimientoInventario;
import utp.edu.pe.exception.StockInsuficienteException;

import utp.edu.pe.repository.ClienteRepository;
import utp.edu.pe.repository.DetallePedidoRepository;
import utp.edu.pe.repository.InventarioRepository;
import utp.edu.pe.repository.PedidoRepository;
import utp.edu.pe.repository.ProductoRepository;

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


    // Método simple para generar un número único (puedes mejorarlo)
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


}
