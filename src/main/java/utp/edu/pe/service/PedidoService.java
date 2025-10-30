package utp.edu.pe.service;
import utp.edu.pe.dto.Carrito;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.exception.StockInsuficienteException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {
	   Pedido crearPedidoDesdeCarrito(Usuario usuario, String direccionEntrega, String observacionesCliente)
	            throws StockInsuficienteException, IllegalStateException;
	   
	   Optional<Pedido> findById(Long idPedido);
	   
	   List<Pedido> findByClienteId(Integer idCliente);
	   
	   List<Pedido> findByUsuarioAndFechas(Usuario usuario, LocalDate fechaInicio, LocalDate fechaFin);

	    /**
	     * Busca un pedido específico por su ID y el Usuario.
	     * El servicio valida que el pedido pertenezca al cliente de ese usuario.
	     */
	    Optional<Pedido> findByIdAndUsuario(Long pedidoId, Usuario usuario);
	    
	    
	    Page<Pedido> findAllPedidosAdmin(Pageable pageable);
	    Optional<Pedido> findByIdAdmin(Long pedidoId);
	    Pedido actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado, Usuario adminUsuario)
	            throws IllegalStateException;

}
	