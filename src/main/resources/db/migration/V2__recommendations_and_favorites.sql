CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    rationale TEXT,
    scoring_weights JSONB
);

CREATE TABLE IF NOT EXISTS recommendation_items (
    id BIGSERIAL PRIMARY KEY,
    recommendation_id BIGINT NOT NULL REFERENCES recommendations(id) ON DELETE CASCADE,
    car_id BIGINT NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    score NUMERIC(6,2) NOT NULL,
    score_breakdown JSONB,
    rank INTEGER NOT NULL,
    CONSTRAINT uq_reco_item UNIQUE (recommendation_id, car_id)
);

CREATE INDEX IF NOT EXISTS idx_recommendation_items_rec_id ON recommendation_items(recommendation_id);
CREATE INDEX IF NOT EXISTS idx_recommendation_items_rank ON recommendation_items(rank);

CREATE TABLE IF NOT EXISTS favorites (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    car_id BIGINT NOT NULL REFERENCES cars(id) ON DELETE CASCADE,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_favorites_user_car UNIQUE (user_id, car_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
