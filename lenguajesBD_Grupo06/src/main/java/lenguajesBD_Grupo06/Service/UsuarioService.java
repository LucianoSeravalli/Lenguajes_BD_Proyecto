package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.*;
import exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
 
/**
 * Registro, verificacion de correo y asignacion de roles de aplicacion.
 * Solo usa JPA: aqui no hay procedimientos con COMMIT propio, por lo que
 * los metodos pueden ser transaccionales sin riesgo.
 */
@Service
public class UsuarioService {
 
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int HORAS_VIGENCIA_TOKEN = 24;
 
    private final UsuarioRepository usuarioRepository;
    private final AppRolRepository appRolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
 
    public UsuarioService(UsuarioRepository usuarioRepository,
                          AppRolRepository appRolRepository,
                          UsuarioRolRepository usuarioRolRepository,
                          ClienteRepository clienteRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.appRolRepository = appRolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }
 
    /**
     * Alta de cliente: crea el USUARIO, le asigna el rol CLIENTE y recien
     * despues inserta el perfil en CLIENTE. El orden importa porque
     * TRG_CLIENTE_VALIDAR_ROL exige que el rol ya exista (ORA-20011).
     *
     * @return el token en claro que hay que enviar por correo. En la base
     *         solo queda su hash.
     */
    @Transactional
    public String registrarCliente(String nombre, String apellido, String telefono,
                                   String correo, String passwordEnClaro,
                                   String cedula, java.time.LocalDate fechaNacimiento) {
 
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new GymCoreException(0, "Ya existe una cuenta con ese correo.");
        }
        if (clienteRepository.existsByCedula(cedula)) {
            throw new GymCoreException(0, "Ya existe un cliente con esa cedula.");
        }
 
        String token = generarToken();
 
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(telefono);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(passwordEnClaro));
        usuario.setTokenConfirmacionHash(hashear(token));
        usuario.setTokenExpiracion(LocalDateTime.now().plusHours(HORAS_VIGENCIA_TOKEN));
        usuario.setCorreoVerificado("N");
        usuario.setEstado("activo");
        usuario = usuarioRepository.saveAndFlush(usuario);
 
        asignarRolInterno(usuario, "CLIENTE");
 
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setCedula(cedula);
        cliente.setFechaNacimiento(fechaNacimiento);
        cliente.setEstado("activo");
        clienteRepository.save(cliente);
 
        return token;
    }
 
    /** Alta de entrenador. No crea fila en CLIENTE. */
    @Transactional
    public Usuario registrarEntrenador(String nombre, String apellido, String telefono,
                                       String correo, String passwordEnClaro,
                                       String especialidad, java.time.LocalDate fechaContratacion) {
 
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new GymCoreException(0, "Ya existe una cuenta con ese correo.");
        }
 
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setTelefono(telefono);
        usuario.setCorreo(correo);
        usuario.setPasswordHash(passwordEncoder.encode(passwordEnClaro));
        usuario.setEspecialidad(especialidad);
        usuario.setFechaContratacion(fechaContratacion);
        usuario.setCorreoVerificado("S");
        usuario.setEstado("activo");
        usuario = usuarioRepository.saveAndFlush(usuario);
 
        asignarRolInterno(usuario, "ENTRENADOR");
        return usuario;
    }
 
    /** Confirma la cuenta a partir del token que llego por correo. */
    @Transactional
    public void confirmarCorreo(String tokenEnClaro) {
        Usuario usuario = usuarioRepository
                .findByTokenConfirmacionHashAndTokenExpiracionAfter(
                        hashear(tokenEnClaro), LocalDateTime.now())
                .orElseThrow(() -> new GymCoreException(0,
                        "El enlace de confirmacion no es valido o ya vencio."));
 
        usuario.setCorreoVerificado("S");
        // CK_USUARIO_TOKEN exige que hash y expiracion se limpien juntos.
        usuario.setTokenConfirmacionHash(null);
        usuario.setTokenExpiracion(null);
        usuarioRepository.save(usuario);
    }
 
    /** Verificacion de credenciales para el login. */
    @Transactional(readOnly = true)
    public Usuario autenticar(String correo, String passwordEnClaro) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new GymCoreException(0, "Correo o contrasena incorrectos."));
 
        if (!passwordEncoder.matches(passwordEnClaro, usuario.getPasswordHash())) {
            throw new GymCoreException(0, "Correo o contrasena incorrectos.");
        }
        if (!"activo".equals(usuario.getEstado())) {
            throw new GymCoreException(0, "La cuenta esta " + usuario.getEstado() + ".");
        }
        if (!"S".equals(usuario.getCorreoVerificado())) {
            throw new GymCoreException(0, "Debe confirmar su correo antes de ingresar.");
        }
        return usuario;
    }
 
    @Transactional(readOnly = true)
    public List<Usuario> listarEntrenadores() {
        return usuarioRepository.findActivosPorRol("ENTRENADOR");
    }
 
    @Transactional(readOnly = true)
    public boolean tieneRol(Long idUsuario, String nombreRol) {
        return usuarioRepository.tieneRol(idUsuario, nombreRol);
    }
 
    /** Desactiva la cuenta sin borrarla, para no romper el historial. */
    @Transactional
    public void cambiarEstado(Long idUsuario, String nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new GymCoreException(0, "El usuario no existe."));
        usuario.setEstado(nuevoEstado);
        usuarioRepository.save(usuario);
    }
 
    private void asignarRolInterno(Usuario usuario, String nombreRol) {
        AppRol rol = appRolRepository.findByNombreRolIgnoreCase(nombreRol)
                .orElseThrow(() -> new GymCoreException(0,
                        "El rol " + nombreRol + " no existe en APP_ROL."));
        usuarioRolRepository.saveAndFlush(new Usuario_rol(usuario, rol));
    }
 
    private String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
 
    private String hashear(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] resumen = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(resumen);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash del token.", e);
        }
    }
}