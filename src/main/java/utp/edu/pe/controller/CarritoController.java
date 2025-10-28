package utp.edu.pe.controller;

import utp.edu.pe.dto.Carrito;

// Tus imports de DTO, Servicio, Entidades, etc.

import utp.edu.pe.service.CarritoService;

// Imports de Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Imports de Spring Core y Web
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

 
import org.thymeleaf.TemplateEngine;
 
import org.thymeleaf.context.WebContext;

 
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

 

@Controller
@RequestMapping("/carrito")
public class CarritoController {
	
    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationContext applicationContext;  

    @PostMapping("/agregar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") int cantidad,
            HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            carritoService.agregarItem(productoId, cantidad);
            responseMap.put("success", true);
            responseMap.put("message", "Producto agregado al carrito.");
            responseMap.putAll(getCarritoResumenMap(request, response));
            return ResponseEntity.ok(responseMap);
        } catch (IllegalArgumentException e) {
             logger.warn("Intento fallido de agregar al carrito: {}", e.getMessage()); // Cambiado a WARN
             responseMap.put("success", false); responseMap.put("message", e.getMessage());
             return ResponseEntity.badRequest().body(responseMap);
        } catch (Exception e) {
             logger.error("Error inesperado al agregar producto al carrito", e);
             responseMap.put("success", false); responseMap.put("message", "Error interno al procesar la solicitud.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    @PostMapping("/actualizar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidad(
            @PathVariable Long productoId,
            @RequestParam int cantidad,
            HttpServletRequest request, HttpServletResponse response) {
         Map<String, Object> responseMap = new HashMap<>();
         try {
             if (cantidad <= 0) {
                 carritoService.eliminarItem(productoId);
                 responseMap.put("message", "Producto eliminado del carrito.");
             } else {
                 carritoService.actualizarCantidad(productoId, cantidad);
                 responseMap.put("message", "Cantidad actualizada.");
             }
             responseMap.put("success", true);
             responseMap.putAll(getCarritoResumenMap(request, response));
             return ResponseEntity.ok(responseMap);
         } catch (IllegalArgumentException e) {
             logger.warn("Intento fallido de actualizar cantidad: {}", e.getMessage()); // Cambiado a WARN
             responseMap.put("success", false); responseMap.put("message", e.getMessage());
             return ResponseEntity.badRequest().body(responseMap);
         } catch (Exception e) {
             logger.error("Error inesperado al actualizar cantidad en el carrito", e);
             responseMap.put("success", false); responseMap.put("message", "Error interno al procesar la solicitud.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
         }
    }

    @PostMapping("/eliminar/{productoId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(
            @PathVariable Long productoId,
            HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            carritoService.eliminarItem(productoId);
             responseMap.put("success", true);
             responseMap.put("message", "Producto eliminado del carrito.");
             responseMap.putAll(getCarritoResumenMap(request, response));
             return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
             logger.error("Error inesperado al eliminar producto del carrito", e);
             responseMap.put("success", false); responseMap.put("message", "Error interno al procesar la solicitud.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

     @PostMapping("/vaciar")
     @ResponseBody
     public ResponseEntity<Map<String, Object>> vaciarCarrito(
             HttpServletRequest request, HttpServletResponse response) {
          Map<String, Object> responseMap = new HashMap<>();
         try {
              carritoService.vaciarCarrito();
              responseMap.put("success", true);
              responseMap.put("message", "Carrito vaciado exitosamente.");
              responseMap.putAll(getCarritoResumenMap(request, response));
              return ResponseEntity.ok(responseMap);
         } catch (Exception e) {
              logger.error("Error inesperado al vaciar el carrito", e);
              responseMap.put("success", false); responseMap.put("message", "Error interno al procesar la solicitud.");
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
         }
     }

     
    @GetMapping("/resumen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCarritoResumen(
            HttpServletRequest request, HttpServletResponse response) {
        try {
             return ResponseEntity.ok(getCarritoResumenMap(request, response));
        } catch (Exception e) {
             // El error específico ya se loggea dentro de getCarritoResumenMap si falla el renderizado
             logger.error("Fallo general al obtener resumen del carrito", e); // Log adicional por si acaso
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("success", false);
             errorResponse.put("message", "No se pudo cargar el resumen del carrito.");
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

     
    @GetMapping("/checkout")
    public String irAlCheckout(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Checkout denegado: Usuario no autenticado.");
             redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para proceder al pago.");
            return "redirect:/login";
        }
         if (carritoService.getCarrito().getItems().isEmpty()) {
             logger.info("Checkout denegado: Carrito vacío para usuario {}", authentication.getName());
              redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío, añade productos antes de proceder.");
              return "redirect:/catalogo"; // Recomiendo redirigir al catálogo
         }
        logger.info("Usuario {} procediendo al checkout.", authentication.getName());
        return "redirect:/pedido/crear"; // O la URL de tu proceso de pedido
    }
    
    private Map<String, Object> getCarritoResumenMap(HttpServletRequest request, HttpServletResponse response) {
        Carrito carrito = carritoService.getCarrito();

        Map<String, Object> variables = new HashMap<>();
        variables.put("carrito", carrito);

        //  Crear el IWebExchange 
        var webApp = org.thymeleaf.web.servlet.JakartaServletWebApplication.buildApplication(servletContext);
        var webExchange = webApp.buildExchange(request, response);

        //  Crear el contexto 
        WebContext context = new WebContext(webExchange, request.getLocale(), variables);

        Map<String, Object> resumen = new HashMap<>();
        int itemCount = carrito.getTotalUnidades();
        resumen.put("itemCount", itemCount);

        try {
            Set<String> fragmentSelectors = new HashSet<>();
            fragmentSelectors.add("items");
            String carritoHtml = templateEngine.process("fragments/carrito", fragmentSelectors, context);
            resumen.put("carritoHtml", carritoHtml);
        } catch (Exception e) {
            logger.error("Error al procesar el fragmento del carrito: {}", e.getMessage(), e);
            resumen.put("carritoHtml", "<div class='alert alert-danger m-3'>Error al renderizar los items del carrito.</div>");
        }

        return resumen;
    }

     
}
