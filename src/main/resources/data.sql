
-- ============================================================================
-- 1. LIMPIAR TABLAS (El orden respeta las claves foráneas)
-- ============================================================================
DELETE FROM ItemRutina;
DELETE FROM Reserva;
DELETE FROM Clase;
DELETE FROM Membresia;
DELETE FROM Usuario;

-- Reiniciar autoincrementales
ALTER TABLE Usuario AUTO_INCREMENT = 1;
ALTER TABLE Membresia AUTO_INCREMENT = 1;
ALTER TABLE Clase AUTO_INCREMENT = 1;
ALTER TABLE Reserva AUTO_INCREMENT = 1;
ALTER TABLE ItemRutina AUTO_INCREMENT = 1;

-- ============================================================================
-- 2. INSERTAR USUARIOS (Clave general para pruebas: "123")
-- ============================================================================
-- Administrador
INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (1, 'admin@unlam.edu.ar', '123', 'ADMIN', 'Admin', 'Sistema', true, null);

-- Entrenadores
INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (2, 'profe1@unlam.edu.ar', '123', 'ENTRENADOR', 'Lucas', 'Martínez', true, null);

INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (3, 'profe2@unlam.edu.ar', '123', 'ENTRENADOR', 'Sofía', 'Gómez', true, null);

-- Socios
INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (4, 'socio@unlam.edu.ar', '123', 'SOCIO', 'Juan', 'Pérez', true, 'TENIS');

INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (5, 'maria@unlam.edu.ar', '123', 'SOCIO', 'María', 'López', true, 'FUTBOL');

INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (6, 'lucas@unlam.edu.ar', '123', 'SOCIO', 'Lucas', 'Rodríguez', true, 'BOXEO');

INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (7, 'camila@unlam.edu.ar', '123', 'SOCIO', 'Camila', 'Fernández', true, 'PADEL');

INSERT INTO Usuario (id, email, password, rol, nombre, apellido, activo, deporte)
VALUES (8, 'sinmembresia@unlam.edu.ar', '123', 'SOCIO', 'Martín', 'Díaz', true, 'BASQUET');

-- ============================================================================
-- 3. INSERTAR MEMBRESÍAS
-- ============================================================================
-- Juan (Socio 4): Membresía Anual activa
INSERT INTO Membresia (id, socio_id, tipo, fechaInicio, fechaVencimiento)
VALUES (1, 4, 'ANUAL', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 YEAR));

-- María (Socio 5): Membresía Trimestral activa
INSERT INTO Membresia (id, socio_id, tipo, fechaInicio, fechaVencimiento)
VALUES (2, 5, 'TRIMESTRAL', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 MONTH));

-- Lucas (Socio 6): Membresía Mensual activa por vencer (para probar avisos en home)
INSERT INTO Membresia (id, socio_id, tipo, fechaInicio, fechaVencimiento)
VALUES (3, 6, 'MENSUAL', DATE_SUB(CURDATE(), INTERVAL 27 DAY), DATE_ADD(CURDATE(), INTERVAL 3 DAY));

-- Camila (Socio 7): Membresía Anual activa
INSERT INTO Membresia (id, socio_id, tipo, fechaInicio, fechaVencimiento)
VALUES (4, 7, 'ANUAL', DATE_SUB(CURDATE(), INTERVAL 1 MONTH), DATE_ADD(CURDATE(), INTERVAL 11 MONTH));

-- Socio 8 no tiene membresía para probar la validación de rechazo de reserva

-- ============================================================================
-- 4. INSERTAR CLASES EN EL CALENDARIO (Próximos días sin solapamientos)
-- ============================================================================
-- DÍA 1 (Mañana): Turno Mañana y Tarde
INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (1, 2, 'Funcional Power', CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 09:00:00'), 60, 'Sala 1', 12, 'FUERZA');

INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (2, 3, 'Spinning Matutino', CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 10:30:00'), 45, 'Sala 2', 15, 'CARDIO');

INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (3, 2, 'Agilidad & Conos', CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 18:00:00'), 50, 'Cancha Polideportiva', 10, 'AGILIDAD');

-- DÍA 2: Incluye una clase con cupo 0 para probar la lista de espera
INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (4, 3, 'Yoga & Equilibrio', CONCAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), ' 09:00:00'), 60, 'Sala Zen', 8, 'COORDINACION');

INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (5, 2, 'Cross HIIT (Llena)', CONCAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY), ' 19:00:00'), 60, 'Sala 1', 0, 'FUERZA');

-- DÍA 3: Clases de fin de semana / tarde
INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (6, 3, 'Spinning Extremo', CONCAT(DATE_ADD(CURDATE(), INTERVAL 3 DAY), ' 17:30:00'), 45, 'Sala 2', 18, 'CARDIO');

INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (7, 2, 'Coordinación y Reflejos', CONCAT(DATE_ADD(CURDATE(), INTERVAL 3 DAY), ' 19:00:00'), 45, 'Cancha Polideportiva', 14, 'COORDINACION');

-- DÍA 4: Próxima semana
INSERT INTO Clase (id, entrenador_id, nombre, inicio, duracion, lugar, cupo, capacidad)
VALUES (8, 2, 'Fuerza Máxima', CONCAT(DATE_ADD(CURDATE(), INTERVAL 4 DAY), ' 10:00:00'), 60, 'Sala 1', 15, 'FUERZA');

-- ============================================================================
-- 5. INSERTAR RESERVAS (Confirmadas y en Espera)
-- ============================================================================
-- Reservas en Clase 1 (Funcional Power)
INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (1, 4, 1, 'CONFIRMADA', NOW());

INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (2, 5, 1, 'CONFIRMADA', NOW());

-- Reservas en Clase 2 (Spinning Matutino)
INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (3, 6, 2, 'CONFIRMADA', NOW());

-- Reservas en Clase 5 (Cross HIIT - Cupo 0 para verificar lista de espera)
INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (4, 4, 5, 'CONFIRMADA', DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (5, 5, 5, 'EN_ESPERA', DATE_SUB(NOW(), INTERVAL 1 HOUR));

INSERT INTO Reserva (id, socio_id, clase_id, estado, fechaReserva)
VALUES (6, 7, 5, 'EN_ESPERA', NOW());

-- ============================================================================
-- 6. INSERTAR RUTINAS SEMANALES (Para Juan - Socio 4 y María - Socio 5)
-- ============================================================================
-- Rutina de Juan (Socio 4 - Tenis)
-- Lunes
INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (1, 4, 2, 'LUNES', 'Press de pecho plano', 'barbell-bench-press', 4, 10, 25.0, 'Pies firmes en el suelo, control excéntrico', true, 25.0);

INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (2, 4, 2, 'LUNES', 'Sentadilla libre', 'barbell-squat', 4, 8, 40.0, 'Romper el paralelo en cada repetición', true, 42.5);

INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (3, 4, 2, 'LUNES', 'Press francés', 'skull-crushers', 3, 12, 15.0, 'Codos cerrados durante la bajada', false, null);

-- Miércoles
INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (4, 4, 2, 'MIERCOLES', 'Jalón al pecho', 'lat-pulldown', 4, 10, 45.0, 'Retracción escapular antes de traccionar', false, null);

INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (5, 4, 2, 'MIERCOLES', 'Remo con barra', 'barbell-row', 3, 10, 35.0, 'Espalda recta a 45 grados', false, null);

-- Viernes
INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (6, 4, 2, 'VIERNES', 'Sentadilla búlgara', 'bulgarian-split-squat', 3, 12, 10.0, 'Mancuernas a los lados, foco en cuádriceps', false, null);

INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (7, 4, 2, 'VIERNES', 'Press de hombros con mancuernas', 'dumbbell-shoulder-press', 4, 10, 14.0, 'No arquear la zona lumbar', false, null);

-- Rutina de María (Socio 5 - Fútbol)
-- Martes
INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (8, 5, 3, 'MARTES', 'Peso muerto', 'deadlift', 4, 6, 50.0, 'Mantener la barra pegada a las tibias', false, null);

INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (9, 5, 3, 'MARTES', 'Curl de bíceps', 'bicep-curl', 3, 12, 8.0, 'Sin balancear el torso', false, null);

-- Jueves
INSERT INTO ItemRutina (id, socio_id, entrenador_id, dia, nombreEjercicio, slugEjercicio, series, repeticiones, pesoKg, observaciones, completado, pesoLevantadoKg)
VALUES (10, 5, 3, 'JUEVES', 'Press inclinado', 'incline-bench-press', 4, 10, 20.0, 'Banco a 30 grados', false, null);