CREATE TABLE global_cost (
    id              UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    daily_cost      NUMERIC(10,2) NOT NULL CHECK (daily_cost > 0),
    effective_from  DATE          NOT NULL UNIQUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
