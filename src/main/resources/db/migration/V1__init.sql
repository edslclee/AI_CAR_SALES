-- Flyway migration: initial schema for core domain tables
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    budget_min INTEGER,
    budget_max INTEGER,
    usage VARCHAR(100),
    passengers SMALLINT,
    preferred_body_types TEXT[],
    preferred_brands TEXT[],
    year_range_start SMALLINT,
    year_range_end SMALLINT,
    mileage_range_start INTEGER,
    mileage_range_end INTEGER,
    options JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cars (
    id BIGSERIAL PRIMARY KEY,
    oem_code VARCHAR(100) NOT NULL,
    model_name VARCHAR(150) NOT NULL,
    trim VARCHAR(150),
    price INTEGER,
    body_type VARCHAR(50),
    fuel_type VARCHAR(50),
    efficiency NUMERIC(6,2),
    seats SMALLINT,
    drivetrain VARCHAR(50),
    release_year SMALLINT,
    features JSONB,
    media_assets JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cars_oem_model_trim UNIQUE (oem_code, model_name, trim)
);

CREATE INDEX IF NOT EXISTS idx_preferences_user_id ON preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_cars_body_type ON cars(body_type);
CREATE INDEX IF NOT EXISTS idx_cars_release_year ON cars(release_year);
