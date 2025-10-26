package utp.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.enums.EstadoPedido;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByCliente(Cliente cliente);

    List<Pedido> findByEstado(EstadoPedido estado);

    //  añadir métodos más complejos con @Query
    //  buscar pedidos entre fechas, por cliente y estado, etc.
}