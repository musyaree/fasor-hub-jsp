CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role CHAR(1) NOT NULL DEFAULT '1',
    identity VARCHAR(30) NULL,
    phone VARCHAR(20) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY users_email_unique (email),
    CONSTRAINT users_role_check CHECK (role IN ('1', '2'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facility_types (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY facility_types_name_unique (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS facilities (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type_id BIGINT UNSIGNED NOT NULL,
    description VARCHAR(200) NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT facilities_type_fk FOREIGN KEY (type_id) REFERENCES facility_types (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    facility_id BIGINT UNSIGNED NOT NULL,
    `date` DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status CHAR(1) NOT NULL DEFAULT '1',
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY time_slots_facility_date_start_unique (facility_id, `date`, start_time),
    CONSTRAINT time_slots_facility_fk FOREIGN KEY (facility_id) REFERENCES facilities (id) ON DELETE RESTRICT,
    CONSTRAINT time_slots_status_check CHECK (status IN ('1', '2', '3'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    organization_unit VARCHAR(50) NOT NULL,
    participant_count INT NOT NULL,
    contact VARCHAR(20) NOT NULL,
    purpose VARCHAR(200) NOT NULL,
    status CHAR(1) NOT NULL DEFAULT '1',
    rejection_reason VARCHAR(255) NULL,
    cancellation_reason VARCHAR(255) NULL,
    previous_status CHAR(1) NULL,
    cancellation_requested_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT bookings_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT bookings_status_check CHECK (status IN ('1', '2', '3', '4', '5')),
    CONSTRAINT bookings_previous_status_check CHECK (previous_status IN ('1', '2'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS booking_time_slot (
    booking_id BIGINT UNSIGNED NOT NULL,
    time_slot_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (booking_id, time_slot_id),
    CONSTRAINT bts_booking_fk FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT bts_slot_fk FOREIGN KEY (time_slot_id) REFERENCES time_slots (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
