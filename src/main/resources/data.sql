-- =====================================================================
-- USUARIOS (todos con password 'test')
-- =====================================================================
-- 1: admin del proyecto base
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'test@unlam.edu.ar', 'test', 'ADMIN', true, 'Admin', 'Test', null);
-- 2 y 3: entrenadores
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'entrenador@unlam.edu.ar', 'test', 'ENTRENADOR', true, 'Laura', 'Gomez', null);
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'entrenador2@unlam.edu.ar', 'test', 'ENTRENADOR', true, 'Martin', 'Diaz', null);
-- 4: socio "ideal": TENIS + membresia vigente (con una vencida en el historial)
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'socio@unlam.edu.ar', 'test', 'SOCIO', true, 'Juan', 'Perez', 'TENIS');
-- 5: socio con membresia VENCIDA -> no puede reservar
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'vencido@unlam.edu.ar', 'test', 'SOCIO', true, 'Ana', 'Lopez', 'TENIS');
-- 6: socio SIN DEPORTE -> no puede generar rutina (pero si reservar)
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'sindeporte@unlam.edu.ar', 'test', 'SOCIO', true, 'Pedro', 'Ruiz', null);
-- 7: socio SIN MEMBRESIA -> no puede reservar
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte) VALUES(null, 'nuevo@unlam.edu.ar', 'test', 'SOCIO', true, 'Sofia', 'Castro', 'TENIS');
-- 8: socio con membresia POR VENCER (le faltan 5 dias) -> activa la alerta en home
INSERT INTO Usuario(id, email, password, rol, activo, nombre, apellido, deporte)
VALUES(null, 'porvencer@unlam.edu.ar', 'test', 'SOCIO', true, 'Carlos', 'Benitez', 'FUTBOL');

-- =====================================================================
-- MEMBRESIAS
-- =====================================================================
-- Socio 4: una vencida (historial) + una vigente
INSERT INTO Membresia(id, socio_id, tipo, fechaInicio, fechaVencimiento) VALUES(null, 4, 'MENSUAL', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_SUB(CURDATE(), INTERVAL 1 MONTH));
INSERT INTO Membresia(id, socio_id, tipo, fechaInicio, fechaVencimiento) VALUES(null, 4, 'TRIMESTRAL', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 2 MONTH));
-- Socio 5: solo vencida
INSERT INTO Membresia(id, socio_id, tipo, fechaInicio, fechaVencimiento) VALUES(null, 5, 'MENSUAL', DATE_SUB(CURDATE(), INTERVAL 2 MONTH), DATE_SUB(CURDATE(), INTERVAL 1 MONTH));
-- Socio 6: vigente anual
INSERT INTO Membresia(id, socio_id, tipo, fechaInicio, fechaVencimiento) VALUES(null, 6, 'ANUAL', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 11 MONTH));
-- Socio 8: vigente pero le quedan exactamente 5 dias para vencer
INSERT INTO Membresia(id, socio_id, tipo, fechaInicio, fechaVencimiento)
VALUES(null, 8, 'MENSUAL', DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 5 DAY), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 5 DAY));
-- =====================================================================
-- CLASES
-- =====================================================================
-- 1: futura, con lugar
INSERT INTO Clase(id, nombre, inicio, duracion, lugar, cupo, capacidad, entrenador_id) VALUES(null, 'Funcional', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00'), 60, 'Sala 1', 10, 'FUERZA', 2);
-- 2: futura, con lugar
INSERT INTO Clase(id, nombre, inicio, duracion, lugar, cupo, capacidad, entrenador_id) VALUES(null, 'Spinning', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00'), 45, 'Sala 2', 15, 'CARDIO', 3);
-- 3: futura, CUPO 1 y ya reservada -> LLENA
INSERT INTO Clase(id, nombre, inicio, duracion, lugar, cupo, capacidad, entrenador_id) VALUES(null, 'Agilidad en cancha', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 60, 'Cancha', 1, 'AGILIDAD', 2);
-- 4: futura, sin reservas
INSERT INTO Clase(id, nombre, inicio, duracion, lugar, cupo, capacidad, entrenador_id) VALUES(null, 'Coordinacion y equilibrio', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '17:00:00'), 50, 'Sala 1', 8, 'COORDINACION', 3);
-- 5: PASADA -> no deberia aparecer en buscarDesde(ahora)
INSERT INTO Clase(id, nombre, inicio, duracion, lugar, cupo, capacidad, entrenador_id) VALUES(null, 'Funcional', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '18:00:00'), 60, 'Sala 1', 10, 'FUERZA', 2);

-- =====================================================================
-- RESERVAS
-- =====================================================================
-- Socio 4 ya reservo la clase 1 -> reservarla de nuevo = ReservaDuplicada
INSERT INTO Reserva(id, socio_id, clase_id, estado, fechaReserva) VALUES(null, 4, 1, 'CONFIRMADA', DATE_SUB(NOW(), INTERVAL 1 DAY));
-- Socio 4 cancelo la clase 2 -> puede volver a reservarla (la cancelada no cuenta)
INSERT INTO Reserva(id, socio_id, clase_id, estado, fechaReserva) VALUES(null, 4, 2, 'CANCELADA', DATE_SUB(NOW(), INTERVAL 1 DAY));
-- Socio 6 ocupa el unico lugar de la clase 3 -> cualquier otro = ClaseSinCupo
INSERT INTO Reserva(id, socio_id, clase_id, estado, fechaReserva) VALUES(null, 6, 3, 'CONFIRMADA', DATE_SUB(NOW(), INTERVAL 1 DAY));
-- Socio 6 en una clase pasada (historial)
INSERT INTO Reserva(id, socio_id, clase_id, estado, fechaReserva) VALUES(null, 6, 5, 'CONFIRMADA', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =====================================================================
-- EJERCICIOS (3 por capacidad)
-- =====================================================================
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Sentadilla con barra', 'Flexion de piernas con barra sobre la espalda', 'FUERZA', 'INTERMEDIO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Press de banca', 'Empuje de barra acostado en banco plano', 'FUERZA', 'INTERMEDIO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Peso muerto', 'Levantamiento de barra desde el piso', 'FUERZA', 'AVANZADO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Zigzag entre conos', 'Carrera cambiando de direccion entre conos', 'AGILIDAD', 'PRINCIPIANTE');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Escalera de agilidad', 'Pasos rapidos dentro de una escalera en el piso', 'AGILIDAD', 'PRINCIPIANTE');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Desplazamientos laterales', 'Desplazamiento lateral rapido tocando marcas', 'AGILIDAD', 'INTERMEDIO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Saltar la soga', 'Saltos continuos con soga', 'CARDIO', 'PRINCIPIANTE');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Burpees', 'Flexion, salto y vuelta a posicion de pie', 'CARDIO', 'INTERMEDIO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Trote continuo', 'Trote a ritmo constante durante 20 minutos', 'CARDIO', 'PRINCIPIANTE');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Pases contra la pared', 'Lanzar y recibir una pelota contra la pared', 'COORDINACION', 'PRINCIPIANTE');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Equilibrio en bosu', 'Mantener el equilibrio en una pierna sobre bosu', 'COORDINACION', 'INTERMEDIO');
INSERT INTO Ejercicio(id, nombre, descripcion, capacidad, dificultad) VALUES(null, 'Saltos alternados', 'Saltos con cambio de pierna coordinando brazos', 'COORDINACION', 'INTERMEDIO');