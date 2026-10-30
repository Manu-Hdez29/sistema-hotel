/* ============================================================
   HotelReserva - script de la base de datos
   Proyecto: sistema-hotel (DWF404, G04L)

   Orden para correrlo:
   1. Parte 1: crea la base.
   2. Corremos el proyecto en IntelliJ una vez. Hibernate crea las tablas
      y DataInitializer carga los datos de prueba.
   3. Parte 2: crea el índice. Necesita que las tablas ya existan.
   4. Parte 3 es opcional, solo para revisar.
   ============================================================ */


/* ------------------------------------------------------------
   PARTE 1. Crear la base (solo si no existe)
   ------------------------------------------------------------ */
IF DB_ID('HotelReserva') IS NULL
    CREATE DATABASE HotelReserva;
GO

USE HotelReserva;
GO


/* ------------------------------------------------------------
   PARTE 2. Índice único filtrado: un solo titular por reserva
   Hibernate no crea este índice, así que lo hacemos a mano.
   ------------------------------------------------------------ */
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'ux_reserva_titular'
      AND object_id = OBJECT_ID('dbo.reserva_huesped'))
    CREATE UNIQUE INDEX ux_reserva_titular
    ON dbo.reserva_huesped (id_reserva)
    WHERE es_titular = 1;
GO

-- Verifico que el índice quedó creado (debe devolver una fila)
SELECT name, is_unique, filter_definition
FROM sys.indexes
WHERE object_id = OBJECT_ID('dbo.reserva_huesped')
  AND name = 'ux_reserva_titular';
GO


/* ------------------------------------------------------------
   PARTE 3. Consultas de revisión (no modifican datos)
   Las usamos para comparar la base con el modelo y el manual.
   ------------------------------------------------------------ */

-- 3.1 Tablas de la base (deben ser 17)
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- 3.2 Versión de SQL Server
SELECT @@VERSION;

-- 3.3 sucursal y tipo_habitacion
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'sucursal';

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.sucursal');

SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tipo_habitacion';

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.tipo_habitacion');

-- 3.4 habitacion
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'habitacion';

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.habitacion');

SELECT name, type_desc
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('dbo.habitacion');

-- 3.5 tarifa
SELECT COLUMN_NAME, DATA_TYPE, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tarifa';

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.tarifa');

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.tarifa');

-- 3.6 disponibilidad
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'disponibilidad';

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.disponibilidad');

SELECT name, type_desc
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('dbo.disponibilidad');

-- 3.7 rol y usuario
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('rol', 'usuario')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.usuario');

SELECT OBJECT_NAME(parent_object_id) AS tabla, name, type_desc
FROM sys.key_constraints
WHERE OBJECT_NAME(parent_object_id) IN ('rol', 'usuario');

-- 3.8 huesped
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'huesped';

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.huesped');

SELECT name, type_desc
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('dbo.huesped');

-- 3.9 servicio
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'servicio';

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.servicio');

-- 3.10 reserva, reserva_huesped y reserva_servicio
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'reserva'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.reserva');

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.reserva');

SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'reserva_huesped'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.reserva_huesped');

SELECT COLUMN_NAME, DATA_TYPE, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'reserva_servicio'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.reserva_servicio');

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.reserva_servicio');

-- 3.11 politica_cancelacion y cancelacion
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'politica_cancelacion'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.politica_cancelacion');

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.politica_cancelacion');

SELECT COLUMN_NAME, DATA_TYPE, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'cancelacion'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.cancelacion');

SELECT name, type_desc
FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('dbo.cancelacion');

-- 3.12 pago y reclamo
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'pago'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.pago');

SELECT name, definition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.pago');

SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'reclamo'
ORDER BY ORDINAL_POSITION;

SELECT name, OBJECT_NAME(referenced_object_id) AS tabla_referenciada
FROM sys.foreign_keys
WHERE parent_object_id = OBJECT_ID('dbo.reclamo');

-- 3.13 Datos de prueba (catálogos y usuarios)
SELECT nombre_rol FROM rol;
SELECT email, estado FROM usuario;
SELECT id_huesped, nombre_completo, documento_identidad, id_usuario FROM huesped;
SELECT COUNT(*) AS habitaciones FROM habitacion;
SELECT COUNT(*) AS tarifas FROM tarifa;
SELECT TOP 5 id_habitacion, numero_habitacion, id_sucursal FROM habitacion ORDER BY id_habitacion;
SELECT id_servicio, nombre, precio FROM servicio;

-- 3.14 Reservas y pagos (después de probar la API)
SELECT id_reserva, id_habitacion, fecha_entrada, fecha_salida, estado_reserva FROM reserva;
SELECT id_reserva, estado_reserva, precio_total FROM reserva;
SELECT * FROM reserva_huesped;
SELECT * FROM reserva_servicio;
SELECT id_pago, id_reserva, metodo_pago, monto, estado_pago FROM pago;
SELECT * FROM cancelacion;
GO