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
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
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

import utp.edu.pe.entity.Producto;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.Rolx;
import utp.edu.pe.service.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ProductoService productoService;
    @MockBean private InventarioExcelService inventarioExcelService;
    @MockBean private CategoriaProductoService categoriaProductoService;
    @MockBean private MarcaService marcaService;
    @MockBean private FormaMonturaService formaMonturaService;
    @MockBean private MaterialMonturaService materialMonturaService;

    // --- CONFIGURACIÓN DEL MOCK ADMIN (Igual que hicimos en Cliente) ---
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
    @interface WithMockAdmin {
        String username() default "admin";
    }

    static class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {
        @Override
        public SecurityContext createSecurityContext(WithMockAdmin annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            Usuario usuario = new Usuario();
            usuario.setUsername(annotation.username());
            usuario.setEmail("admin@tienda.com");
            usuario.setNombreCompleto("Administrador");
            usuario.setPassword("pass");
            usuario.setRol(Rolx.ADMIN);
            usuario.setEstado(EstadoGeneral.ACTIVO);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    usuario, "pass", usuario.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }
    }
    // -------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Mockeamos las listas vacías para que no falle al cargar los <select> del formulario
        when(categoriaProductoService.findAll()).thenReturn(new ArrayList<>());
        when(marcaService.findAll()).thenReturn(new ArrayList<>());
        when(formaMonturaService.findAll()).thenReturn(new ArrayList<>());
        when(materialMonturaService.findAll()).thenReturn(new ArrayList<>());
    }

    // PI-P01: Listar Productos (Acceso Admin)
    @Test
    @WithMockAdmin
    void testListarProductos() throws Exception {
        Page<Producto> paginaVacia = new PageImpl<>(new ArrayList<>());
        when(productoService.findAll(any(Pageable.class))).thenReturn(paginaVacia);

        mockMvc.perform(get("/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/productos/index"))
                .andExpect(model().attributeExists("paginaProductos"));
    }

    // PI-P02: Cargar Formulario Nuevo (Verificar selects cargados)
    @Test
    @WithMockAdmin
    void testMostrarFormularioNuevo() throws Exception {
        mockMvc.perform(get("/admin/productos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/productos/form"))
                .andExpect(model().attributeExists("producto"))
                .andExpect(model().attributeExists("categorias")) // Verifica que se carguen los selects
                .andExpect(model().attributeExists("marcas"));
    }

    // PI-P03: Guardar Producto Exitosamente
    @Test
    @WithMockAdmin
    void testGuardarProductoExitoso() throws Exception {
        mockMvc.perform(post("/admin/productos/guardar")
                .param("nombre", "Nuevo Producto")
                .param("sku", "NEW-001")
                .param("precioVenta", "150.00")
                .param("stockActual", "10")
                // Parámetros de relaciones (necesarios para evitar errores de binding si son @NotNull)
                // Si tu entidad valida @NotNull en categoria/marca, descomenta lo siguiente:
                // .param("categoria.id", "1") 
                // .param("marca.id", "1") 
                .with(csrf())) // Token CSRF obligatorio
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // PI-P04: Eliminar Producto (Soft Delete)
    @Test
    @WithMockAdmin
    void testEliminarProducto() throws Exception {
        mockMvc.perform(post("/admin/productos/eliminar/{id}", 1L)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}