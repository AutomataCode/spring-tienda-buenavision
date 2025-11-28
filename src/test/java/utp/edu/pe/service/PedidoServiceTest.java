package utp.edu.pe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utp.edu.pe.dto.Carrito;
import utp.edu.pe.dto.CarritoItem;
import utp.edu.pe.entity.*;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.entity.enums.EstadoVenta;
import utp.edu.pe.entity.enums.TipoMovimientoInventario;
import utp.edu.pe.exception.StockInsuficienteException;
import utp.edu.pe.repository.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private CarritoService carritoService;
    @Mock private VentaRepository ventaRepository; // Se usa al entregar pedido

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    // PU-PE01: Crear Pedido Exitoso (Valida Stock, Inventario y Totales)
    @Test
    void testCrearPedidoDesdeCarrito_Exitoso() throws Exception {
        // --- ARRANGE ---
        Usuario usuario = new Usuario(); usuario.setIdUsuario(1);
        Cliente cliente = new Cliente(); cliente.setIdCliente(10);

        // Simulamos Producto con Stock 10
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Lentes Test");
        producto.setStockActual(10);
        producto.setPrecioVenta(new BigDecimal("100.00"));

        // Simulamos Carrito con 1 item de cantidad 2
        Carrito carrito = new Carrito();
        CarritoItem item = new CarritoItem(producto, 2); 
        carrito.setItems(Collections.singletonList(item));

        // Mocks
        when(carritoService.getCarrito()).thenReturn(carrito);
        when(clienteRepository.findByUsuario_IdUsuario(1)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        Pedido resultado = pedidoService.crearPedidoDesdeCarrito(usuario, "Calle Test 123", "Ninguna");

        // --- ASSERT ---
        assertNotNull(resultado);
        assertEquals(EstadoPedido.PENDIENTE, resultado.getEstado());
        assertEquals(new BigDecimal("200.00"), resultado.getSubtotal()); // 100 * 2
        
        // Validar reducción de stock (10 - 2 = 8)
        assertEquals(8, producto.getStockActual());
        verify(productoRepository).save(producto);

        // Validar creación de movimiento de inventario (SALIDA)
        verify(inventarioRepository).save(argThat(inv -> 
            inv.getTipoMovimiento() == TipoMovimientoInventario.SALIDA &&
            inv.getCantidad() == -2 // Salida es negativa
        ));
        
        // Validar que se vació el carrito
        verify(carritoService).vaciarCarrito();
    }

    // PU-PE02: Crear Pedido - Stock Insuficiente
    @Test
    void testCrearPedido_StockInsuficiente() {
        // --- ARRANGE ---
        Usuario usuario = new Usuario(); usuario.setIdUsuario(1);
        Cliente cliente = new Cliente();
        
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setStockActual(1); // Solo 1 en stock

        Carrito carrito = new Carrito();
        // Pedimos 5 unidades
        CarritoItem item = new CarritoItem(producto, 5); 
        carrito.setItems(Collections.singletonList(item));

        when(carritoService.getCarrito()).thenReturn(carrito);
        when(clienteRepository.findByUsuario_IdUsuario(1)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // --- ACT & ASSERT ---
        assertThrows(StockInsuficienteException.class, () -> {
            pedidoService.crearPedidoDesdeCarrito(usuario, "Dir", "Obs");
        });

        // Verificar que NO se guardó nada
        verify(pedidoRepository, never()).save(any());
    }

    // PU-PE03: Admin Actualiza a ENTREGADO (Genera Venta)
    @Test
    void testActualizarEstado_Entregado() {
        // --- ARRANGE ---
        Long pedidoId = 100L;
        Usuario admin = new Usuario(); admin.setEmail("admin@test.com");

        Pedido pedido = new Pedido();
        pedido.setIdPedido(pedidoId);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setSubtotal(new BigDecimal("100.00"));
        pedido.setTotal(new BigDecimal("118.00"));

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        Pedido resultado = pedidoService.actualizarEstadoPedido(pedidoId, EstadoPedido.ENTREGADO, admin);

        // --- ASSERT ---
        assertEquals(EstadoPedido.ENTREGADO, resultado.getEstado());
        
        // Verificar que se creó la VENTA
        verify(ventaRepository).save(argThat(v -> 
            v.getEstado() == EstadoVenta.COMPLETADA &&
            v.getTotal().equals(new BigDecimal("118.00"))
        ));
    }

    // PU-PE04: Admin Actualiza a CANCELADO (Repone Stock)
    @Test
    void testActualizarEstado_Cancelado_ReponeStock() {
        // --- ARRANGE ---
        Long pedidoId = 200L;
        Usuario admin = new Usuario();

        Producto producto = new Producto();
        producto.setIdProducto(5L);
        producto.setStockActual(10); // Stock original

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(3); // Se devolverán 3

        Pedido pedido = new Pedido();
        pedido.setIdPedido(pedidoId);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(Collections.singletonList(detalle));
        pedido.setNumeroPedido("PED-001");

        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        pedidoService.actualizarEstadoPedido(pedidoId, EstadoPedido.CANCELADO, admin);

        // --- ASSERT ---
        // 1. El stock debe haber aumentado (10 + 3 = 13)
        assertEquals(13, producto.getStockActual());
        verify(productoRepository).save(producto);

        // 2. Se debe crear movimiento de inventario (ENTRADA)
        verify(inventarioRepository).save(argThat(inv -> 
            inv.getTipoMovimiento() == TipoMovimientoInventario.ENTRADA &&
            inv.getCantidad() == 3 &&
            inv.getMotivo().contains("CANCELADO")
        ));
    }
}