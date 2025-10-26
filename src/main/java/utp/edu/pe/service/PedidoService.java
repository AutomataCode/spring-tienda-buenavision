package utp.edu.pe.service;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.exception.StockInsuficienteException; 

import java.util.List;
import java.util.Optional;

public interface PedidoService {
	   Pedido crearPedidoDesdeCarrito(Usuario usuario, String direccionEntrega, String observacionesCliente)
	            throws StockInsuficienteException, IllegalStateException;
	   
	   Optional<Pedido> findById(Long idPedido);
	   
	   List<Pedido> findByClienteId(Integer idCliente);

}
	