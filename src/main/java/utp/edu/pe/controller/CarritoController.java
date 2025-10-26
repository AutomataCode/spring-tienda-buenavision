package utp.edu.pe.controller;


import utp.edu.pe.dto.Carrito;
import utp.edu.pe.service.CarritoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;



import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class); // Logback

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private TemplateEngine templateEngine; // Para renderizar fragmentos HTML

    // Endpoint AJAX para AGREGAR
    @PostMapping("/agregar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Map<String, Object> response = new HashMap<>();
        try {
            carritoService.agregarItem(productoId, cantidad);
            response.put("success", true);
            response.put("message", "Producto agregado");
            response.putAll(getCarritoResumenMap()); // Devuelve conteo y HTML
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al agregar al carrito: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
             logger.error("Error inesperado al agregar", e);
             response.put("success", false);
             response.put("message", "Error interno.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint AJAX para ACTUALIZAR cantidad
    @PostMapping("/actualizar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidad(
            @PathVariable Long productoId,
            @RequestParam int cantidad) {
         Map<String, Object> response = new HashMap<>();
         try {
             if (cantidad <= 0) {
                 carritoService.eliminarItem(productoId);
                 response.put("message", "Producto eliminado");
             } else {
                 carritoService.actualizarCantidad(productoId, cantidad);
                 response.put("message", "Cantidad actualizada");
             }
             response.put("success", true);
             response.putAll(getCarritoResumenMap());
             return ResponseEntity.ok(response);
         } catch (IllegalArgumentException e) {
             logger.error("Error al actualizar: {}", e.getMessage());
             response.put("success", false);
             response.put("message", e.getMessage());
             return ResponseEntity.badRequest().body(response);
         } catch (Exception e) {
             logger.error("Error inesperado al actualizar", e);
             response.put("success", false);
             response.put("message", "Error interno.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
         }
    }

    // Endpoint AJAX para ELIMINAR item
    @PostMapping("/eliminar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(@PathVariable Long productoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            carritoService.eliminarItem(productoId);
             response.put("success", true);
             response.put("message", "Producto eliminado");
             response.putAll(getCarritoResumenMap());
             return ResponseEntity.ok(response);
        } catch (Exception e) {
             logger.error("Error inesperado al eliminar", e);
             response.put("success", false);
             response.put("message", "Error interno.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

     // Endpoint AJAX para VACIAR carrito
    @PostMapping("/vaciar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vaciarCarrito() {
         Map<String, Object> response = new HashMap<>();
        try {
             carritoService.vaciarCarrito();
             response.put("success", true);
             response.put("message", "Carrito vaciado");
             response.putAll(getCarritoResumenMap());
             return ResponseEntity.ok(response);
        } catch (Exception e) {
             logger.error("Error inesperado al vaciar", e);
             response.put("success", false);
             response.put("message", "Error interno.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint AJAX para OBTENER RESUMEN (conteo y HTML)
    @GetMapping("/resumen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCarritoResumen() {
        try {
             return ResponseEntity.ok(getCarritoResumenMap());
        } catch (Exception e) {
             logger.error("Error al obtener resumen", e);
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("success", false);
             errorResponse.put("message", "Error al cargar.");
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Endpoint para IR AL CHECKOUT (verifica login)
    @GetMapping("/checkout")
    public String irAlCheckout(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Checkout denegado: Usuario no autenticado.");
             redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para proceder al pago.");
            // Opcional: guardar URL destino para redirigir después del login
            // request.getSession().setAttribute("url_previo_login", "/carrito/checkout");
            return "redirect:/login";
        }

         if (carritoService.getCarrito().getItems().isEmpty()) {
             logger.info("Checkout denegado: Carrito vacío.");
              redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío.");
              return "redirect:/catalogo";
         }

        logger.info("Usuario {} procediendo al checkout.", authentication.getName());
        // Redirige a la página donde se crea el Pedido (necesitarás crear este controlador/vista)
        return "redirect:/pedido/crear";
    }

     // --- Método auxiliar para generar el resumen ---
     private Map<String, Object> getCarritoResumenMap() {
         Carrito carrito = carritoService.getCarrito();
         Context context = new Context();
         context.setVariable("carrito", carrito);

         // Renderiza el fragmento 'items' de 'fragments/carrito.html'
         String carritoHtml = templateEngine.process("fragments/carrito :: items", context); // Añade ':: items'

         Map<String, Object> resumen = new HashMap<>();
         resumen.put("itemCount", carrito.getTotalUnidades());
         resumen.put("carritoHtml", carritoHtml);
         return resumen;
     }
}