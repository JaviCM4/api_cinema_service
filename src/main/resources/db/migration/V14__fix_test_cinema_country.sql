-- Corrige el country_id del cinema de prueba para que coincida
-- con el país real registrado en el movies-service (Estados Unidos).
UPDATE cinema
SET country_id = 'aaaaaaaa-0001-0001-0001-000000000001'
WHERE id = 'a1b2c3d4-0000-0000-0000-000000000001';
