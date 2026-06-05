-- ============================================================
--  HOTEL MANAGEMENT SYSTEM - DATABASE SCHEMA
--  MySQL 8.x compatible
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name           VARCHAR(50)  NOT NULL,
    last_name            VARCHAR(50)  NOT NULL,
    email                VARCHAR(100) NOT NULL UNIQUE,
    password             VARCHAR(255) NOT NULL,
    phone                VARCHAR(15)  UNIQUE,
    address              VARCHAR(255),
    profile_image        VARCHAR(500),
    role                 ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified       BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token   VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expiry DATETIME,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email   (email),
    INDEX idx_users_role    (role),
    INDEX idx_users_active  (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- ROOMS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rooms (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number    VARCHAR(10)  NOT NULL UNIQUE,
    room_type      ENUM('SINGLE','DOUBLE','TWIN','SUITE','DELUXE','PRESIDENTIAL','FAMILY','STUDIO') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    capacity       INT NOT NULL,
    description    TEXT,
    floor_number   INT,
    room_size      DOUBLE,
    room_status    ENUM('AVAILABLE','OCCUPIED','MAINTENANCE','RESERVED','CLEANING') NOT NULL DEFAULT 'AVAILABLE',
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rooms_type   (room_type),
    INDEX idx_rooms_status (room_status),
    INDEX idx_rooms_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS room_amenities (
    room_id  BIGINT      NOT NULL,
    amenity  VARCHAR(100) NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS room_images (
    room_id   BIGINT       NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ─────────────────────────────────────────
-- BOOKINGS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_reference   VARCHAR(20)    NOT NULL UNIQUE,
    user_id             BIGINT         NOT NULL,
    room_id             BIGINT         NOT NULL,
    check_in_date       DATE           NOT NULL,
    check_out_date      DATE           NOT NULL,
    number_of_guests    INT            NOT NULL,
    total_amount        DECIMAL(10,2),
    advance_payment     DECIMAL(10,2)  DEFAULT 0.00,
    balance_amount      DECIMAL(10,2),
    booking_status      ENUM('PENDING','CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED','NO_SHOW')
                            NOT NULL DEFAULT 'PENDING',
    special_requests    TEXT,
    cancellation_reason TEXT,
    cancelled_at        DATETIME,
    checked_in_at       DATETIME,
    checked_out_at      DATETIME,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_bookings_user    (user_id),
    INDEX idx_bookings_room    (room_id),
    INDEX idx_bookings_status  (booking_status),
    INDEX idx_bookings_checkin (check_in_date),
    INDEX idx_bookings_ref     (booking_reference)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- PAYMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id   VARCHAR(50)   NOT NULL UNIQUE,
    booking_id       BIGINT        NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    payment_method   ENUM('CASH','CREDIT_CARD','DEBIT_CARD','UPI','NET_BANKING','WALLET','BANK_TRANSFER') NOT NULL,
    payment_status   ENUM('PENDING','COMPLETED','FAILED','REFUNDED','PARTIALLY_REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_type     ENUM('ADVANCE','FULL_PAYMENT','BALANCE','REFUND','SERVICE_CHARGE'),
    payment_date     DATETIME,
    gateway_reference VARCHAR(100),
    failure_reason   VARCHAR(255),
    notes            TEXT,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_payments_booking (booking_id),
    INDEX idx_payments_status  (payment_status),
    INDEX idx_payments_txn     (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- HOTEL SERVICES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS hotel_services (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id          BIGINT        NOT NULL,
    service_type        ENUM('LAUNDRY','ROOM_SERVICE','RESTAURANT','SPA','GYM',
                             'AIRPORT_TRANSFER','CONCIERGE','HOUSEKEEPING') NOT NULL,
    service_description TEXT,
    amount              DECIMAL(10,2),
    service_status      ENUM('REQUESTED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'REQUESTED',
    requested_at        DATETIME,
    completed_at        DATETIME,
    staff_notes         TEXT,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    INDEX idx_services_booking (booking_id),
    INDEX idx_services_status  (service_status),
    INDEX idx_services_type    (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- ENQUIRIES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS enquiries (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT,
    guest_name       VARCHAR(100),
    guest_email      VARCHAR(100),
    guest_phone      VARCHAR(15),
    subject          VARCHAR(200) NOT NULL,
    message          TEXT         NOT NULL,
    response         TEXT,
    enquiry_status   ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    enquiry_type     ENUM('BOOKING','ROOM_INFO','SERVICES','COMPLAINT','FEEDBACK','PRICING','OTHER'),
    responded_at     DATETIME,
    responded_by     VARCHAR(100),
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_enquiries_user   (user_id),
    INDEX idx_enquiries_status (enquiry_status),
    INDEX idx_enquiries_email  (guest_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
