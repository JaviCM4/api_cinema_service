-- 1. Agregar columna temporal con el valor del enum
ALTER TABLE showtime ADD COLUMN version_type_new VARCHAR(50);

-- 2. Poblar basado en el nombre de la tabla version_type
UPDATE showtime s
SET version_type_new = CASE
    WHEN vt.name ILIKE 'Original'    THEN 'ORIGINAL'
    WHEN vt.name ILIKE 'Subtitulada' THEN 'SUBTITLED'
    WHEN vt.name ILIKE 'Doblada'     THEN 'DUBBED'
    ELSE UPPER(vt.name)
END
FROM version_type vt
WHERE s.version_type = vt.id;

-- 3. Asignar NOT NULL
ALTER TABLE showtime ALTER COLUMN version_type_new SET NOT NULL;

-- 4. Eliminar la columna FK original
ALTER TABLE showtime DROP COLUMN version_type;

-- 5. Renombrar la nueva columna
ALTER TABLE showtime RENAME COLUMN version_type_new TO version_type;

-- 6. Eliminar la tabla version_type
DROP TABLE version_type;
