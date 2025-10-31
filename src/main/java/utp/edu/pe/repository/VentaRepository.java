package utp.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.Venta;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroVenta(String numeroVenta);

    Optional<Venta> findByPedido(Pedido pedido);

    List<Venta> findByVendedor(Usuario vendedor);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    Optional<Venta> findByPedidoIdPedido(Long pedidoId);
}
