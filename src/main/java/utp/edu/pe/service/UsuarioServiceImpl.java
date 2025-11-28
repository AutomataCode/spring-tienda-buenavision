package utp.edu.pe.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utp.edu.pe.dto.RegistroClienteDTO;
import utp.edu.pe.entity.Cliente;
import utp.edu.pe.entity.Usuario;
import utp.edu.pe.entity.enums.EstadoGeneral;
import utp.edu.pe.entity.enums.Rolx;
import utp.edu.pe.repository.ClienteRepository;
import utp.edu.pe.repository.UsuarioRepository;


@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService{
	
	private final UsuarioRepository usuarioRepository;
	
	private final ClienteRepository clienteRepository; 
    private final PasswordEncoder passwordEncoder;
    
    public static final int MAX_INTENTOS_FALLIDOS = 3;
	
    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,ClienteRepository clienteRepository, 
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }
	@Override
    @Transactional(readOnly = true) 
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public Usuario registrar(Usuario usuario) {
        // La lógica de negocio (codificar contraseña, setear rol)
        // se maneja en el controlador por ahora.
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByResetToken(String resetToken) {
        return usuarioRepository.findByResetToken(resetToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findByEstado(EstadoGeneral estado) {
        return usuarioRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        // Este método simplemente guarda.

        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Integer id) {
        usuarioRepository.deleteById(id);
    }

	@Override
	public Usuario registrarCliente(RegistroClienteDTO dto) {
		// 1. Crear el objeto Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword())); // Codificamos
        usuario.setNombreCompleto(dto.getNombre() + " " + dto.getApellido()); // Combinamos
        usuario.setRol(Rolx.CLIENTE); // Rol por defecto
        usuario.setEstado(EstadoGeneral.ACTIVO); // Estado por defecto
        usuario.setFechaCreacion(java.time.LocalDateTime.now());

        //  Crear el objeto Cliente
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTipoDocumento(dto.getTipoDocumento());
        cliente.setNumeroDocumento(dto.getNumeroDocumento());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        
        // VINCULAR AMBOS OBJETOS

        usuario.setCliente(cliente);
        cliente.setUsuario(usuario);

        //  Guardar
        // Gracias a `cascade = CascadeType.ALL` en la entidad Usuario,
        // al guardar el 'usuario', JPA también guardará automáticamente el 'cliente'
        // y manejará la asignación de la clave foránea.
        return usuarioRepository.save(usuario);
	}
	@Override
	public void aumentarIntentosFallidos(Usuario usuario) {
		int nuevosIntentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(nuevosIntentos);
        usuarioRepository.save(usuario);
		
	}
	@Override
	public void resetearIntentosFallidos(Usuario usuario) {
		usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueada(false);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);
		
	}
	@Override
	public void bloquear(Usuario usuario) {
		usuario.setCuentaBloqueada(true);
        usuario.setFechaBloqueo(new Date());
        usuarioRepository.save(usuario);
		
	}
}


