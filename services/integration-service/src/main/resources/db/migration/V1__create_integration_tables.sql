-- Integration Service schema
-- V1: Create integration_requests and integration_responses tables

CREATE TABLE IF NOT EXISTS integration_requests (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id      UUID         NOT NULL,
    supplier_id     VARCHAR(50),
    supplier_type   VARCHAR(20),
    request_payload TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER      DEFAULT 0,
    trace_id        VARCHAR(64),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_integration_req_booking_id
    ON integration_requests (booking_id);

CREATE INDEX IF NOT EXISTS idx_integration_req_status
    ON integration_requests (status);

CREATE INDEX IF NOT EXISTS idx_integration_req_supplier_type
    ON integration_requests (supplier_type);


CREATE TABLE IF NOT EXISTS integration_responses (
    id                     UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_request_id UUID      NOT NULL REFERENCES integration_requests (id),
    response_payload       TEXT,
    http_status            INTEGER,
    success                BOOLEAN,
    error_message          VARCHAR(500),
    processing_time_ms     BIGINT,
    received_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_integration_resp_request_id
    ON integration_responses (integration_request_id);
