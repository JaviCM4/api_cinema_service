CREATE TABLE companies (
    id          UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO companies (name)
SELECT DISTINCT TRIM(name)
FROM cinema
WHERE name IS NOT NULL
  AND TRIM(name) <> ''
ON CONFLICT (name) DO NOTHING;

ALTER TABLE cinema ADD COLUMN company_id UUID;

UPDATE cinema c
SET company_id = cp.id
FROM companies cp
WHERE c.company_id IS NULL
  AND cp.name = TRIM(c.name);

ALTER TABLE cinema
    ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE cinema
    ADD CONSTRAINT fk_cinema_company
    FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE cinema
    ALTER COLUMN admin_cinema_id DROP NOT NULL;

CREATE INDEX idx_cinema_company_id ON cinema(company_id);
