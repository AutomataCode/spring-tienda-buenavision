package utp.edu.pe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.TipoDocumento;
import utp.edu.pe.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    // PU-C01: Verificar el guardado de un cliente (Sin email ni imagen)
    @Test
    void testGuardarClienteExitoso() {
        // --- ARRANGE ---
        Cliente clienteInput = new Cliente();
        clienteInput.setNombre("Juan");
        clienteInput.setApellido("Pérez");
        clienteInput.setNumeroDocumento("12345678");
        clienteInput.setTipoDocumento(TipoDocumento.DNI);
        clienteInput.setDireccion("Av. Principal 123");
        // Relacionamos con un usuario dummy (el email va dentro del usuario, no del cliente)
        Usuario usuarioDummy = new Usuario(); 
        clienteInput.setUsuario(usuarioDummy);

        // Simulamos que al guardar, el repositorio devuelve el cliente con ID 1
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setIdCliente(1); 
            c.setEstado(EstadoGeneral.ACTIVO);
            return c;
        });

        // --- ACT ---
        Cliente resultado = clienteService.save(clienteInput);

        // --- ASSERT ---
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdCliente());
        assertEquals("Juan", resultado.getNombre());
        assertEquals("12345678", resultado.getNumeroDocumento());
        assertEquals(EstadoGeneral.ACTIVO, resultado.getEstado());
        
        verify(clienteRepository, times(1)).save(clienteInput);
    }

    // PU-C02: Verificar si existe un cliente por número de documento
    @Test
    void testExistsByNumeroDocumento() {
        String dni = "87654321";
        when(clienteRepository.existsByNumeroDocumento(dni)).thenReturn(true);

        boolean existe = clienteService.existsByNumeroDocumento(dni);

        assertTrue(existe);
        verify(clienteRepository).existsByNumeroDocumento(dni);
    }

    // PU-C03: Verificar búsqueda por ID
    @Test
    void testFindById() {
        Integer id = 10;
        Cliente clienteMock = new Cliente();
        clienteMock.setIdCliente(id);
        
        when(clienteRepository.findById(id)).thenReturn(Optional.of(clienteMock));

        Optional<Cliente> resultado = clienteService.findById(id);

        assertTrue(resultado.isPresent());
        assertEquals(id, resultado.get().getIdCliente());
    }
}