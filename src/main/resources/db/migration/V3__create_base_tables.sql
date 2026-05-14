CREATE TABLE type_theater (
    id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE version_type (
    id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name  VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE cinema (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_cinema_id  UUID         NOT NULL,
    country_id       UUID         NOT NULL,
    name             VARCHAR(255) NOT NULL,
    address          VARCHAR(500),
    phone            VARCHAR(20),
    email            VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE cinema_wallet (
    id         UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    cinema_id  UUID          NOT NULL UNIQUE REFERENCES cinema(id),
    balance    NUMERIC(10,2) NOT NULL DEFAULT 0.00
        CONSTRAINT chk_cinema_balance CHECK (balance >= 0),
    updated_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE wallet_transaction (
    id                UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    cinema_wallet_id  UUID           NOT NULL REFERENCES cinema_wallet(id),
    amount            NUMERIC(10,2)  NOT NULL CHECK (amount > 0),
    type              wallet_tx_type NOT NULL,
    description       VARCHAR(255),
    transaction_date  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE operating_cost (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    cinema_id       UUID          NOT NULL REFERENCES cinema(id),
    daily_cost      NUMERIC(10,2) NOT NULL CHECK (daily_cost >= 0),
    effective_from  DATE          NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE ad_block (
    id            UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    cinema_id     UUID          NOT NULL REFERENCES cinema(id),
    days_blocked  INTEGER       NOT NULL CHECK (days_blocked > 0),
    start_date    DATE          NOT NULL,
    end_date      DATE          NOT NULL,
    amount_paid   NUMERIC(10,2) NOT NULL CHECK (amount_paid >= 0),
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_ad_block_dates CHECK (end_date > start_date)
);

CREATE TABLE theater (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    cinema_id       UUID         NOT NULL REFERENCES cinema(id),
    type_theater_id UUID         NOT NULL REFERENCES type_theater(id),
    name            VARCHAR(255) NOT NULL,
    rows            INTEGER      NOT NULL CHECK (rows > 0),
    cols            INTEGER      NOT NULL CHECK (cols > 0),
    is_visible      BOOLEAN      NOT NULL DEFAULT TRUE,
    allow_comments  BOOLEAN      NOT NULL DEFAULT TRUE,
    allow_ratings   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE theater_pricing (
    id               UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id       UUID          NOT NULL REFERENCES theater(id),
    type_theater_id  UUID          NOT NULL REFERENCES type_theater(id),
    price            NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    effective_date   DATE          NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE seat (
    id          UUID       PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id  UUID       NOT NULL REFERENCES theater(id),
    row_name    VARCHAR(5) NOT NULL,
    col_number  INTEGER    NOT NULL CHECK (col_number > 0),
    is_active   BOOLEAN    NOT NULL DEFAULT TRUE,
    UNIQUE (theater_id, row_name, col_number)
);

CREATE TABLE showtime (
    id              UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id      UUID      NOT NULL REFERENCES theater(id),
    movie_id        UUID      NOT NULL,
    version_type    UUID      NOT NULL REFERENCES version_type(id),
    date_showtime   DATE      NOT NULL,
    start_showtime  TIME      NOT NULL,
    end_showtime    TIME      NOT NULL,
    is_active       BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_showtime_times CHECK (end_showtime > start_showtime)
);

CREATE TABLE room_comment (
    id          UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id  UUID      NOT NULL REFERENCES theater(id),
    user_id     UUID      NOT NULL,
    content     TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE room_rating (
    id          UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id  UUID      NOT NULL REFERENCES theater(id),
    user_id     UUID      NOT NULL,
    score       SMALLINT  NOT NULL CHECK (score BETWEEN 1 AND 5),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (theater_id, user_id)
);