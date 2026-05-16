-- ============================================
-- BASE DE DATOS: BDNotas
-- Control de Notas - Programación 1
-- ============================================

CREATE DATABASE IF NOT EXISTS BDNotas;
USE BDNotas;

-- Tabla de Alumnos
CREATE TABLE IF NOT EXISTS alumnos (
    carnet VARCHAR(10) PRIMARY KEY,
    nombres VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    seccion CHAR(1) NOT NULL CHECK (seccion IN ('A', 'B'))
);

-- Tabla de Notas
CREATE TABLE IF NOT EXISTS notas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    carnet VARCHAR(10) NOT NULL,
    nota1 DECIMAL(5,2) DEFAULT 0,
    nota2 DECIMAL(5,2) DEFAULT 0,
    nota3 DECIMAL(5,2) DEFAULT 0,
    promedio DECIMAL(5,2) GENERATED ALWAYS AS ((nota1 + nota2 + nota3) / 3) STORED,
    FOREIGN KEY (carnet) REFERENCES alumnos(carnet) ON DELETE CASCADE
);

-- Datos de prueba (opcional)
INSERT INTO alumnos VALUES ('2024001', 'Juan Carlos', 'Pérez García', 'A');
INSERT INTO alumnos VALUES ('2024002', 'María', 'López Ramírez', 'A');
INSERT INTO alumnos VALUES ('2024003', 'Pedro', 'Hernández Cruz', 'B');
INSERT INTO alumnos VALUES ('2024004', 'Ana Lucía', 'González Díaz', 'B');

INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES ('2024001', 85, 90, 78);
INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES ('2024002', 92, 88, 95);
INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES ('2024003', 70, 75, 80);
INSERT INTO notas (carnet, nota1, nota2, nota3) VALUES ('2024004', 88, 82, 91);
