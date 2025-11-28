package utp.edu.pe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import utp.edu.pe.dto.RegistroClienteDTO;
import utp.edu.pe.service.ClienteService;
import utp.edu.pe.service.UsuarioService;

@SpringBootTest
@AutoConfigureMockMvc
public class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ClienteService clienteService;

    // PI-C01: Verificar que la página de registro carga correctamente
    @Test
    void testMostrarFormularioRegistro() throws Exception {
        mockMvc.perform(get("/public/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("public/registrar-usuario"))
                .andExpect(model().attributeExists("registroDto"));
    }

    // PI-C02: Verificar un registro exitoso (Sin imágenes, el email va en el DTO)
    @Test
    void testProcesarRegistroExitoso() throws Exception {
        // Simulamos que NO existen duplicados
        when(usuarioService.existsByUsername(anyString())).thenReturn(false);
        when(usuarioService.existsByEmail(anyString())).thenReturn(false);
        when(clienteService.existsByNumeroDocumento(anyString())).thenReturn(false);

        mockMvc.perform(post("/public/registro")
                // Datos del DTO (RegistroClienteDTO)
                .param("nombre", "Carlos")
                .param("apellido", "López")
                .param("numeroDocumento", "44556677")
                .param("telefono", "999888777")
                .param("direccion", "Calle Lima 123")
                .param("username", "carloslopez")
                .param("email", "carlos@correo.com") // El email se envía como parámetro del form
                .param("password", "123456")
                .param("confirmPassword", "123456")
                .with(csrf())) // Token de seguridad obligatorio para POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("registroExitoso"));
    }

    // PI-C03: Verificar fallo por contraseñas no coincidentes
    @Test
    void testRegistroPasswordNoCoincide() throws Exception {
        mockMvc.perform(post("/public/registro")
                .param("nombre", "Test")
                .param("apellido", "ApellidoTest") // FALTABA ESTE
                .param("numeroDocumento", "12345678") // FALTABA ESTE
                .param("username", "testuser")
                .param("email", "test@correo.com")
                .param("password", "123456")
                .param("confirmPassword", "654321") // Contraseñas diferentes
                .with(csrf()))
                .andExpect(status().isOk()) 
                .andExpect(view().name("public/registrar-usuario"))
                .andExpect(model().attributeHasFieldErrors("registroDto", "confirmPassword"));
    }

    // PI-C04: Verificar validación de DNI duplicado
    @Test
    void testRegistroDniDuplicado() throws Exception {
        // Simulamos que el DNI ya existe
        when(clienteService.existsByNumeroDocumento("44556677")).thenReturn(true);

        mockMvc.perform(post("/public/registro")
                .param("nombre", "Usuario")
                .param("apellido", "ApellidoUser") // FALTABA ESTE
                .param("username", "userdni")      // FALTABA ESTE
                .param("email", "dni@test.com")    // FALTABA ESTE
                .param("numeroDocumento", "44556677") // DNI existente
                .param("password", "123456")
                .param("confirmPassword", "123456")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public/registrar-usuario"))
                .andExpect(model().attributeHasFieldErrors("registroDto", "numeroDocumento"));
    }
}