package utp.edu.pe.controller;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import utp.edu.pe.dto.Carrito;
import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario; // Necesitas la entidad Usuario
import utp.edu.pe.exception.StockInsuficienteException;

import utp.edu.pe.service.CarritoService;
import utp.edu.pe.service.ClienteService;
import utp.edu.pe.service.PedidoService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ClienteService clienteService; // Para obtener datos del cliente


    @GetMapping("/crear")
    public String mostrarFormularioPedido(@AuthenticationPrincipal Usuario usuario, Model model, RedirectAttributes redirectAttributes) {
        Carrito carrito = carritoService.getCarrito();
        if (usuario == null) {
            // Seguridad adicional por si se accede directamente sin estar logueado
             redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para crear un pedido.");
             return "redirect:/login";
        }
        if (carrito.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío.");
            return "redirect:/catalogo";
        }
        // Obtener el cliente asociado al usuario logueado
        Optional<Cliente> clienteOpt = clienteService.findByUsuarioId(usuario.getIdUsuario());
        if (clienteOpt.isEmpty()) {
            logger.error("Usuario ID {} no tiene un cliente asociado.", usuario.getIdUsuario());
             redirectAttributes.addFlashAttribute("error", "Error interno: No se encontraron datos de cliente. Contacte soporte.");
             return "redirect:/catalogo"; // O a una página de error/perfil
        }
        Cliente cliente = clienteOpt.get();
        // Calcular totales (deberían coincidir con los del carrito, pero recalculamos por seguridad)
        BigDecimal subtotal = carrito.getSubtotalTotal();
        BigDecimal igv = subtotal.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv); // Asumiendo sin descuento inicial

        model.addAttribute("carrito", carrito);
        model.addAttribute("cliente", cliente);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("igv", igv);
        model.addAttribute("total", total);
        // Nombre de la vista Thymeleaf
        return "pedido/formulario-pedido";
    }

    /**
     * Procesa la creación del pedido desde el formulario.
     */
    @PostMapping("/guardar")
    public String guardarPedido(@AuthenticationPrincipal Usuario usuario,
                              @RequestParam("direccionEntrega") String direccionEntrega,
                              @RequestParam(value = "observacionesCliente", required = false) String observacionesCliente,
                              RedirectAttributes redirectAttributes) {

        if (usuario == null) {
            return "redirect:/login"; // Seguridad
        }

        try {
            Pedido nuevoPedido = pedidoService.crearPedidoDesdeCarrito(usuario, direccionEntrega, observacionesCliente);
            logger.info("Pedido #{} guardado exitosamente.", nuevoPedido.getNumeroPedido());

            // Mensaje de éxito para la página de confirmación
            redirectAttributes.addFlashAttribute("successMessage", "¡Tu pedido #" + nuevoPedido.getNumeroPedido() + " ha sido registrado con éxito!");

            // Redirige a la página de confirmación con el ID del pedido
            return "redirect:/pedido/confirmacion/" + nuevoPedido.getIdPedido();

        } catch (StockInsuficienteException e) {
            logger.warn("Error de stock al intentar guardar pedido: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear el pedido: " + e.getMessage());
            // Vuelve al formulario de creación (la vista /pedido/crear mostrará el mensaje)
            return "redirect:/pedido/crear";
        } catch (IllegalStateException e) {
            logger.warn("Error de estado al intentar guardar pedido (ej. carrito vacío): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
             return "redirect:/catalogo"; 
        } catch (Exception e) {
            logger.error("Error inesperado al guardar el pedido", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error inesperado al procesar tu pedido. Inténtalo de nuevo.");
            return "redirect:/pedido/crear";
        }
    }

    /**
     * Muestra la página de confirmación del pedido.
     */
    @GetMapping("/confirmacion/{pedidoId}")
    public String mostrarConfirmacionPedido(@PathVariable Long pedidoId,
                                          @AuthenticationPrincipal Usuario usuario,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {

         if (usuario == null) {
             return "redirect:/login";
         }

        Optional<Pedido> pedidoOpt = pedidoService.findById(pedidoId);

        if (pedidoOpt.isEmpty()) {
            logger.warn("Intento de ver confirmación de pedido no existente: ID {}", pedidoId);
            redirectAttributes.addFlashAttribute("errorMessage", "El pedido solicitado no existe.");
            return "redirect:/public"; // O a la página principal
        }

        Pedido pedido = pedidoOpt.get();

        // Verificar que el pedido pertenezca al usuario logueado
        if (!pedido.getCliente().getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
             logger.warn("Usuario ID {} intentó acceder a pedido ID {} de otro cliente.", usuario.getIdUsuario(), pedidoId);
             redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para ver este pedido.");
              return "redirect:/public";
        }

        model.addAttribute("pedido", pedido);
        // El mensaje de éxito se recibe vía flash attribute del método guardarPedido

        return "pedido/confirmacion-pedido"; // Nombre de la vista de confirmación
    }
    
    
    @GetMapping("/mis-pedidos")
    public String verMisPedidos(
            Model model,
            @AuthenticationPrincipal Usuario usuario, // <-- Tu patrón
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        if (usuario == null) {
            return "redirect:/login";
        }

        // Pasa el usuario directamente al servicio
        List<Pedido> pedidos = pedidoService.findByUsuarioAndFechas(usuario, fechaInicio, fechaFin);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "pedido/mis-pedidos"; // La nueva vista
    }

    @GetMapping("/mis-pedidos/detalle/{id}")
    public String verDetallePedido(@PathVariable("id") Long pedidoId,
                                   Model model,
                                   @AuthenticationPrincipal Usuario usuario) { // <-- Tu patrón
        
        if (usuario == null) {
            return "redirect:/login";
        }

        // El servicio se encarga de todo: buscar Y validar que te pertenece
        Optional<Pedido> pedidoOpt = pedidoService.findByIdAndUsuario(pedidoId, usuario);

        if (pedidoOpt.isEmpty()) {
            logger.warn("Usuario ID {} intentó ver detalle de pedido ID {} que no le pertenece o no existe.", usuario.getIdUsuario(), pedidoId);
            return "redirect:/mis-pedidos?error=noencontrado";
        }

        model.addAttribute("pedido", pedidoOpt.get());
        return "pedido/detalle-pedido"; // La nueva vista de detalle
    }
    
    
 
}
