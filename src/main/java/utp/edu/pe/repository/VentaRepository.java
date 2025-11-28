package utp.edu.pe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utp.edu.pe.entity.Venta;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoVenta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import utp.edu.pe.dto.dashboard.ProductoTopDTO;
import utp.edu.pe.dto.dashboard.VentaPeriodoDTO;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByNumeroVenta(String numeroVenta);

    Optional<Venta> findByPedido(Pedido pedido);

    List<Venta> findByVendedor(Usuario vendedor);

    List<Venta> findByEstado(EstadoVenta estado);

    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);
    
    Optional<Venta> findByPedidoIdPedido(Long pedidoId);
    
    
 // 1. Ventas de los últimos 7 días
    @Query(value = "SELECT DATE_FORMAT(v.fecha_venta, '%d-%m-%Y') as etiqueta, SUM(v.total) as total " +
                   "FROM venta v " +
                   "WHERE v.fecha_venta >= DATE(NOW()) - INTERVAL 7 DAY " +
                   "AND v.estado = 'COMPLETADA' " +
                   "GROUP BY DATE_FORMAT(v.fecha_venta, '%d-%m-%Y'), DATE(v.fecha_venta) " +
                   "ORDER BY DATE(v.fecha_venta) ASC", nativeQuery = true)
    List<VentaPeriodoDTO> obtenerVentasUltimos7Dias();

    // 2. Ventas por Mes (del año actual)
    @Query(value = "SELECT DATE_FORMAT(v.fecha_venta, '%M') as etiqueta, SUM(v.total) as total " +
                   "FROM venta v " +
                   "WHERE YEAR(v.fecha_venta) = YEAR(NOW()) " +
                   "AND v.estado = 'COMPLETADA' " +
                   "GROUP BY DATE_FORMAT(v.fecha_venta, '%M'), MONTH(v.fecha_venta) " +
                   "ORDER BY MONTH(v.fecha_venta) ASC", nativeQuery = true)
    List<VentaPeriodoDTO> obtenerVentasPorMes();

    // 3. Top 5 Productos más vendidos
    // Unimos Venta -> Pedido -> Detalle -> Producto
    @Query(value = "SELECT p.nombre as nombre, SUM(dp.cantidad) as cantidad " +
                   "FROM venta v " +
                   "JOIN pedido pe ON v.id_pedido = pe.id_pedido " +
                   "JOIN detalle_pedido dp ON pe.id_pedido = dp.id_pedido " +
                   "JOIN producto p ON dp.id_producto = p.id_producto " +
                   "WHERE v.estado = 'COMPLETADA' " +
                   "GROUP BY p.id_producto, p.nombre " +
                   "ORDER BY cantidad DESC " +
                   "LIMIT 5", nativeQuery = true)
    List<ProductoTopDTO> obtenerTopProductosVendidos();
    
    // 4. Total ingresos hoy (KPI)
    @Query(value = "SELECT COALESCE(SUM(v.total), 0) FROM venta v WHERE DATE(v.fecha_venta) = CURDATE() AND v.estado = 'COMPLETADA'", nativeQuery = true)
    Double obtenerIngresosHoy();
}
