package utp.edu.pe.service;
import utp.edu.pe.dto.Carrito;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.exception.StockInsuficienteException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

}
	