package utp.edu.pe.controller;
import utp.edu.pe.TiendaBuenaVisionApplication;
import utp.edu.pe.dto.Carrito;
import utp.edu.pe.service.CarritoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext; // Necesario para SpringWebContext
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.spring6.context.SpringWebContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Ya no necesitas importar Locale explícitamente si usas request.getLocale()


@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final TiendaBuenaVisionApplication tiendaBuenaVisionApplication;

    private static final Logger logger = LoggerFactory.getLogger(CarritoController.class);

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ServletContext servletContext;

    // Inyecta ApplicationContext
    @Autowired
    private ApplicationContext applicationContext;

    CarritoController(TiendaBuenaVisionApplication tiendaBuenaVisionApplication) {
        this.tiendaBuenaVisionApplication = tiendaBuenaVisionApplication;
    }

    // --- Endpoints AJAX (Agregar, Actualizar, Eliminar, Vaciar) ---
    // (Estos métodos no cambian, solo necesitan pasar request y response
    // a getCarritoResumenMap como ya lo hacían)

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
            responseMap.put("message", "Producto agregado");
            responseMap.putAll(getCarritoResumenMap(request, response));
            return ResponseEntity.ok(responseMap);
        } catch (IllegalArgumentException e) {
             logger.error("Error al agregar: {}", e.getMessage());
             responseMap.put("success", false); responseMap.put("message", e.getMessage());
             return ResponseEntity.badRequest().body(responseMap);
        } catch (Exception e) {
             logger.error("Error inesperado al agregar", e);
             responseMap.put("success", false); responseMap.put("message", "Error interno.");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    // ... (actualizarCantidad, eliminarDelCarrito, vaciarCarrito - asegúrate que pasen request, response) ...

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
                  responseMap.put("message", "Producto eliminado");
              } else {
                  carritoService.actualizarCantidad(productoId, cantidad);
                  responseMap.put("message", "Cantidad actualizada");
              }
              responseMap.put("success", true);
              responseMap.putAll(getCarritoResumenMap(request, response));
              return ResponseEntity.ok(responseMap);
          } catch (IllegalArgumentException e) {
              logger.error("Error al actualizar: {}", e.getMessage());
              responseMap.put("success", false); responseMap.put("message", e.getMessage());
              return ResponseEntity.badRequest().body(responseMap);
          } catch (Exception e) {
              logger.error("Error inesperado al actualizar", e);
              responseMap.put("success", false); responseMap.put("message", "Error interno.");
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
              responseMap.put("message", "Producto eliminado");
              responseMap.putAll(getCarritoResumenMap(request, response));
              return ResponseEntity.ok(responseMap);
         } catch (Exception e) {
              logger.error("Error inesperado al eliminar", e);
              responseMap.put("success", false); responseMap.put("message", "Error interno.");
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
               responseMap.put("message", "Carrito vaciado");
               responseMap.putAll(getCarritoResumenMap(request, response));
               return ResponseEntity.ok(responseMap);
          } catch (Exception e) {
               logger.error("Error inesperado al vaciar", e);
               responseMap.put("success", false); responseMap.put("message", "Error interno.");
               return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
          }
      }


    // Endpoint GET /resumen
    @GetMapping("/resumen")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCarritoResumen(
            HttpServletRequest request, HttpServletResponse response) {
        try {
             return ResponseEntity.ok(getCarritoResumenMap(request, response));
        } catch (Exception e) {
             logger.error("Error al obtener resumen", e);
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("success", false);
             errorResponse.put("message", "Error al cargar resumen del carrito.");
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Endpoint GET /checkout (sin cambios)
    @GetMapping("/checkout")
    public String irAlCheckout(RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Checkout denegado: Usuario no autenticado.");
             redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para proceder al pago.");
            return "redirect:/login";
        }
         if (carritoService.getCarrito().getItems().isEmpty()) {
             logger.info("Checkout denegado: Carrito vacío.");
              redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío.");
              return "redirect:/catalogo";
         }
        logger.info("Usuario {} procediendo al checkout.", authentication.getName());
        return "redirect:/pedido/crear";
    }

    // --- Método auxiliar para generar el resumen ---
     private Map<String, Object> getCarritoResumenMap(HttpServletRequest request, HttpServletResponse response) {
         Carrito carrito = carritoService.getCarrito();
         final Map<String, Object> variables = new HashMap<>();
         variables.put("carrito", carrito);

         // ✅ USA SpringWebContext
         final SpringWebContext context = new SpringWebContext(
                                             request,
                                             response,
                                             servletContext,
                                             request.getLocale(),
                                             variables,
                                             applicationContext); // Pasa el ApplicationContext

         Map<String, Object> resumen = new HashMap<>();
         int itemCount = carrito.getTotalUnidades();
         resumen.put("itemCount", itemCount);
         String carritoHtml = ""; // Inicializa

         try {
             Set<String> fragmentSelectors = new HashSet<>();
             fragmentSelectors.add("items"); // Nombre del fragmento
             // Intenta procesar siempre, el th:if dentro del fragmento maneja el caso vacío
             carritoHtml = templateEngine.process("fragments/carrito", fragmentSelectors, context);
             resumen.put("carritoHtml", carritoHtml);

             // Log solo si no está vacío para claridad
             if (itemCount > 0) {
                logger.debug("Procesando fragmento HTML para carrito con {} items.", itemCount);
             } else {
                 logger.debug("Procesado fragmento HTML para carrito vacío.");
             }

         } catch (Exception e) {
             logger.error("Error crítico al procesar el fragmento del carrito: {}", e.getMessage(), e);
             // Devuelve un error HTML claro si falla el renderizado
             resumen.put("carritoHtml", "<div class='alert alert-danger m-3'>Error al renderizar el carrito. Intente recargar la página.</div>");
             // Asegúrate que itemCount sea 0 si falla el renderizado completo
             resumen.put("itemCount", 0);
         }

         return resumen;
     }
}