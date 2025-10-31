package utp.edu.pe.controller;


import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.service.PedidoService;


@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {
	
	@Autowired
	private  PedidoService pedidoService;
	
    private static final Logger log = LoggerFactory.getLogger(AdminPedidoController.class);
    
    

    @GetMapping
    public String listarPedidos(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
        Page<Pedido> paginaPedidos = pedidoService.findAllPedidosAdmin(pageable);
        
        model.addAttribute("paginaPedidos", paginaPedidos);
        return "admin/pedidos/index"; 
    }

  
    @GetMapping("/detalle/{id}")
    public String verDetallePedido(@PathVariable("id") Long pedidoId, Model model, RedirectAttributes attributes) {
        
        Optional<Pedido> pedidoOpt = pedidoService.findByIdAdmin(pedidoId);

        if (pedidoOpt.isEmpty()) {
            attributes.addFlashAttribute("errorMessage", "Pedido no encontrado.");
            return "redirect:/admin/pedidos";
        }

        model.addAttribute("pedido", pedidoOpt.get());
        model.addAttribute("todosLosEstados", EstadoPedido.values()); 
        
        return "admin/pedidos/detalle"; 
    }
   
    @PostMapping("/actualizar-estado")
    public String actualizarEstado(@RequestParam("pedidoId") Long pedidoId,
                                 @RequestParam("nuevoEstado") EstadoPedido nuevoEstado,
                                 @AuthenticationPrincipal Usuario adminUsuario,
                                 RedirectAttributes attributes) {
        try {
            pedidoService.actualizarEstadoPedido(pedidoId, nuevoEstado, adminUsuario);
            attributes.addFlashAttribute("successMessage", "Estado del pedido actualizado a " + nuevoEstado.name());
        
        } catch (IllegalStateException e) {
            log.warn("Error al actualizar estado: {}", e.getMessage());
            attributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al actualizar estado de pedido {}", pedidoId, e);
            attributes.addFlashAttribute("errorMessage", "Error inesperado al actualizar el estado.");
        }

        return "redirect:/admin/pedidos/detalle/" + pedidoId;
    }
    

}
