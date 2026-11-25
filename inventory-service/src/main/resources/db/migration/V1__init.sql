-- Inventory service schema (schema selected via datasource currentSchema=inventory).
CREATE TABLE stock_item (
    sku       VARCHAR(255) NOT NULL,
    available INTEGER      NOT NULL,
    CONSTRAINT pk_stock_item PRIMARY KEY (sku)
);

CREATE TABLE stock_reservation (
    id       UUID         NOT NULL,
    order_id UUID         NOT NULL,
    sku      VARCHAR(255) NOT NULL,
    quantity INTEGER      NOT NULL,
    released BOOLEAN      NOT NULL,
    CONSTRAINT pk_stock_reservation PRIMARY KEY (id)
);

CREATE INDEX idx_stock_reservation_order ON stock_reservation (order_id) WHERE NOT released;

CREATE TABLE processed_event (
    event_id     UUID NOT NULL,
    processed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_processed_event PRIMARY KEY (event_id)
);
