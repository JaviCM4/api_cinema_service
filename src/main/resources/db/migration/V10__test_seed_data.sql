-- =============================================================================
-- V11 — Datos de prueba para testing con Postman
-- =============================================================================
-- IDs fijos para facilitar uso en colecciones Postman:
--
--   CINEMA     : a1b2c3d4-0000-0000-0000-000000000001
--   THEATER 1  : a1b2c3d4-0000-0000-0000-000000000011  (2D  · 5×8  = 40 asientos)
--   THEATER 2  : a1b2c3d4-0000-0000-0000-000000000012  (3D  · 4×6  = 24 asientos)
--   THEATER 3  : a1b2c3d4-0000-0000-0000-000000000013  (IMAX· 6×10 = 60 asientos)
--   MOVIE IDs  : a1b2c3d4-0000-0000-0000-0000000000A1/A2/A3
-- =============================================================================

DO $$
DECLARE
    -- ── Entidades principales ─────────────────────────────────────────────────
    v_cinema_id    UUID := 'a1b2c3d4-0000-0000-0000-000000000001';
    v_wallet_id    UUID := 'a1b2c3d4-0000-0000-0000-000000000002';

    -- IDs externos (admin y país vienen de otros microservicios — UUIDs fake)
    v_admin_id     UUID := 'aaaaaaaa-0000-0000-0000-000000000001';
    v_country_id   UUID := 'cccccccc-0000-0000-0000-000000000001';

    -- ── Salas ────────────────────────────────────────────────────────────────
    v_theater1_id  UUID := 'a1b2c3d4-0000-0000-0000-000000000011';
    v_theater2_id  UUID := 'a1b2c3d4-0000-0000-0000-000000000012';
    v_theater3_id  UUID := 'a1b2c3d4-0000-0000-0000-000000000013';

    -- ── Tipos de sala (sembrados en V5) ──────────────────────────────────────
    v_type_2d      UUID;
    v_type_3d      UUID;
    v_type_imax    UUID;

    -- ── Películas (UUIDs fake — vienen del movies-service) ───────────────────
    v_movie1_id    UUID := 'ffffffff-0006-0006-0006-000000000001';
    v_movie2_id    UUID := 'ffffffff-0006-0006-0006-000000000001';
    v_movie3_id    UUID := 'ffffffff-0006-0006-0006-000000000002';

BEGIN
    -- ── Resolver IDs de type_theater ─────────────────────────────────────────
    SELECT id INTO v_type_2d   FROM type_theater WHERE name = '2D'   LIMIT 1;
    SELECT id INTO v_type_3d   FROM type_theater WHERE name = '3D'   LIMIT 1;
    SELECT id INTO v_type_imax FROM type_theater WHERE name = 'IMAX' LIMIT 1;

    -- ── Cinema ────────────────────────────────────────────────────────────────
    INSERT INTO cinema (id, admin_cinema_id, country_id, name, address, phone, email)
    VALUES (
        v_cinema_id,
        v_admin_id,
        v_country_id,
        'CineMax Premium',
        'Av. Principal 123, Ciudad Capital',
        '+1-555-0100',
        'info@cinemax.com'
    );

    -- ── Wallet del cinema ─────────────────────────────────────────────────────
    INSERT INTO cinema_wallet (id, cinema_id, balance)
    VALUES (v_wallet_id, v_cinema_id, 15000.00);

    -- ── Transacciones de wallet ───────────────────────────────────────────────
    INSERT INTO wallet_transaction (cinema_wallet_id, amount, type, description) VALUES
        (v_wallet_id, 20000.00, 'RECHARGE', 'Depósito inicial de apertura'),
        (v_wallet_id,  5000.00, 'PAYMENT',  'Pago de costos operativos enero 2026');

    -- ── Costo operativo del cinema ────────────────────────────────────────────
    INSERT INTO operating_cost (cinema_id, daily_cost, effective_from) VALUES
        (v_cinema_id, 8500.00, '2026-01-01'),
        (v_cinema_id, 9000.00, '2026-04-01');

    -- ── Precio por día de publicidad del cinema ───────────────────────────────
    INSERT INTO ad_block_pricing (cinema_id, price_per_day)
    VALUES (v_cinema_id, 250.00);

    -- ── Bloque de publicidad activo ───────────────────────────────────────────
    INSERT INTO ad_block (cinema_id, days_blocked, start_date, end_date, amount_paid) VALUES
        (v_cinema_id, 10, '2026-05-20', '2026-05-30', 2500.00);

    -- ── Costo global vigente ──────────────────────────────────────────────────
    INSERT INTO global_cost (daily_cost, effective_from)
    VALUES (300.00, '2026-01-01')
    ON CONFLICT (effective_from) DO NOTHING;

    -- ── Salas ─────────────────────────────────────────────────────────────────
    INSERT INTO theater (id, cinema_id, type_theater_id, name, rows, cols, is_visible, allow_comments, allow_ratings) VALUES
        (v_theater1_id, v_cinema_id, v_type_2d,   'Sala 2D - A',  5, 8,  TRUE, TRUE, TRUE),
        (v_theater2_id, v_cinema_id, v_type_3d,   'Sala 3D - B',  4, 6,  TRUE, TRUE, TRUE),
        (v_theater3_id, v_cinema_id, v_type_imax, 'Sala IMAX',    6, 10, TRUE, TRUE, FALSE);

    -- ── Asientos: Sala 1 (5 filas A-E × 8 columnas) ──────────────────────────
    INSERT INTO seat (theater_id, row_name, col_number)
    SELECT v_theater1_id, chr(64 + r), c
    FROM generate_series(1, 5) r, generate_series(1, 8) c;

    -- ── Asientos: Sala 2 (4 filas A-D × 6 columnas) ──────────────────────────
    INSERT INTO seat (theater_id, row_name, col_number)
    SELECT v_theater2_id, chr(64 + r), c
    FROM generate_series(1, 4) r, generate_series(1, 6) c;

    -- ── Asientos: Sala 3 (6 filas A-F × 10 columnas) ─────────────────────────
    INSERT INTO seat (theater_id, row_name, col_number)
    SELECT v_theater3_id, chr(64 + r), c
    FROM generate_series(1, 6) r, generate_series(1, 10) c;

    -- ── Precios de sala ───────────────────────────────────────────────────────
    INSERT INTO theater_pricing (theater_id, type_theater_id, price, effective_date) VALUES
        (v_theater1_id, v_type_2d,    85.00, '2026-01-01'),
        (v_theater2_id, v_type_3d,   120.00, '2026-01-01'),
        (v_theater3_id, v_type_imax, 180.00, '2026-01-01');

    -- ── Funciones (fechas futuras, sin solapamiento por sala) ─────────────────
    -- Sala 1 (2D) — 20 mayo 2026
    INSERT INTO showtime (theater_id, movie_id, version_type, date_showtime, start_showtime, end_showtime) VALUES
        (v_theater1_id, v_movie1_id, 'ORIGINAL',  '2026-05-25', '10:00', '12:15'),
        (v_theater1_id, v_movie2_id, 'DUBBED',    '2026-05-25', '13:00', '15:00'),
        (v_theater1_id, v_movie3_id, 'SUBTITLED', '2026-05-24', '16:00', '18:30'),
        -- Sala 1 (2D) — 21 mayo 2026
        (v_theater1_id, v_movie2_id, 'ORIGINAL',  '2026-05-26', '11:00', '13:00'),
        (v_theater1_id, v_movie1_id, 'SUBTITLED', '2026-05-26', '15:30', '17:45');

    -- Sala 2 (3D) — 20 y 21 mayo 2026
    INSERT INTO showtime (theater_id, movie_id, version_type, date_showtime, start_showtime, end_showtime) VALUES
        (v_theater2_id, v_movie1_id, 'SUBTITLED', '2026-05-25', '10:30', '12:45'),
        (v_theater2_id, v_movie3_id, 'DUBBED',    '2026-05-25', '14:00', '16:30'),
        (v_theater2_id, v_movie2_id, 'ORIGINAL',  '2026-05-25', '09:00', '11:15'),
        (v_theater2_id, v_movie1_id, 'DUBBED',    '2026-05-27', '12:00', '14:00');

    -- Sala 3 (IMAX) — 22 y 23 mayo 2026
    INSERT INTO showtime (theater_id, movie_id, version_type, date_showtime, start_showtime, end_showtime) VALUES
        (v_theater3_id, v_movie3_id, 'ORIGINAL',  '2026-05-25', '09:00', '11:30'),
        (v_theater3_id, v_movie1_id, 'SUBTITLED', '2026-05-25', '12:30', '15:00'),
        (v_theater3_id, v_movie2_id, 'DUBBED',    '2026-05-26', '16:00', '18:15'),
        (v_theater3_id, v_movie3_id, 'ORIGINAL',  '2026-05-26', '10:00', '12:30'),
        (v_theater3_id, v_movie1_id, 'ORIGINAL',  '2026-05-27', '14:00', '16:30');

    -- ── Comentarios de sala ───────────────────────────────────────────────────
    INSERT INTO room_comment (theater_id, user_id, content) VALUES
        (v_theater1_id, 'dddddddd-0000-0000-0000-000000000001', 'Excelente sonido e imagen, muy cómodos los asientos.'),
        (v_theater1_id, 'dddddddd-0000-0000-0000-000000000002', 'El aire acondicionado estaba muy fuerte.'),
        (v_theater2_id, 'dddddddd-0000-0000-0000-000000000001', 'Los efectos 3D son increíbles en esta sala.'),
        (v_theater3_id, 'dddddddd-0000-0000-0000-000000000003', 'La mejor experiencia IMAX de la ciudad.');

    -- ── Calificaciones de sala ────────────────────────────────────────────────
    INSERT INTO room_rating (theater_id, user_id, score) VALUES
        (v_theater1_id, 'dddddddd-0000-0000-0000-000000000001', 5),
        (v_theater1_id, 'dddddddd-0000-0000-0000-000000000002', 3),
        (v_theater2_id, 'dddddddd-0000-0000-0000-000000000001', 4),
        (v_theater2_id, 'dddddddd-0000-0000-0000-000000000003', 5),
        (v_theater3_id, 'dddddddd-0000-0000-0000-000000000002', 5),
        (v_theater3_id, 'dddddddd-0000-0000-0000-000000000003', 4);

END $$;
