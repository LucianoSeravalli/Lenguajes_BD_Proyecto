package lenguajesBD_Grupo06.Service;

import lenguajesBD_Grupo06.Domain.Cliente;
import exception.GymCoreException;
import lenguajesBD_Grupo06.Repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
public class ClienteService {
 
    private final ClienteRepository clienteRepository;
 
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
 
    @Transactional(readOnly = true)
    public Cliente buscarPorCedula(String cedula) {
        return clienteRepository.findByCedula(cedula)
                .orElseThrow(() -> new GymCoreException(0,
                        "No existe un cliente con la cedula " + cedula + "."));
    }
 
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new GymCoreException(0, "El cliente no existe."));
    }
 
    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByEstadoConUsuario("activo");
    }
 
    /**
     * Actualiza los datos personales. Nombre, telefono y correo viven en
     * USUARIO; cedula y fecha de nacimiento en CLIENTE.
     */
    @Transactional
    public Cliente actualizarDatos(Long idCliente, String nombre, String apellido,
                                   String telefono, java.time.LocalDate fechaNacimiento) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.getUsuario().setNombre(nombre);
        cliente.getUsuario().setApellido(apellido);
        cliente.getUsuario().setTelefono(telefono);
        cliente.setFechaNacimiento(fechaNacimiento);
        return clienteRepository.save(cliente);
    }
 
    /** Inactivar en vez de borrar: hay membresias, pagos y accesos que dependen. */
    @Transactional
    public void cambiarEstado(Long idCliente, String nuevoEstado) {
        Cliente cliente = buscarPorId(idCliente);
        cliente.setEstado(nuevoEstado);
        clienteRepository.save(cliente);
    }
}