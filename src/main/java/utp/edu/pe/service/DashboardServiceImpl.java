package utp.edu.pe.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utp.edu.pe.dto.dashboard.ProductoTopDTO;
import utp.edu.pe.dto.dashboard.VentaPeriodoDTO;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.repository.FormaMonturaRepository;
import utp.edu.pe.repository.PedidoRepository;
import utp.edu.pe.repository.ProductoRepository;
import utp.edu.pe.repository.VentaRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    
    public DashboardServiceImpl(VentaRepository ventaRepository,ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
    	
    	this.ventaRepository=ventaRepository;
    	this.productoRepository=productoRepository;
    	this.pedidoRepository=pedidoRepository;
    }
    


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenKpis() {
        Map<String, Object> kpis = new HashMap<>();
        
        // KPI 1: Ingresos de Hoy
        kpis.put("ingresosHoy", ventaRepository.obtenerIngresosHoy());
        
        // KPI 2: Productos Bajos de Stock (Menos de 5 unidades)
        kpis.put("productosBajoStock", productoRepository.findByStockActualEquals(0).size()); // O crear un query para < 5
        
        // KPI 3: Pedidos Pendientes de Atención (Suponiendo que tienes un método countByEstado)
        // Si no tienes countByEstado en PedidoRepository, añádelo: long countByEstado(EstadoPedido estado);
        // Aquí usaré una lógica genérica:
        long pendientes = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE)
                .count();
        kpis.put("pedidosPendientes", pendientes);
        
        return kpis;
    }

    @Override
    public List<VentaPeriodoDTO> getVentasUltimos7Dias() {
        return ventaRepository.obtenerVentasUltimos7Dias();
    }

    @Override
    public List<VentaPeriodoDTO> getVentasPorMes() {
        return ventaRepository.obtenerVentasPorMes();
    }

    @Override
    public List<ProductoTopDTO> getTopProductos() {
        return ventaRepository.obtenerTopProductosVendidos();
    }
}