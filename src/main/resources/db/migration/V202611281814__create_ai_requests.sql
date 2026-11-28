-- Tao bang ai_requests luu lich su va gioi han tan suat goi tro ly AI
CREATE TABLE ai_requests (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    request_type   VARCHAR(40)  NOT NULL,
    prompt_summary VARCHAR(300) NOT NULL,
    response_text  TEXT         NOT NULL,
    used_fallback  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)  NOT NULL,
    CONSTRAINT fk_ai_requests_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ai_requests_user_created ON ai_requests (user_id, created_at);
