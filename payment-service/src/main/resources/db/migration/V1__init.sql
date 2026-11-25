-- Payment service schema (schema selected via datasource currentSchema=payments).
CREATE TABLE payment (
    id              UUID         NOT NULL,
    source_event_id UUID         NOT NULL,
    order_id        UUID         NOT NULL,
    approved        BOOLEAN      NOT NULL,
    transaction_id  VARCHAR(255),
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id),
    CONSTRAINT uq_payment_source_event UNIQUE (source_event_id)
);
