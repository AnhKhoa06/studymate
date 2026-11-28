-- Tao bang ho so hoc tap, mon hoc va khung gio ranh cho module user
CREATE TABLE student_profiles (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    faculty        VARCHAR(100) NULL,
    class_name     VARCHAR(50)  NULL,
    academic_level VARCHAR(50)  NULL,
    study_goal     VARCHAR(50)  NULL,
    bio            VARCHAR(500) NULL,
    updated_at     DATETIME(6)  NOT NULL,
    CONSTRAINT uq_student_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_student_profiles_faculty ON student_profiles (faculty);
CREATE INDEX idx_student_profiles_goal ON student_profiles (study_goal);
CREATE INDEX idx_student_profiles_level ON student_profiles (academic_level);

CREATE TABLE profile_subjects (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id   BIGINT       NOT NULL,
    subject_code VARCHAR(30)  NOT NULL,
    subject_name VARCHAR(120) NOT NULL,
    CONSTRAINT uq_profile_subject UNIQUE (profile_id, subject_code),
    CONSTRAINT fk_profile_subjects_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_profile_subjects_code ON profile_subjects (subject_code);
CREATE INDEX idx_profile_subjects_name ON profile_subjects (subject_name);

CREATE TABLE availability_slots (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id  BIGINT  NOT NULL,
    day_of_week TINYINT NOT NULL,
    start_time  TIME    NOT NULL,
    end_time    TIME    NOT NULL,
    CONSTRAINT fk_availability_slots_profile FOREIGN KEY (profile_id) REFERENCES student_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_availability_profile_day ON availability_slots (profile_id, day_of_week, start_time, end_time);
