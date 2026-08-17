package lenguajesBD_Grupo06.web;
import exception.GymCoreException;
import lenguajesBD_Grupo06.web.dto.Responses;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
 
import java.util.stream.Collectors;
 
/**
 * Traduce los errores del cuerpo transaccional PL/SQL a respuestas HTTP.
 * Sin esto, el frontend recibiria un 500 con el texto ORA- crudo.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {
 
    @ExceptionHandler(GymCoreException.class)
    public ResponseEntity<Responses.ErrorDto> gymCore(GymCoreException ex) {
        HttpStatus estado = mapear(ex.getCodigoOracle());
        return ResponseEntity.status(estado).body(new Responses.ErrorDto(
                estado.value(),
                estado.getReasonPhrase(),
                ex.getMessage(),
                ex.getCodigoOracle() != 0 ? ex.getCodigoOracle() : null));
    }
 
    /**
     * 20002 sin membresia activa, 20004 cupo lleno y 20005 reserva duplicada
     * son conflictos de estado, no errores de sintaxis de la peticion.
     */
    private HttpStatus mapear(int codigoOracle) {
        return switch (codigoOracle) {
            case 20002, 20004, 20005 -> HttpStatus.CONFLICT;
            case 20007, 20008, 20009 -> HttpStatus.NOT_FOUND;
            case 20010, 20011, 20012 -> HttpStatus.UNPROCESSABLE_ENTITY;
            case 0 -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
 
    /** Errores de @Valid en los records de Requests. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Responses.ErrorDto> validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
 
        return ResponseEntity.badRequest().body(new Responses.ErrorDto(
                400, "Bad Request", detalle, null));
    }
 
    /**
     * Restricciones que la base rechaza sin pasar por PL/SQL: UNIQUE de cedula
     * o correo, CHECK de estados y metodos de pago, y el indice parcial
     * UQ_RESERVA_CONFIRMADA si alguien inserta saltandose el procedimiento.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Responses.ErrorDto> integridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Responses.ErrorDto(
                409, "Conflict",
                "La operacion viola una restriccion de integridad de la base de datos.",
                null));
    }
}