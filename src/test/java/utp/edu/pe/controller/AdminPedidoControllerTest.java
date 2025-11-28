package utp.edu.pe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.web.servlet.MockMvc;

import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.Pedido;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.EstadoPedido;
import utp.edu.pe.entity.enums.Rolx;
import utp.edu.pe.service.PedidoService;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    // --- MOCK ADMIN USER ---
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockAdminPedidoSecurityContextFactory.class)
    @interface WithMockAdminPedido {}

    static class WithMockAdminPedidoSecurityContextFactory implements WithSecurityContextFactory<WithMockAdminPedido> {
        @Override
        public SecurityContext createSecurityContext(WithMockAdminPedido annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            Usuario usuario = new Usuario();
            usuario.setUsername("admin");
            usuario.setEmail("admin@tienda.com");
            usuario.setNombreCompleto("Admin Pedidos");
            usuario.setRol(Rolx.ADMIN);
            usuario.setEstado(EstadoGeneral.ACTIVO);
            usuario.setPassword("pass");

            Authentication auth = new UsernamePasswordAuthenticationToken(usuario, "pass", usuario.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }
    }
    // -----------------------

    // PI-PE01: Listar Pedidos
    @Test
    @WithMockAdminPedido
    void testListarPedidos() throws Exception {
        Page<Pedido> paginaVacia = new PageImpl<>(new ArrayList<>());
        when(pedidoService.findAllPedidosAdmin(any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/admin/pedidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pedidos/index"))
                .andExpect(model().attributeExists("paginaPedidos"));
    }

    // PI-PE02: Ver Detalle de Pedido Existente

 // PI-PE02: Ver Detalle de Pedido Existente
    @Test
    @WithMockAdminPedido
    void testVerDetallePedido() throws Exception {
        Long pedidoId = 1L;
        
        // 1. Creamos el Pedido Mock
        Pedido pedidoMock = new Pedido();
        pedidoMock.setIdPedido(pedidoId);
        pedidoMock.setNumeroPedido("PED-0001"); // Agregamos número por si la vista lo pide
        pedidoMock.setEstado(EstadoPedido.PENDIENTE);
        
        // 2. SOLUCIÓN: Crear y asignar un Cliente Mock con datos
        // Thymeleaf necesita leer pedido.cliente.nombre y pedido.cliente.apellido
        Cliente clienteMock = new Cliente();
        clienteMock.setNombre("Juan");
        clienteMock.setApellido("Pérez");
        clienteMock.setTelefono("987654321");
        
        // Si tu vista muestra el email, asigna también el usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("juan.perez@test.com");
        clienteMock.setUsuario(usuarioMock);
        
        // Asignamos el cliente al pedido
        pedidoMock.setCliente(clienteMock); 

        // 3. Simulamos la respuesta del servicio
        when(pedidoService.findByIdAdmin(pedidoId)).thenReturn(Optional.of(pedidoMock));

        // 4. Ejecutamos la prueba
        mockMvc.perform(get("/admin/pedidos/detalle/{id}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pedidos/detalle"))
                .andExpect(model().attributeExists("pedido"))
                .andExpect(model().attributeExists("todosLosEstados"));
    }

    // PI-PE03: Actualizar Estado de Pedido (Exitoso)
    @Test
    @WithMockAdminPedido
    void testActualizarEstadoExitoso() throws Exception {
        mockMvc.perform(post("/admin/pedidos/actualizar-estado")
                .param("pedidoId", "1")
                .param("nuevoEstado", "ENTREGADO")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/detalle/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // PI-PE04: Intento de Actualizar Pedido Finalizado (Error de Negocio)
    @Test
    @WithMockAdminPedido
    void testActualizarEstadoError() throws Exception {
        // Simulamos que el servicio lanza excepción porque el pedido ya estaba finalizado
        when(pedidoService.actualizarEstadoPedido(any(), any(), any()))
            .thenThrow(new IllegalStateException("El pedido ya está finalizado"));

        mockMvc.perform(post("/admin/pedidos/actualizar-estado")
                .param("pedidoId", "1")
                .param("nuevoEstado", "CANCELADO")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/detalle/1"))
                .andExpect(flash().attributeExists("errorMessage")); // Verifica que muestre el error
    }
}