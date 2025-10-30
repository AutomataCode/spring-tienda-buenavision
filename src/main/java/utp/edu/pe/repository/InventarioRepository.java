package utp.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.Inventario;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByProductoOrderByFechaMovimientoDesc(Producto producto);

    List<Inventario> findByTipoMovimiento(TipoMovimientoInventario tipoMovimiento);

    List<Inventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);
    
    List<Inventario> findByPedido(Pedido pedido);

    // añadir querys para obtener el último movimiento de un producto, etc.
    
    
}
