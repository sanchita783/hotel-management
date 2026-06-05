-- ============================================================
--  HOTEL MANAGEMENT SYSTEM - SEED DATA
--  Run AFTER schema.sql
--  Admin password: Admin@1234 (BCrypt encoded)
-- ============================================================

USE hotel_db;

-- ─────────────────────────────────────────
-- DEFAULT ADMIN USER
-- ─────────────────────────────────────────
INSERT IGNORE INTO users (first_name, last_name, email, password, phone, role,
                           is_active, email_verified)
VALUES ('Hotel', 'Admin',
        'admin@grandhotel.com',
        '$2a$12$LQ1GkGBXIqHPuGcqRW4EueN6yEGfxnHyEmJvSBijfpgRXzUG7y5tu',
        '9000000001',
        'ADMIN', TRUE, TRUE);

-- Sample Customer
INSERT IGNORE INTO users (first_name, last_name, email, password, phone, role,
                           is_active, email_verified)
VALUES ('Rahul', 'Sharma',
        'rahul@example.com',
        '$2a$12$LQ1GkGBXIqHPuGcqRW4EueN6yEGfxnHyEmJvSBijfpgRXzUG7y5tu',
        '9876543210',
        'CUSTOMER', TRUE, TRUE);

-- ─────────────────────────────────────────
-- ROOMS
-- ─────────────────────────────────────────
INSERT IGNORE INTO rooms (room_number, room_type, price_per_night, capacity,
                           description, floor_number, room_size, room_status, is_active)
VALUES
('101', 'SINGLE',  1200.00, 1, 'Cozy single room with city view and complimentary breakfast', 1, 200.0, 'AVAILABLE', TRUE),
('102', 'SINGLE',  1200.00, 1, 'Compact single room with garden view', 1, 200.0, 'AVAILABLE', TRUE),
('103', 'DOUBLE',  2200.00, 2, 'Spacious double room with king bed and mountain view', 1, 320.0, 'AVAILABLE', TRUE),
('104', 'DOUBLE',  2200.00, 2, 'Comfortable double room with twin beds', 1, 320.0, 'AVAILABLE', TRUE),
('201', 'TWIN',    2400.00, 2, 'Twin room with two single beds and pool access', 2, 300.0, 'AVAILABLE', TRUE),
('202', 'DELUXE',  3500.00, 2, 'Deluxe room with sea view, bathtub, and lounge area', 2, 450.0, 'AVAILABLE', TRUE),
('203', 'FAMILY',  4200.00, 4, 'Family room with 2 bedrooms, living area, and kitchenette', 2, 600.0, 'AVAILABLE', TRUE),
('301', 'SUITE',   7500.00, 2, 'Executive suite with private balcony, Jacuzzi, and butler service', 3, 800.0, 'AVAILABLE', TRUE),
('302', 'STUDIO',  2800.00, 2, 'Studio apartment with kitchenette and workspace', 3, 380.0, 'AVAILABLE', TRUE),
('401', 'PRESIDENTIAL', 25000.00, 4, 'Presidential suite with panoramic views, private pool, and personal butler', 4, 2000.0, 'AVAILABLE', TRUE);

-- ─────────────────────────────────────────
-- ROOM AMENITIES
-- ─────────────────────────────────────────
-- Single rooms (101, 102)
INSERT INTO room_amenities (room_id, amenity)
SELECT r.id, a.amenity
FROM rooms r,
(SELECT 'Free WiFi' amenity UNION SELECT 'Air Conditioning' UNION SELECT 'LCD TV'
 UNION SELECT 'Mini Fridge' UNION SELECT 'Room Service') a
WHERE r.room_number IN ('101','102');

-- Double rooms (103, 104)
INSERT INTO room_amenities (room_id, amenity)
SELECT r.id, a.amenity
FROM rooms r,
(SELECT 'Free WiFi' amenity UNION SELECT 'Air Conditioning' UNION SELECT 'Smart TV'
 UNION SELECT 'Mini Bar' UNION SELECT 'Safe Deposit' UNION SELECT 'Room Service'
 UNION SELECT 'Daily Housekeeping') a
WHERE r.room_number IN ('103','104');

-- Suite (301)
INSERT INTO room_amenities (room_id, amenity)
SELECT r.id, a.amenity
FROM rooms r,
(SELECT 'Free WiFi' amenity UNION SELECT 'Air Conditioning' UNION SELECT '55" Smart TV'
 UNION SELECT 'Jacuzzi' UNION SELECT 'Mini Bar' UNION SELECT 'Safe Deposit'
 UNION SELECT '24/7 Butler Service' UNION SELECT 'Private Balcony'
 UNION SELECT 'Espresso Machine' UNION SELECT 'Premium Toiletries') a
WHERE r.room_number = '301';

-- Presidential (401)
INSERT INTO room_amenities (room_id, amenity)
SELECT r.id, a.amenity
FROM rooms r,
(SELECT 'Free WiFi' amenity UNION SELECT 'Air Conditioning' UNION SELECT '75" Smart TV'
 UNION SELECT 'Private Pool' UNION SELECT 'Jacuzzi' UNION SELECT 'Full Bar'
 UNION SELECT 'Personal Butler' UNION SELECT 'Private Terrace' UNION SELECT 'Grand Piano'
 UNION SELECT 'Home Theatre' UNION SELECT 'Luxury Toiletries' UNION SELECT 'Airport Transfer') a
WHERE r.room_number = '401';
