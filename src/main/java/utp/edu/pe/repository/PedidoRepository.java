package utp.edu.pe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.enums.EstadoPedido;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByCliente(Cliente cliente);

    List<Pedido> findByEstado(EstadoPedido estado);

 
    
    
    @Query("SELECT p FROM Pedido p WHERE p.id = :id AND p.cliente.idCliente = :clienteId")
    Optional<Pedido> findByIdAndClienteId(
            @Param("id") Long id,
            @Param("clienteId") Integer clienteId
    );

    /**
     * Busca pedidos de un cliente con filtros de fecha opcionales.
     * Corregido para usar p.cliente.idCliente
     */
    @Query("SELECT p FROM Pedido p WHERE p.cliente.idCliente = :clienteId " +
           "AND (:fechaInicio IS NULL OR p.fechaPedido >= :fechaInicio) " +
           "AND (:fechaFin IS NULL OR p.fechaPedido <= :fechaFin) " +
           "ORDER BY p.fechaPedido DESC")
    List<Pedido> findByClienteIdAndFechas(
            @Param("clienteId") Integer clienteId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
   
    
    
    /**
     * Busca todos los pedidos paginados, uniendo el cliente
     * para evitar N+1 queries en la lista de admin.
     */
    @Query(value = "SELECT p FROM Pedido p JOIN FETCH p.cliente ORDER BY p.fechaPedido DESC",
           countQuery = "SELECT COUNT(p) FROM Pedido p")
    Page<Pedido> findAll(Pageable pageable);
    
}