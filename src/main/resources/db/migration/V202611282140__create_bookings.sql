-- Tao bang bookings cho module dat phong hoc
CREATE TABLE bookings (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id              BIGINT       NOT NULL,
    group_id             BIGINT       NOT NULL,
    requested_by_user_id BIGINT       NOT NULL,
    start_time           DATETIME(6)  NOT NULL,
    end_time             DATETIME(6)  NOT NULL,
    purpose              VARCHAR(255) NOT NULL DEFAULT '',
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason     VARCHAR(255) NULL,
    reviewed_by_user_id  BIGINT       NULL,
    reviewed_at          DATETIME(6)  NULL,
    version              BIGINT       NOT NULL DEFAULT 0,
    created_at           DATETIME(6)  NOT NULL,
    updated_at           DATETIME(6)  NOT NULL,
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_bookings_requester FOREIGN KEY (requested_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_reviewer FOREIGN KEY (reviewed_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_bookings_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookings_room_time ON bookings (room_id, start_time, end_time);
CREATE INDEX idx_bookings_group_status ON bookings (group_id, status);
CREATE INDEX idx_bookings_status_start ON bookings (status, start_time);
