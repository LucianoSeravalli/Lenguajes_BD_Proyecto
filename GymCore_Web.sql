--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 01 - Seguridad base
-- Ejecutar como SYSTEM en la conexión habitual de Oracle Database XE.
--------------------------------------------------------------------------------

CREATE ROLE rol_desarrollador;
CREATE ROLE rol_usuario_final;
CREATE ROLE rol_backend_app;

CREATE USER gymcore_owner IDENTIFIED BY "Owner_2026*"
  DEFAULT TABLESPACE users QUOTA UNLIMITED ON users;

GRANT CREATE SESSION, CREATE TABLE, CREATE VIEW,
      CREATE PROCEDURE, CREATE TRIGGER
TO gymcore_owner;

CREATE USER gymcore_dev IDENTIFIED BY "Dev_2026*"
  DEFAULT TABLESPACE users QUOTA 100M ON users;

GRANT CREATE SESSION, CREATE PROCEDURE, CREATE TRIGGER, CREATE TABLE
TO gymcore_dev;
GRANT rol_desarrollador TO gymcore_dev;

CREATE USER gymcore_app IDENTIFIED BY "App_2026*"
  DEFAULT TABLESPACE users QUOTA 0 ON users;

GRANT CREATE SESSION TO gymcore_app;
GRANT rol_usuario_final TO gymcore_app;
GRANT rol_backend_app TO gymcore_app;

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 02 - Modelo fisico
-- Ejecutar como GYMCORE_OWNER.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- Seguridad de la aplicacion web.
-- PASSWORD_HASH y TOKEN_CONFIRMACION_HASH reciben hashes generados por el
-- backend. Nunca se debe almacenar ni comparar la contrasena en texto plano.
--------------------------------------------------------------------------------
CREATE TABLE app_rol (
  id_rol      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_rol  VARCHAR2(30) NOT NULL,
  descripcion VARCHAR2(200),
  estado      CHAR(1) DEFAULT 'A' NOT NULL,
  CONSTRAINT uq_app_rol_nombre UNIQUE (nombre_rol),
  CONSTRAINT ck_app_rol_estado CHECK (estado IN ('A','I'))
);

CREATE TABLE usuario (
  id_usuario              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre                  VARCHAR2(50) NOT NULL,
  apellido                VARCHAR2(50) NOT NULL,
  telefono                VARCHAR2(20),
  correo                  VARCHAR2(150) NOT NULL,
  password_hash           VARCHAR2(255) NOT NULL,
  token_confirmacion_hash VARCHAR2(255),
  token_expiracion        TIMESTAMP,
  correo_verificado       CHAR(1) DEFAULT 'N' NOT NULL,
  estado                  VARCHAR2(10) DEFAULT 'activo' NOT NULL,
  especialidad            VARCHAR2(50),
  fecha_contratacion      DATE,
  fecha_creacion          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  fecha_actualizacion     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT ck_usuario_verificado CHECK (correo_verificado IN ('S','N')),
  CONSTRAINT ck_usuario_estado CHECK (estado IN ('activo','inactivo','bloqueado')),
  CONSTRAINT ck_usuario_token CHECK (
    (token_confirmacion_hash IS NULL AND token_expiracion IS NULL) OR
    (token_confirmacion_hash IS NOT NULL AND token_expiracion IS NOT NULL)
  )
);

-- Evita correos duplicados aunque cambien mayusculas o minusculas.
CREATE UNIQUE INDEX uq_usuario_correo_lower ON usuario(LOWER(correo));
CREATE UNIQUE INDEX uq_usuario_token_hash ON usuario(token_confirmacion_hash);
CREATE UNIQUE INDEX uq_app_rol_nombre_lower ON app_rol(UPPER(nombre_rol));

CREATE TABLE usuario_rol (
  id_usuario      NUMBER NOT NULL,
  id_rol          NUMBER NOT NULL,
  fecha_asignacion TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  CONSTRAINT pk_usuario_rol PRIMARY KEY (id_usuario,id_rol),
  CONSTRAINT fk_ur_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT fk_ur_rol FOREIGN KEY (id_rol) REFERENCES app_rol(id_rol)
);

CREATE INDEX ix_usuario_rol_rol ON usuario_rol(id_rol,id_usuario);

-- CLIENTE conserva su identificador para no romper membresias, reservas,
-- accesos, rutinas ni los procesos PL/SQL existentes.
CREATE TABLE cliente (
  id_cliente       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_usuario       NUMBER NOT NULL,
  cedula           VARCHAR2(20) NOT NULL,
  fecha_nacimiento DATE,
  fecha_registro   DATE DEFAULT SYSDATE NOT NULL,
  estado           VARCHAR2(10) DEFAULT 'activo' NOT NULL,
  CONSTRAINT uq_cliente_usuario UNIQUE (id_usuario),
  CONSTRAINT uq_cliente_cedula UNIQUE (cedula),
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario(id_usuario),
  CONSTRAINT ck_cliente_estado CHECK (estado IN ('activo','inactivo'))
);

CREATE TABLE tipo_membresia (
  id_tipo_membresia NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre            VARCHAR2(30) NOT NULL,
  duracion_meses    NUMBER(3) NOT NULL,
  precio            NUMBER(10,2) NOT NULL,
  beneficios        VARCHAR2(300),
  CONSTRAINT uq_tipo_membresia_nombre UNIQUE (nombre),
  CONSTRAINT ck_tipo_duracion CHECK (duracion_meses > 0),
  CONSTRAINT ck_tipo_precio CHECK (precio >= 0)
);

CREATE TABLE membresia (
  id_membresia      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_cliente        NUMBER NOT NULL,
  id_tipo_membresia NUMBER NOT NULL,
  fecha_inicio      DATE NOT NULL,
  fecha_fin         DATE NOT NULL,
  estado            VARCHAR2(10) DEFAULT 'activa' NOT NULL,
  CONSTRAINT fk_membresia_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente),
  CONSTRAINT fk_membresia_tipo FOREIGN KEY (id_tipo_membresia)
    REFERENCES tipo_membresia(id_tipo_membresia),
  CONSTRAINT ck_membresia_estado CHECK (estado IN ('activa','vencida','cancelada')),
  CONSTRAINT ck_membresia_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE TABLE pago (
  id_pago      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_membresia NUMBER NOT NULL,
  monto        NUMBER(10,2) NOT NULL,
  fecha_pago   DATE DEFAULT SYSDATE NOT NULL,
  metodo_pago  VARCHAR2(20) NOT NULL,
  estado       VARCHAR2(15) DEFAULT 'completado' NOT NULL,
  CONSTRAINT fk_pago_membresia FOREIGN KEY (id_membresia)
    REFERENCES membresia(id_membresia),
  CONSTRAINT ck_pago_monto CHECK (monto > 0),
  CONSTRAINT ck_pago_metodo CHECK
    (metodo_pago IN ('efectivo','tarjeta','transferencia','sinpe')),
  CONSTRAINT ck_pago_estado CHECK (estado IN ('completado','pendiente'))
);

CREATE TABLE ejercicio (
  id_ejercicio   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre         VARCHAR2(80) NOT NULL,
  descripcion    VARCHAR2(300),
  grupo_muscular VARCHAR2(40)
);

CREATE TABLE rutina (
  id_rutina     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre        VARCHAR2(60) NOT NULL,
  nivel         VARCHAR2(15) NOT NULL,
  id_entrenador NUMBER NOT NULL,
  CONSTRAINT ck_rutina_nivel CHECK
    (nivel IN ('principiante','intermedio','avanzado')),
  CONSTRAINT fk_rutina_entrenador FOREIGN KEY (id_entrenador)
    REFERENCES usuario(id_usuario)
);

CREATE TABLE rutina_ejercicio (
  id_rutina         NUMBER NOT NULL,
  id_ejercicio      NUMBER NOT NULL,
  series            NUMBER(3) NOT NULL,
  repeticiones      NUMBER(3) NOT NULL,
  descanso_segundos NUMBER(5) DEFAULT 60 NOT NULL,
  CONSTRAINT pk_rutina_ejercicio PRIMARY KEY (id_rutina,id_ejercicio),
  CONSTRAINT fk_re_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina),
  CONSTRAINT fk_re_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio),
  CONSTRAINT ck_re_series CHECK (series > 0),
  CONSTRAINT ck_re_repeticiones CHECK (repeticiones > 0),
  CONSTRAINT ck_re_descanso CHECK (descanso_segundos >= 0)
);

CREATE TABLE rutina_cliente (
  id_rutina_cliente NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_rutina         NUMBER NOT NULL,
  id_cliente        NUMBER NOT NULL,
  fecha_asignacion  DATE DEFAULT SYSDATE NOT NULL,
  fecha_fin         DATE,
  CONSTRAINT fk_rc_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina),
  CONSTRAINT fk_rc_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  CONSTRAINT ck_rc_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_asignacion)
);

CREATE TABLE clase (
  id_clase      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre        VARCHAR2(60) NOT NULL,
  descripcion   VARCHAR2(300),
  id_entrenador NUMBER NOT NULL,
  cupo_maximo   NUMBER(4) NOT NULL,
  dia_semana    VARCHAR2(10) NOT NULL,
  hora_inicio   VARCHAR2(5) NOT NULL,
  hora_fin      VARCHAR2(5) NOT NULL,
  CONSTRAINT fk_clase_entrenador FOREIGN KEY (id_entrenador)
    REFERENCES usuario(id_usuario),
  CONSTRAINT ck_clase_cupo CHECK (cupo_maximo > 0),
  CONSTRAINT ck_clase_dia CHECK
    (dia_semana IN ('lunes','martes','miercoles','jueves','viernes','sabado','domingo')),
  CONSTRAINT ck_clase_hora_inicio CHECK
    (REGEXP_LIKE(hora_inicio,'^([01][0-9]|2[0-3]):[0-5][0-9]$')),
  CONSTRAINT ck_clase_hora_fin CHECK
    (REGEXP_LIKE(hora_fin,'^([01][0-9]|2[0-3]):[0-5][0-9]$')),
  CONSTRAINT ck_clase_rango_hora CHECK (hora_fin > hora_inicio)
);

CREATE TABLE reserva (
  id_reserva    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_cliente    NUMBER NOT NULL,
  id_clase      NUMBER NOT NULL,
  fecha_reserva DATE DEFAULT SYSDATE NOT NULL,
  estado        VARCHAR2(12) DEFAULT 'confirmada' NOT NULL,
  CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  CONSTRAINT fk_reserva_clase FOREIGN KEY (id_clase) REFERENCES clase(id_clase),
  CONSTRAINT ck_reserva_estado CHECK (estado IN ('confirmada','cancelada','asistio'))
);

CREATE TABLE acceso (
  id_acceso  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  id_cliente NUMBER NOT NULL,
  fecha_hora DATE DEFAULT SYSDATE NOT NULL,
  tipo       VARCHAR2(10) NOT NULL,
  resultado  VARCHAR2(10) NOT NULL,
  CONSTRAINT fk_acceso_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  CONSTRAINT ck_acceso_tipo CHECK (tipo IN ('entrada','salida')),
  CONSTRAINT ck_acceso_resultado CHECK (resultado IN ('permitido','denegado'))
);

CREATE INDEX ix_membresia_cliente ON membresia(id_cliente,estado,fecha_fin);
CREATE INDEX ix_pago_membresia ON pago(id_membresia,estado);
CREATE INDEX ix_acceso_cliente ON acceso(id_cliente,fecha_hora);
CREATE INDEX ix_rutina_entrenador ON rutina(id_entrenador);
CREATE INDEX ix_clase_entrenador ON clase(id_entrenador);
CREATE INDEX ix_reserva_clase ON reserva(id_clase,estado);
CREATE INDEX ix_reserva_cliente ON reserva(id_cliente,id_clase,estado);

-- Solo puede existir una reserva confirmada por cliente y clase, incluso si
-- otro proceso intenta insertar directamente sin utilizar el procedimiento.
CREATE UNIQUE INDEX uq_reserva_confirmada
ON reserva(
  CASE WHEN estado='confirmada' THEN id_cliente END,
  CASE WHEN estado='confirmada' THEN id_clase END
);

PROMPT Modelo fisico creado.

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 03 - Cuerpo transaccional
-- Ejecutar como GYMCORE_OWNER.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- FN_USUARIO_TIENE_ROL
-- Permite al backend y a las reglas de negocio comprobar roles de aplicacion.
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_usuario_tiene_rol(
  p_id_usuario IN usuario.id_usuario%TYPE,
  p_nombre_rol IN app_rol.nombre_rol%TYPE
) RETURN VARCHAR2
IS
  v_cantidad NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_cantidad
    FROM usuario u
    JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario
    JOIN app_rol ar ON ar.id_rol = ur.id_rol
   WHERE u.id_usuario = p_id_usuario
     AND u.estado = 'activo'
     AND ar.estado = 'A'
     AND UPPER(ar.nombre_rol) = UPPER(TRIM(p_nombre_rol));

  RETURN CASE WHEN v_cantidad > 0 THEN 'S' ELSE 'N' END;
END fn_usuario_tiene_rol;
/

--------------------------------------------------------------------------------
-- FN_MEMBRESIA_ACTIVA
-- Entrada: identificador del cliente.
-- Retorno: 'S' si existe una membresia vigente, con al menos un pago
--          completado y sin pagos pendientes; de lo contrario retorna 'N'.
-- No modifica datos ni confirma transacciones.
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_membresia_activa(
  p_id_cliente IN cliente.id_cliente%TYPE
) RETURN VARCHAR2
IS
  v_cantidad NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_cantidad
    FROM membresia m
   WHERE m.id_cliente = p_id_cliente
     AND m.estado <> 'cancelada'
     AND m.fecha_inicio <= TRUNC(SYSDATE)
     AND m.fecha_fin >= TRUNC(SYSDATE)
     AND EXISTS (
           SELECT 1 FROM pago p
            WHERE p.id_membresia = m.id_membresia
              AND p.estado = 'completado'
         )
     AND NOT EXISTS (
           SELECT 1 FROM pago p
            WHERE p.id_membresia = m.id_membresia
              AND p.estado = 'pendiente'
         );

  RETURN CASE WHEN v_cantidad > 0 THEN 'S' ELSE 'N' END;
END fn_membresia_activa;
/

--------------------------------------------------------------------------------
-- SP_REGISTRAR_PAGO
-- Entradas: membresia, monto, metodo de pago y estado.
-- Salida: no tiene parametro OUT. Si finaliza sin error, el pago queda
--         registrado y confirmado.
-- Error -20007: la membresia indicada no existe.
-- Otros errores: restricciones de monto, metodo, estado o integridad.
-- Efecto adicional: trg_pago_sync_membresia sincroniza la membresia.
--------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_registrar_pago(
  p_id_membresia IN pago.id_membresia%TYPE,
  p_monto        IN pago.monto%TYPE,
  p_metodo_pago  IN pago.metodo_pago%TYPE,
  p_estado       IN pago.estado%TYPE DEFAULT 'completado'
)
IS
  v_existe NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_existe
    FROM membresia WHERE id_membresia = p_id_membresia;
  IF v_existe = 0 THEN
    RAISE_APPLICATION_ERROR(-20007,'La membresia no existe.');
  END IF;

  INSERT INTO pago(id_membresia,monto,fecha_pago,metodo_pago,estado)
  VALUES(p_id_membresia,p_monto,SYSDATE,p_metodo_pago,p_estado);
  COMMIT;
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END sp_registrar_pago;
/

--------------------------------------------------------------------------------
-- FN_CUPO_DISPONIBLE
-- Entrada: identificador de la clase.
-- Retorno: numero de cupos restantes (cupo maximo menos reservas confirmadas).
-- Error -20008: la clase no existe.
-- No modifica datos ni confirma transacciones.
--------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_cupo_disponible(
  p_id_clase IN clase.id_clase%TYPE
) RETURN NUMBER
IS
  v_cupo NUMBER;
BEGIN
  SELECT c.cupo_maximo -
         (SELECT COUNT(*) FROM reserva r
           WHERE r.id_clase = c.id_clase AND r.estado = 'confirmada')
    INTO v_cupo
    FROM clase c
   WHERE c.id_clase = p_id_clase;
  RETURN v_cupo;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20008,'La clase no existe.');
END fn_cupo_disponible;
/

--------------------------------------------------------------------------------
-- SP_RESERVAR_CLASE
-- Entradas: cedula del cliente e identificador de la clase.
-- Salida p_resultado: mensaje de confirmacion cuando la reserva se registra.
-- Error -20002: membresia inactiva, vencida, sin pago o con pago pendiente.
-- Error -20004: la clase alcanzo su cupo maximo.
-- Error -20005: el cliente ya tiene una reserva confirmada.
-- Error -20009: el cliente activo o la clase no existen.
-- El bloqueo SELECT FOR UPDATE evita sobre-reservas concurrentes.
--------------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_reservar_clase(
  p_cedula  IN cliente.cedula%TYPE,
  p_id_clase IN clase.id_clase%TYPE,
  p_resultado OUT VARCHAR2
)
IS
  v_id_cliente cliente.id_cliente%TYPE;
  v_cupo_maximo clase.cupo_maximo%TYPE;
  v_reservadas NUMBER;
  v_duplicada NUMBER;
BEGIN
  SELECT id_cliente INTO v_id_cliente
    FROM cliente WHERE cedula = p_cedula AND estado = 'activo';

  IF fn_membresia_activa(v_id_cliente) = 'N' THEN
    RAISE_APPLICATION_ERROR(-20002,'El cliente no tiene membresia activa y pagada.');
  END IF;

  -- Serializa las reservas de la misma clase y evita sobre-reservas.
  SELECT cupo_maximo INTO v_cupo_maximo
    FROM clase WHERE id_clase = p_id_clase FOR UPDATE;

  SELECT COUNT(*) INTO v_duplicada
    FROM reserva
   WHERE id_cliente = v_id_cliente
     AND id_clase = p_id_clase
     AND estado = 'confirmada';
  IF v_duplicada > 0 THEN
    RAISE_APPLICATION_ERROR(-20005,'El cliente ya tiene una reserva confirmada.');
  END IF;

  SELECT COUNT(*) INTO v_reservadas
    FROM reserva WHERE id_clase = p_id_clase AND estado = 'confirmada';
  IF v_reservadas >= v_cupo_maximo THEN
    RAISE_APPLICATION_ERROR(-20004,'Cupo maximo alcanzado para esta clase.');
  END IF;

  INSERT INTO reserva(id_cliente,id_clase,fecha_reserva,estado)
  VALUES(v_id_cliente,p_id_clase,SYSDATE,'confirmada');
  p_resultado := 'Reserva registrada correctamente.';
  COMMIT;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    ROLLBACK;
    RAISE_APPLICATION_ERROR(-20009,'Cliente activo o clase no encontrados.');
  WHEN OTHERS THEN
    ROLLBACK;
    RAISE;
END sp_reservar_clase;
/

--------------------------------------------------------------------------------
-- TRG_PAGO_SYNC_MEMBRESIA
-- Se ejecuta despues de INSERT o UPDATE del estado de PAGO.
-- Los triggers no retornan valores. Su efecto es actualizar MEMBRESIA.estado:
--   * 'vencida' si la fecha termino o existe un pago pendiente.
--   * 'activa' si esta vigente y no existen pagos pendientes.
--   * no modifica membresias canceladas.
-- Es compound trigger para consultar PAGO despues de la sentencia y evitar
-- ORA-04091 (tabla mutante).
--------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_pago_sync_membresia
FOR INSERT OR UPDATE OF estado ON pago
COMPOUND TRIGGER
  TYPE t_ids IS TABLE OF membresia.id_membresia%TYPE INDEX BY PLS_INTEGER;
  g_ids t_ids;
  g_total PLS_INTEGER := 0;

  AFTER EACH ROW IS
  BEGIN
    g_total := g_total + 1;
    g_ids(g_total) := :NEW.id_membresia;
  END AFTER EACH ROW;

  AFTER STATEMENT IS
    v_pendientes NUMBER;
    v_fecha_fin DATE;
    v_estado membresia.estado%TYPE;
  BEGIN
    IF g_total > 0 THEN
      FOR i IN 1..g_total LOOP
        SELECT fecha_fin,estado INTO v_fecha_fin,v_estado
          FROM membresia WHERE id_membresia = g_ids(i);
        IF v_estado <> 'cancelada' THEN
          SELECT COUNT(*) INTO v_pendientes FROM pago
           WHERE id_membresia = g_ids(i) AND estado = 'pendiente';
          IF v_fecha_fin < TRUNC(SYSDATE) OR v_pendientes > 0 THEN
            UPDATE membresia SET estado = 'vencida' WHERE id_membresia = g_ids(i);
          ELSE
            UPDATE membresia SET estado = 'activa' WHERE id_membresia = g_ids(i);
          END IF;
        END IF;
      END LOOP;
    END IF;
  END AFTER STATEMENT;
END trg_pago_sync_membresia;
/

--------------------------------------------------------------------------------
-- TRG_MEMBRESIA_FECHA_ESTADO
-- Se ejecuta antes de insertar una membresia o actualizar su fecha final.
-- Los triggers no retornan valores. Su efecto es asignar :NEW.estado =
-- 'vencida' cuando fecha_fin ya paso, salvo que la membresia este cancelada.
--------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_membresia_fecha_estado
BEFORE INSERT OR UPDATE OF fecha_fin ON membresia
FOR EACH ROW
BEGIN
  IF :NEW.estado <> 'cancelada' AND :NEW.fecha_fin < TRUNC(SYSDATE) THEN
    :NEW.estado := 'vencida';
  END IF;
END trg_membresia_fecha_estado;
/

--------------------------------------------------------------------------------
-- Mantiene la fecha de actualizacion del usuario y garantiza que las clases y
-- rutinas solo referencien usuarios activos con rol ENTRENADOR.
--------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_usuario_fecha_actualiza
BEFORE UPDATE ON usuario
FOR EACH ROW
BEGIN
  :NEW.fecha_actualizacion := SYSTIMESTAMP;
END trg_usuario_fecha_actualiza;
/

CREATE OR REPLACE TRIGGER trg_cliente_validar_rol
BEFORE INSERT OR UPDATE OF id_usuario ON cliente
FOR EACH ROW
BEGIN
  IF fn_usuario_tiene_rol(:NEW.id_usuario,'CLIENTE') = 'N' THEN
    RAISE_APPLICATION_ERROR(-20011,
      'El usuario asociado no tiene un rol CLIENTE activo.');
  END IF;
END trg_cliente_validar_rol;
/

CREATE OR REPLACE TRIGGER trg_rutina_validar_entrenador
BEFORE INSERT OR UPDATE OF id_entrenador ON rutina
FOR EACH ROW
BEGIN
  IF fn_usuario_tiene_rol(:NEW.id_entrenador,'ENTRENADOR') = 'N' THEN
    RAISE_APPLICATION_ERROR(-20010,
      'El usuario asignado a la rutina no es un entrenador activo.');
  END IF;
END trg_rutina_validar_entrenador;
/

CREATE OR REPLACE TRIGGER trg_clase_validar_entrenador
BEFORE INSERT OR UPDATE OF id_entrenador ON clase
FOR EACH ROW
BEGIN
  IF fn_usuario_tiene_rol(:NEW.id_entrenador,'ENTRENADOR') = 'N' THEN
    RAISE_APPLICATION_ERROR(-20010,
      'El usuario asignado a la clase no es un entrenador activo.');
  END IF;
END trg_clase_validar_entrenador;
/

-- Impide retirar un rol mientras el usuario conserve un perfil o asignaciones
-- que dependan de el. Primero deben reasignarse o desactivarse esos registros.
CREATE OR REPLACE TRIGGER trg_usuario_rol_proteger
BEFORE DELETE OR UPDATE OF id_usuario,id_rol ON usuario_rol
FOR EACH ROW
DECLARE
  v_nombre_rol app_rol.nombre_rol%TYPE;
  v_referencias NUMBER;
BEGIN
  SELECT nombre_rol INTO v_nombre_rol
    FROM app_rol WHERE id_rol = :OLD.id_rol;

  IF UPPER(v_nombre_rol) = 'CLIENTE' THEN
    SELECT COUNT(*) INTO v_referencias
      FROM cliente WHERE id_usuario = :OLD.id_usuario;
  ELSIF UPPER(v_nombre_rol) = 'ENTRENADOR' THEN
    SELECT (SELECT COUNT(*) FROM rutina WHERE id_entrenador = :OLD.id_usuario) +
           (SELECT COUNT(*) FROM clase WHERE id_entrenador = :OLD.id_usuario)
      INTO v_referencias
      FROM dual;
  ELSE
    v_referencias := 0;
  END IF;

  IF v_referencias > 0 THEN
    RAISE_APPLICATION_ERROR(-20012,
      'No se puede retirar el rol porque el usuario conserva referencias.');
  END IF;
END trg_usuario_rol_proteger;
/

--------------------------------------------------------------------------------
-- Vistas de lectura para el backend. No exponen hashes ni tokens.
--------------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_clientes AS
SELECT c.id_cliente,
       u.id_usuario,
       u.nombre,
       u.apellido,
       c.cedula,
       u.telefono,
       u.correo,
       c.fecha_nacimiento,
       c.fecha_registro,
       c.estado AS estado_cliente,
       u.correo_verificado
  FROM cliente c
  JOIN usuario u ON u.id_usuario = c.id_usuario;

CREATE OR REPLACE VIEW vw_entrenadores AS
SELECT u.id_usuario AS id_entrenador,
       u.nombre,
       u.apellido,
       u.telefono,
       u.correo,
       u.especialidad,
       u.fecha_contratacion,
       u.estado
  FROM usuario u
  JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario
  JOIN app_rol ar ON ar.id_rol = ur.id_rol
 WHERE ar.nombre_rol = 'ENTRENADOR'
   AND ar.estado = 'A';

CREATE OR REPLACE VIEW vw_clases_disponibles AS
SELECT c.id_clase,
       c.nombre,
       c.descripcion,
       c.dia_semana,
       c.hora_inicio,
       c.hora_fin,
       c.cupo_maximo,
       fn_cupo_disponible(c.id_clase) AS cupos_disponibles,
       u.id_usuario AS id_entrenador,
       u.nombre || ' ' || u.apellido AS entrenador
  FROM clase c
  JOIN usuario u ON u.id_usuario = c.id_entrenador;

SHOW ERRORS FUNCTION fn_usuario_tiene_rol;
SHOW ERRORS FUNCTION fn_membresia_activa;
SHOW ERRORS PROCEDURE sp_registrar_pago;
SHOW ERRORS FUNCTION fn_cupo_disponible;
SHOW ERRORS PROCEDURE sp_reservar_clase;
SHOW ERRORS TRIGGER trg_pago_sync_membresia;
SHOW ERRORS TRIGGER trg_membresia_fecha_estado;
SHOW ERRORS TRIGGER trg_usuario_fecha_actualiza;
SHOW ERRORS TRIGGER trg_cliente_validar_rol;
SHOW ERRORS TRIGGER trg_rutina_validar_entrenador;
SHOW ERRORS TRIGGER trg_clase_validar_entrenador;
SHOW ERRORS TRIGGER trg_usuario_rol_proteger;

PROMPT Cuerpo transaccional creado.

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 04 - Privilegios sobre objetos
-- Ejecutar como GYMCORE_OWNER despues de los scripts 02 y 03.
--------------------------------------------------------------------------------

GRANT SELECT, INSERT, UPDATE, DELETE ON app_rol TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON usuario TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON usuario_rol TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON cliente TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON tipo_membresia TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON membresia TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON pago TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON ejercicio TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON rutina TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON rutina_ejercicio TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON rutina_cliente TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON clase TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON reserva TO rol_desarrollador;
GRANT SELECT, INSERT, UPDATE, DELETE ON acceso TO rol_desarrollador;
GRANT SELECT ON vw_clientes TO rol_desarrollador;
GRANT SELECT ON vw_entrenadores TO rol_desarrollador;
GRANT SELECT ON vw_clases_disponibles TO rol_desarrollador;

GRANT EXECUTE ON fn_usuario_tiene_rol TO rol_usuario_final;
GRANT EXECUTE ON fn_membresia_activa TO rol_usuario_final;
GRANT EXECUTE ON sp_registrar_pago TO rol_usuario_final;
GRANT EXECUTE ON fn_cupo_disponible TO rol_usuario_final;
GRANT EXECUTE ON sp_reservar_clase TO rol_usuario_final;
GRANT SELECT ON vw_clientes TO rol_usuario_final;
GRANT SELECT ON vw_entrenadores TO rol_usuario_final;
GRANT SELECT ON vw_clases_disponibles TO rol_usuario_final;

--------------------------------------------------------------------------------
-- GYMCORE_APP es una cuenta exclusiva para el servidor/backend. El navegador
-- nunca debe conectarse directamente a Oracle ni recibir estas credenciales.
--------------------------------------------------------------------------------
GRANT SELECT, INSERT, UPDATE ON usuario TO rol_backend_app;
GRANT SELECT ON app_rol TO rol_backend_app;
GRANT SELECT, INSERT, DELETE ON usuario_rol TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE ON cliente TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON tipo_membresia TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE ON membresia TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE ON pago TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ejercicio TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON rutina TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON rutina_ejercicio TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE ON rutina_cliente TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON clase TO rol_backend_app;
GRANT SELECT, INSERT, UPDATE ON reserva TO rol_backend_app;
GRANT SELECT, INSERT ON acceso TO rol_backend_app;
GRANT SELECT ON vw_clientes TO rol_backend_app;
GRANT SELECT ON vw_entrenadores TO rol_backend_app;
GRANT SELECT ON vw_clases_disponibles TO rol_backend_app;
GRANT EXECUTE ON fn_usuario_tiene_rol TO rol_backend_app;
GRANT EXECUTE ON fn_membresia_activa TO rol_backend_app;
GRANT EXECUTE ON fn_cupo_disponible TO rol_backend_app;
GRANT EXECUTE ON sp_registrar_pago TO rol_backend_app;
GRANT EXECUTE ON sp_reservar_clase TO rol_backend_app;

PROMPT Privilegios de desarrollo, API y backend aplicados.

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 05 - Datos de prueba
-- Ejecutar como GYMCORE_OWNER.
--------------------------------------------------------------------------------

DELETE FROM reserva WHERE id_cliente IN
  (SELECT id_cliente FROM cliente WHERE cedula IN
    ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444'));
DELETE FROM acceso WHERE id_cliente IN
  (SELECT id_cliente FROM cliente WHERE cedula IN
    ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444'));
DELETE FROM rutina_cliente WHERE id_cliente IN
  (SELECT id_cliente FROM cliente WHERE cedula IN
    ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444'));
DELETE FROM pago WHERE id_membresia IN
  (SELECT id_membresia FROM membresia WHERE id_cliente IN
    (SELECT id_cliente FROM cliente WHERE cedula IN
      ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444')));
DELETE FROM membresia WHERE id_cliente IN
  (SELECT id_cliente FROM cliente WHERE cedula IN
    ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444'));
DELETE FROM clase WHERE nombre IN ('Spinning','Concurrencia');
DELETE FROM cliente WHERE cedula IN
  ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444');
DELETE FROM usuario_rol WHERE id_usuario IN
  (SELECT id_usuario FROM usuario WHERE correo IN
    ('ana.rojas@test.com','luis.vargas@test.com','carla.mora@test.com',
     'mario.soto@test.com','paola.jimenez@test.com'));
DELETE FROM usuario WHERE correo IN
  ('ana.rojas@test.com','luis.vargas@test.com','carla.mora@test.com',
   'mario.soto@test.com','paola.jimenez@test.com');
DELETE FROM tipo_membresia WHERE nombre IN ('Mensual','Trimestral');
COMMIT;

MERGE INTO app_rol r
USING (
  SELECT 'CLIENTE' nombre_rol, 'Cliente del gimnasio' descripcion FROM dual
  UNION ALL SELECT 'ENTRENADOR', 'Entrenador del gimnasio' FROM dual
  UNION ALL SELECT 'ADMINISTRADOR', 'Administrador de la aplicacion' FROM dual
) d
ON (r.nombre_rol = d.nombre_rol)
WHEN NOT MATCHED THEN
  INSERT (nombre_rol,descripcion) VALUES (d.nombre_rol,d.descripcion);

INSERT INTO tipo_membresia(nombre,duracion_meses,precio,beneficios)
VALUES('Mensual',1,25000,'Sala de pesas y cardio');
INSERT INTO tipo_membresia(nombre,duracion_meses,precio,beneficios)
VALUES('Trimestral',3,65000,'Acceso completo y clases grupales');

--------------------------------------------------------------------------------
-- Hash centinela para cuentas de prueba: no permite iniciar sesion. El backend
-- debe sustituirlo por un hash Argon2id o bcrypt real antes de habilitarlas.
--------------------------------------------------------------------------------
INSERT INTO usuario(nombre,apellido,telefono,correo,password_hash,correo_verificado)
VALUES('Ana','Rojas','8888-1111','ana.rojas@test.com','DEMO_SIN_ACCESO_REEMPLAZAR_HASH','S');
INSERT INTO usuario(nombre,apellido,telefono,correo,password_hash,correo_verificado)
VALUES('Luis','Vargas','8888-2222','luis.vargas@test.com','DEMO_SIN_ACCESO_REEMPLAZAR_HASH','S');
INSERT INTO usuario(nombre,apellido,telefono,correo,password_hash,correo_verificado)
VALUES('Carla','Mora','8888-3333','carla.mora@test.com','DEMO_SIN_ACCESO_REEMPLAZAR_HASH','S');
INSERT INTO usuario(nombre,apellido,telefono,correo,password_hash,correo_verificado)
VALUES('Mario','Soto','8888-4444','mario.soto@test.com','DEMO_SIN_ACCESO_REEMPLAZAR_HASH','S');
INSERT INTO usuario(
  nombre,apellido,telefono,correo,password_hash,correo_verificado,
  especialidad,fecha_contratacion
)
VALUES(
  'Paola','Jimenez','8777-2222','paola.jimenez@test.com',
  'DEMO_SIN_ACCESO_REEMPLAZAR_HASH','S','Spinning',SYSDATE
);

INSERT INTO usuario_rol(id_usuario,id_rol)
SELECT u.id_usuario,r.id_rol
  FROM usuario u CROSS JOIN app_rol r
 WHERE u.correo IN ('ana.rojas@test.com','luis.vargas@test.com',
                    'carla.mora@test.com','mario.soto@test.com')
   AND r.nombre_rol='CLIENTE';

INSERT INTO usuario_rol(id_usuario,id_rol)
SELECT u.id_usuario,r.id_rol
  FROM usuario u CROSS JOIN app_rol r
 WHERE u.correo='paola.jimenez@test.com'
   AND r.nombre_rol='ENTRENADOR';

INSERT INTO cliente(id_usuario,cedula,fecha_nacimiento)
SELECT id_usuario,'1-1111-1111',DATE '1995-03-14'
  FROM usuario WHERE correo='ana.rojas@test.com';
INSERT INTO cliente(id_usuario,cedula,fecha_nacimiento)
SELECT id_usuario,'2-2222-2222',DATE '1990-07-22'
  FROM usuario WHERE correo='luis.vargas@test.com';
INSERT INTO cliente(id_usuario,cedula,fecha_nacimiento)
SELECT id_usuario,'3-3333-3333',DATE '1998-11-02'
  FROM usuario WHERE correo='carla.mora@test.com';
INSERT INTO cliente(id_usuario,cedula,fecha_nacimiento)
SELECT id_usuario,'4-4444-4444',DATE '1993-08-17'
  FROM usuario WHERE correo='mario.soto@test.com';

INSERT INTO membresia(id_cliente,id_tipo_membresia,fecha_inicio,fecha_fin,estado)
SELECT c.id_cliente,t.id_tipo_membresia,TRUNC(SYSDATE)-5,TRUNC(SYSDATE)+25,'activa'
  FROM cliente c CROSS JOIN tipo_membresia t
 WHERE c.cedula='1-1111-1111' AND t.nombre='Mensual';

INSERT INTO membresia(id_cliente,id_tipo_membresia,fecha_inicio,fecha_fin,estado)
SELECT c.id_cliente,t.id_tipo_membresia,TRUNC(SYSDATE)-10,TRUNC(SYSDATE)+80,'activa'
  FROM cliente c CROSS JOIN tipo_membresia t
 WHERE c.cedula='2-2222-2222' AND t.nombre='Trimestral';

INSERT INTO membresia(id_cliente,id_tipo_membresia,fecha_inicio,fecha_fin,estado)
SELECT c.id_cliente,t.id_tipo_membresia,TRUNC(SYSDATE)-60,TRUNC(SYSDATE)-30,'activa'
  FROM cliente c CROSS JOIN tipo_membresia t
 WHERE c.cedula='3-3333-3333' AND t.nombre='Mensual';

INSERT INTO membresia(id_cliente,id_tipo_membresia,fecha_inicio,fecha_fin,estado)
SELECT c.id_cliente,t.id_tipo_membresia,TRUNC(SYSDATE)-2,TRUNC(SYSDATE)+28,'activa'
  FROM cliente c CROSS JOIN tipo_membresia t
 WHERE c.cedula='4-4444-4444' AND t.nombre='Mensual';

INSERT INTO pago(id_membresia,monto,metodo_pago,estado)
SELECT m.id_membresia,25000,'tarjeta','completado' FROM membresia m JOIN cliente c
ON c.id_cliente=m.id_cliente WHERE c.cedula='1-1111-1111';
INSERT INTO pago(id_membresia,monto,metodo_pago,estado)
SELECT m.id_membresia,65000,'sinpe','pendiente' FROM membresia m JOIN cliente c
ON c.id_cliente=m.id_cliente WHERE c.cedula='2-2222-2222';
INSERT INTO pago(id_membresia,monto,metodo_pago,estado)
SELECT m.id_membresia,25000,'efectivo','completado' FROM membresia m JOIN cliente c
ON c.id_cliente=m.id_cliente WHERE c.cedula='3-3333-3333';
INSERT INTO pago(id_membresia,monto,metodo_pago,estado)
SELECT m.id_membresia,25000,'transferencia','completado' FROM membresia m JOIN cliente c
ON c.id_cliente=m.id_cliente WHERE c.cedula='4-4444-4444';

INSERT INTO clase(nombre,descripcion,id_entrenador,cupo_maximo,dia_semana,hora_inicio,hora_fin)
SELECT 'Spinning','Clase de ciclismo indoor',id_usuario,2,'lunes','18:00','19:00'
FROM usuario WHERE correo='paola.jimenez@test.com';
INSERT INTO clase(nombre,descripcion,id_entrenador,cupo_maximo,dia_semana,hora_inicio,hora_fin)
SELECT 'Concurrencia','Clase para prueba simultanea',id_usuario,1,'martes','18:00','19:00'
FROM usuario WHERE correo='paola.jimenez@test.com';
COMMIT;

SELECT c.cedula,u.nombre,m.estado AS membresia,p.estado AS pago,m.fecha_fin
FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario
JOIN membresia m ON m.id_cliente=c.id_cliente
JOIN pago p ON p.id_membresia=m.id_membresia
WHERE c.cedula IN ('1-1111-1111','2-2222-2222','3-3333-3333','4-4444-4444')
ORDER BY c.cedula;

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 06 - Pruebas funcionales
-- Ejecutar los bloques A, B y C como GYMCORE_OWNER.
--------------------------------------------------------------------------------
SET SERVEROUTPUT ON;

PROMPT A. FUNCION DE MEMBRESIA Y TRIGGER DE PAGOS
SELECT u.nombre,
       fn_membresia_activa(c.id_cliente) AS membresia_activa,
       m.estado AS estado_membresia,
       p.estado AS estado_pago
FROM cliente c
JOIN usuario u ON u.id_usuario=c.id_usuario
JOIN membresia m ON m.id_cliente=c.id_cliente
JOIN pago p ON p.id_membresia=m.id_membresia
WHERE c.cedula IN ('1-1111-1111','2-2222-2222','3-3333-3333')
ORDER BY c.cedula;
-- Esperado antes de completar el pago: Ana=S, Luis=N y Carla=N.

UPDATE pago SET estado='completado'
WHERE estado='pendiente' AND id_membresia IN
 (SELECT m.id_membresia FROM membresia m JOIN cliente c
    ON c.id_cliente=m.id_cliente WHERE c.cedula='2-2222-2222');
COMMIT;

SELECT u.nombre,
       fn_membresia_activa(c.id_cliente) AS membresia_activa,
       m.estado AS estado_membresia
FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario
JOIN membresia m ON m.id_cliente=c.id_cliente
WHERE c.cedula='2-2222-2222';
-- Esperado: membresia_activa='S' y estado_membresia='activa'.

PROMPT B. CUPO, SALIDA DEL PROCEDIMIENTO Y LIMITE DE RESERVAS
DECLARE
  v_clase NUMBER;
  v_resultado VARCHAR2(100);
BEGIN
  SELECT id_clase INTO v_clase FROM clase WHERE nombre='Spinning';
  sp_reservar_clase('1-1111-1111',v_clase,v_resultado);
  DBMS_OUTPUT.PUT_LINE(v_resultado);
  DBMS_OUTPUT.PUT_LINE('Cupo despues de Ana: '||fn_cupo_disponible(v_clase)||' (esperado 1)');
  sp_reservar_clase('4-4444-4444',v_clase,v_resultado);
  DBMS_OUTPUT.PUT_LINE(v_resultado);
  DBMS_OUTPUT.PUT_LINE('Cupo despues de Mario: '||fn_cupo_disponible(v_clase)||' (esperado 0)');
END;
/

DECLARE
  v_clase NUMBER;
  v_resultado VARCHAR2(100);
BEGIN
  SELECT id_clase INTO v_clase FROM clase WHERE nombre='Spinning';
  sp_reservar_clase('2-2222-2222',v_clase,v_resultado);
EXCEPTION WHEN OTHERS THEN
  DBMS_OUTPUT.PUT_LINE('Tercer cupo: '||SQLERRM||' (esperado ORA-20004)');
END;
/

PROMPT C. INTEGRIDAD Y ATOMICIDAD
DECLARE
  v_antes NUMBER;
  v_despues NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_antes FROM pago;
  BEGIN
    sp_registrar_pago(-999,1000,'efectivo','completado');
  EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error controlado: '||SQLERRM);
  END;
  SELECT COUNT(*) INTO v_despues FROM pago;
  DBMS_OUTPUT.PUT_LINE('Pagos antes='||v_antes||', despues='||v_despues||' (deben ser iguales)');
END;
/

--------------------------------------------------------------------------------
-- BLOQUE D: ejecutar en otra conexion como GYMCORE_APP.
-- La consulta directa debe fallar. Para probar la API, sustituir &ID_CLASE
-- por un identificador de clase obtenido previamente como GYMCORE_OWNER.
--------------------------------------------------------------------------------
-- SELECT * FROM gymcore_owner.cliente;
-- SET SERVEROUTPUT ON;
-- DEFINE ID_CLASE = 1
-- DECLARE v_resultado VARCHAR2(100);
-- BEGIN
--   gymcore_owner.sp_reservar_clase('1-1111-1111',&ID_CLASE,v_resultado);
--   DBMS_OUTPUT.PUT_LINE(v_resultado);
-- END;
-- /

--------------------------------------------------------------------------------
-- BLOQUE E: ejecutar en otra conexion como GYMCORE_DEV.
--------------------------------------------------------------------------------
-- SELECT COUNT(*) FROM gymcore_owner.cliente;
-- UPDATE gymcore_owner.cliente SET telefono='8888-0000'
-- WHERE cedula='1-1111-1111';
-- ROLLBACK;

--------------------------------------------------------------------------------
-- GymCore | Avance 2 | 07 - Prueba de aislamiento en dos sesiones
--------------------------------------------------------------------------------
-- Preparacion como GYMCORE_OWNER:
-- DELETE FROM reserva WHERE id_clase=(SELECT id_clase FROM clase WHERE nombre='Concurrencia');
-- COMMIT;
--
-- SESION A (ejecutar primero):
-- SELECT cupo_maximo FROM clase WHERE nombre='Concurrencia' FOR UPDATE;
-- Mantener esta sesion sin COMMIT durante unos segundos.
--
-- SESION B (ejecutar mientras A mantiene el bloqueo):
-- SET SERVEROUTPUT ON;
-- DECLARE v_clase NUMBER; v_resultado VARCHAR2(100);
-- BEGIN
--   SELECT id_clase INTO v_clase FROM clase WHERE nombre='Concurrencia';
--   gymcore_owner.sp_reservar_clase('1-1111-1111',v_clase,v_resultado);
--   DBMS_OUTPUT.PUT_LINE(v_resultado);
-- END;
-- /
-- La llamada queda esperando el bloqueo de CLASE.
--
-- SESION A:
-- COMMIT;
--
-- SESION B termina y reserva el unico cupo. Luego, desde SESION A:
-- SET SERVEROUTPUT ON;
-- DECLARE v_clase NUMBER; v_resultado VARCHAR2(100);
-- BEGIN
--   SELECT id_clase INTO v_clase FROM clase WHERE nombre='Concurrencia';
--   gymcore_owner.sp_reservar_clase('4-4444-4444',v_clase,v_resultado);
-- EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE(SQLERRM); END;
-- /
-- Resultado esperado: ORA-20004. Nunca quedan dos reservas confirmadas.
