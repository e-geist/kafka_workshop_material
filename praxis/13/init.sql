-- Daten des Producers
CREATE TABLE measurement (
    id        BIGSERIAL PRIMARY KEY,
    sensor_id TEXT        NOT NULL,
    value     INT         NOT NULL,
    ts        TIMESTAMPTZ NOT NULL
);

-- Part 2: Events, die noch an Kafka gesendet werden müssen
CREATE TABLE outbox (
    id           BIGSERIAL PRIMARY KEY,
    event_id     UUID        NOT NULL,
    message_key  TEXT        NOT NULL,
    payload      TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);

-- Part 3: Daten des Consumers (in der Praxis eigene Datenbank)
CREATE TABLE processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sensor_total (
    sensor_id         TEXT PRIMARY KEY,
    measurement_count INT    NOT NULL,
    value_sum         BIGINT NOT NULL
);
