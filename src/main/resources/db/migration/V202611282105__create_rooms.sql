-- Tao bang rooms cho module quan ly phong hoc
CREATE TABLE rooms (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(30)  NOT NULL,
    building   VARCHAR(50)  NOT NULL,
    capacity   INT          NOT NULL,
    equipment  VARCHAR(255) NOT NULL DEFAULT '',
    status     VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    version    BIGINT       NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT uq_rooms_code UNIQUE (code),
    CONSTRAINT chk_rooms_capacity CHECK (capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rooms_status_building_capacity ON rooms (status, building, capacity);
