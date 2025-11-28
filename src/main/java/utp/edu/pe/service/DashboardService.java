package utp.edu.pe.service;

import utp.edu.pe.dto.dashboard.ProductoTopDTO;
import utp.edu.pe.dto.dashboard.VentaPeriodoDTO;
import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> obtenerResumenKpis();
    List<VentaPeriodoDTO> getVentasUltimos7Dias();
    List<VentaPeriodoDTO> getVentasPorMes();
    List<ProductoTopDTO> getTopProductos();
}