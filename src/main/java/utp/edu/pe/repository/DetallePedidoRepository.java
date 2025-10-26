package utp.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.DetallePedido;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Producto;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedido(Pedido pedido);

    List<DetallePedido> findByProducto(Producto producto);
}