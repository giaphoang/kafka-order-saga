-- Order service schema (schema selected via datasource currentSchema=orders).
-- Flyway owns all tables for this service; docker/postgres/init.sql only creates the schemas.
CREATE TABLE customer_order (
    id            UUID         NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    total         NUMERIC(19,2) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    detail        VARCHAR(255),
    created_at    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_customer_order PRIMARY KEY (id)
);

CREATE TABLE outbox_event (
    id           UUID         NOT NULL,
    aggregate_id UUID         NOT NULL,
    topic        VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_outbox_event PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_event_unpublished ON outbox_event (created_at) WHERE published_at IS NULL;
