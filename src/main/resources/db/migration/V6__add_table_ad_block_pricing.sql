CREATE TABLE ad_block_pricing (
        id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
        cinema_id      UUID           NOT NULL UNIQUE,
        price_per_day  NUMERIC(10, 2) NOT NULL CHECK (price_per_day > 0),
        updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);