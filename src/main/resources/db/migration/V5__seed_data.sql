INSERT INTO type_theater (id, name) VALUES
    (uuid_generate_v4(), '2D'),
    (uuid_generate_v4(), '3D'),
    (uuid_generate_v4(), 'IMAX'),
    (uuid_generate_v4(), '4DX');

INSERT INTO version_type (id, name) VALUES
    (uuid_generate_v4(), 'Original'),
    (uuid_generate_v4(), 'Subtitulada'),
    (uuid_generate_v4(), 'Doblada');