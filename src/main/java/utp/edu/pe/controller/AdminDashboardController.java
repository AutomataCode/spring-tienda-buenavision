package utp.edu.pe.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import utp.edu.pe.dto.dashboard.ProductoTopDTO;
import utp.edu.pe.dto.dashboard.VentaPeriodoDTO;
import utp.edu.pe.repository.FormaMonturaRepository;
import utp.edu.pe.service.DashboardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
	
	@Autowired
    private DashboardService dashboardService;
 


    @GetMapping
    public String verDashboard(Model model) {
        
        // 1. Cargar KPIs (Tarjetas Superiores)
        Map<String, Object> kpis = dashboardService.obtenerResumenKpis();
        model.addAllAttributes(kpis);

        // 2. Preparar datos para Gráfico de Ventas (Últimos 7 días)
        List<VentaPeriodoDTO> ventasDias = dashboardService.getVentasUltimos7Dias();
        List<String> diasLabels = new ArrayList<>();
        List<Double> diasData = new ArrayList<>();
        for(VentaPeriodoDTO v : ventasDias) {
            diasLabels.add(v.getEtiqueta());
            diasData.add(v.getTotal());
        }
        model.addAttribute("diasLabels", diasLabels);
        model.addAttribute("diasData", diasData);

        // 3. Preparar datos para Gráfico de Ventas (Mensual)
        List<VentaPeriodoDTO> ventasMes = dashboardService.getVentasPorMes();
        List<String> mesLabels = new ArrayList<>();
        List<Double> mesData = new ArrayList<>();
        for(VentaPeriodoDTO v : ventasMes) {
            mesLabels.add(v.getEtiqueta());
            mesData.add(v.getTotal());
        }
        model.addAttribute("mesLabels", mesLabels);
        model.addAttribute("mesData", mesData);

        // 4. Preparar datos para Top Productos
        List<ProductoTopDTO> topProds = dashboardService.getTopProductos();
        List<String> prodLabels = new ArrayList<>();
        List<Integer> prodData = new ArrayList<>();
        for(ProductoTopDTO p : topProds) {
            prodLabels.add(p.getNombre());
            prodData.add(p.getCantidad());
        }
        model.addAttribute("prodLabels", prodLabels);
        model.addAttribute("prodData", prodData);

        return "admin/dashboard"; // Nueva vista
    }
}