CREATE DATABASE carnival_db;
USE carnival_db;

-- 1. Roles
CREATE TABLE Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 2. Employees
CREATE TABLE Employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    cnic VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    role_id INT NOT NULL,
    hire_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    salary DECIMAL(10,2) NOT NULL CHECK (salary > 0),
    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (role_id) REFERENCES Roles(role_id)
);

-- 3. Performers
CREATE TABLE Performers (
    performer_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL UNIQUE,
    stage_name VARCHAR(100),
    specialty VARCHAR(100) NOT NULL,
    experience_years INT CHECK (experience_years >= 0),
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);

-- 4. Venues
CREATE TABLE Venues (
    venue_id INT AUTO_INCREMENT PRIMARY KEY,
    venue_name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(150) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    status ENUM('AVAILABLE','UNDER_MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
);

-- 5. Shows
CREATE TABLE Shows (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    genre VARCHAR(50),
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    description TEXT,
    status ENUM('ACTIVE','RETIRED') NOT NULL DEFAULT 'ACTIVE'
);

-- 6. ShowPerformers (junction table — added)
CREATE TABLE ShowPerformers (
    show_id INT NOT NULL,
    performer_id INT NOT NULL,
    role_in_show VARCHAR(50),
    PRIMARY KEY (show_id, performer_id),
    FOREIGN KEY (show_id) REFERENCES Shows(show_id),
    FOREIGN KEY (performer_id) REFERENCES Performers(performer_id)
);

-- 7. ShowSchedules
CREATE TABLE ShowSchedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    venue_id INT NOT NULL,
    show_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    total_seats INT NOT NULL CHECK (total_seats > 0),
    available_seats INT NOT NULL CHECK (available_seats >= 0),
    status ENUM('SCHEDULED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'SCHEDULED',
    FOREIGN KEY (show_id) REFERENCES Shows(show_id),
    FOREIGN KEY (venue_id) REFERENCES Venues(venue_id),
    CHECK (end_time > start_time),
    UNIQUE (venue_id, show_date, start_time)
);

-- 8. Customers
CREATE TABLE Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    registration_date DATE NOT NULL DEFAULT (CURRENT_DATE)
);

-- 9. DiscountCodes
CREATE TABLE DiscountCodes (
    discount_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    discount_percent DECIMAL(5,2) NOT NULL CHECK (discount_percent BETWEEN 0 AND 100),
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    max_uses INT NOT NULL DEFAULT 100,
    times_used INT NOT NULL DEFAULT 0,
    CHECK (valid_to > valid_from)
);

-- 10. Tickets
CREATE TABLE Tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    schedule_id INT NOT NULL,
    customer_id INT NOT NULL,
    discount_id INT NULL,
    seat_number VARCHAR(10) NOT NULL,
    price DECIMAL(8,2) NOT NULL CHECK (price >= 0),
    purchase_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('BOOKED','CANCELLED','USED') NOT NULL DEFAULT 'BOOKED',
    FOREIGN KEY (schedule_id) REFERENCES ShowSchedules(schedule_id),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (discount_id) REFERENCES DiscountCodes(discount_id),
    UNIQUE (schedule_id, seat_number)
);

-- 11. Payments
CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT NOT NULL,
    amount DECIMAL(8,2) NOT NULL CHECK (amount >= 0),
    payment_method ENUM('CASH','CARD','ONLINE') NOT NULL,
    payment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUCCESS','FAILED','REFUNDED') NOT NULL DEFAULT 'SUCCESS',
    FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id)
);

-- 12. Concessions
CREATE TABLE Concessions (
    concession_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category ENUM('FOOD','MERCHANDISE') NOT NULL,
    price DECIMAL(8,2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0)
);

-- 13. Equipment
CREATE TABLE Equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    condition_status ENUM('GOOD','NEEDS_REPAIR','RETIRED') NOT NULL DEFAULT 'GOOD',
    purchase_date DATE
);

-- 14. Inventory
CREATE TABLE Inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    show_id INT NOT NULL,
    quantity_allocated INT NOT NULL CHECK (quantity_allocated > 0),
    allocation_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    return_date DATE,
    FOREIGN KEY (equipment_id) REFERENCES Equipment(equipment_id),
    FOREIGN KEY (show_id) REFERENCES Shows(show_id),
    CHECK (return_date IS NULL OR return_date >= allocation_date)
);

-- 15. MaintenanceRecords
CREATE TABLE MaintenanceRecords (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    performed_by INT NOT NULL,
    maintenance_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    description TEXT,
    cost DECIMAL(8,2) CHECK (cost >= 0),
    FOREIGN KEY (equipment_id) REFERENCES Equipment(equipment_id),
    FOREIGN KEY (performed_by) REFERENCES Employees(employee_id)
);

-- 16. Feedbacks
CREATE TABLE Feedbacks (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    show_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    feedback_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (show_id) REFERENCES Shows(show_id)
);

-- 17. AuditLogs
CREATE TABLE AuditLogs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    action_type ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    performed_by INT NULL,
    action_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    FOREIGN KEY (performed_by) REFERENCES Employees(employee_id)
);

-- 18. Notifications
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    type ENUM('EMAIL','SMS') NOT NULL,
    message TEXT NOT NULL,
    sent_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SENT','FAILED','PENDING') NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);
-- Constraint count check:PRIMARY KEY, FOREIGN KEY (×16), UNIQUE (×7), NOT NULL (everywhere), CHECK (×15+), and DEFAULT (×12+) 


-- TRIGGERS: 
DELIMITER $$

-- 1. Block booking if no seats left
CREATE TRIGGER trg_CheckSeatAvailability
BEFORE INSERT ON Tickets
FOR EACH ROW
BEGIN
    DECLARE v_available INT;
    SELECT available_seats INTO v_available FROM ShowSchedules WHERE schedule_id = NEW.schedule_id;
    IF v_available <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No seats available for this schedule.';
    END IF;
END$$

-- 2. Decrement seat count automatically after booking
CREATE TRIGGER trg_UpdateSeatsAfterBooking
AFTER INSERT ON Tickets
FOR EACH ROW
BEGIN
    UPDATE ShowSchedules
    SET available_seats = available_seats - 1
    WHERE schedule_id = NEW.schedule_id;
END$$

-- 3. Auto-log employee record changes into AuditLogs
CREATE TRIGGER trg_LogEmployeeAudit
AFTER UPDATE ON Employees
FOR EACH ROW
BEGIN
    INSERT INTO AuditLogs (table_name, action_type, performed_by, details)
    VALUES ('Employees', 'UPDATE', NEW.employee_id,
            CONCAT('Employee ', NEW.full_name, ' record updated.'));
END$$

DELIMITER ;

-- STORED PROCEDURES:
DELIMITER $$

-- 1. Book a ticket
CREATE PROCEDURE sp_BookTicket(
    IN p_schedule_id INT, IN p_customer_id INT, IN p_discount_id INT,
    IN p_seat_number VARCHAR(10), IN p_price DECIMAL(8,2)
)
BEGIN
    INSERT INTO Tickets (schedule_id, customer_id, discount_id, seat_number, price)
    VALUES (p_schedule_id, p_customer_id, p_discount_id, p_seat_number, p_price);
END$$

-- 2. Cancel a ticket (and restore the seat)
CREATE PROCEDURE sp_CancelTicket(IN p_ticket_id INT)
BEGIN
    DECLARE v_schedule_id INT;
    SELECT schedule_id INTO v_schedule_id FROM Tickets WHERE ticket_id = p_ticket_id;

    UPDATE Tickets SET status = 'CANCELLED' WHERE ticket_id = p_ticket_id;
    UPDATE ShowSchedules SET available_seats = available_seats + 1 WHERE schedule_id = v_schedule_id;
END$$

-- 3. Show revenue report
CREATE PROCEDURE sp_GetShowRevenue(IN p_show_id INT)
BEGIN
    SELECT s.title, COUNT(t.ticket_id) AS tickets_sold, SUM(p.amount) AS total_revenue
    FROM Shows s
    JOIN ShowSchedules ss ON s.show_id = ss.show_id
    JOIN Tickets t ON ss.schedule_id = t.schedule_id
    JOIN Payments p ON t.ticket_id = p.ticket_id
    WHERE s.show_id = p_show_id AND p.status = 'SUCCESS'
    GROUP BY s.show_id, s.title;
END$$

DELIMITER ;

-- VIEWS: 
CREATE VIEW vw_UpcomingShows AS
SELECT ss.schedule_id, s.title, v.venue_name, ss.show_date, ss.start_time, ss.available_seats
FROM ShowSchedules ss
JOIN Shows s ON ss.show_id = s.show_id
JOIN Venues v ON ss.venue_id = v.venue_id
WHERE ss.show_date >= CURRENT_DATE AND ss.status = 'SCHEDULED';

CREATE VIEW vw_TicketSalesSummary AS
SELECT ss.schedule_id, s.title, COUNT(t.ticket_id) AS tickets_sold, SUM(t.price) AS gross_sales
FROM ShowSchedules ss
JOIN Shows s ON ss.show_id = s.show_id
LEFT JOIN Tickets t ON ss.schedule_id = t.schedule_id AND t.status != 'CANCELLED'
GROUP BY ss.schedule_id, s.title;

CREATE VIEW vw_EmployeeRoster AS
SELECT e.employee_id, e.full_name, r.role_name, e.status
FROM Employees e
JOIN Roles r ON e.role_id = r.role_id;

CREATE VIEW vw_PerformerLineup AS
SELECT sp.show_id, s.title, p.stage_name, sp.role_in_show
FROM ShowPerformers sp
JOIN Performers p ON sp.performer_id = p.performer_id
JOIN Shows s ON sp.show_id = s.show_id;

CREATE VIEW vw_CustomerFeedbackOverview AS
SELECT f.feedback_id, c.full_name, s.title, f.rating, f.feedback_date
FROM Feedbacks f
JOIN Customers c ON f.customer_id = c.customer_id
JOIN Shows s ON f.show_id = s.show_id;

CREATE VIEW vw_EquipmentMaintenanceHistory AS
SELECT m.maintenance_id, e.equipment_name, m.maintenance_date, m.cost, emp.full_name AS performed_by
FROM MaintenanceRecords m
JOIN Equipment e ON m.equipment_id = e.equipment_id
JOIN Employees emp ON m.performed_by = emp.employee_id;

CREATE VIEW vw_RevenueByVenue AS
SELECT v.venue_name, SUM(p.amount) AS total_revenue
FROM Venues v
JOIN ShowSchedules ss ON v.venue_id = ss.venue_id
JOIN Tickets t ON ss.schedule_id = t.schedule_id
JOIN Payments p ON t.ticket_id = p.ticket_id
WHERE p.status = 'SUCCESS'
GROUP BY v.venue_id, v.venue_name;


-- Data entry: 
-- 1. Roles
INSERT INTO Roles (role_name, description) VALUES
('Ringmaster','Hosts and oversees the show'),
('Performer','Performs in shows'),
('Ticket Agent','Sells and manages tickets'),
('Maintenance Technician','Maintains equipment'),
('Manager','Oversees daily operations');

-- 2. Employees (cnic & email must be unique, salary must be > 0)
INSERT INTO Employees (full_name, cnic, email, phone, role_id, hire_date, salary, status) VALUES
('Ali Raza','35202-1234567-1','ali.raza@carnival.com','03001234567',1,'2023-01-15',85000.00,'ACTIVE'),
('Sara Khan','35202-2345678-2','sara.khan@carnival.com','03007654321',2,'2023-03-01',60000.00,'ACTIVE'),
('Bilal Ahmed','35202-3456789-3','bilal.ahmed@carnival.com','03009876543',2,'2023-03-10',58000.00,'ACTIVE'),
('Hina Tariq','35202-4567890-4','hina.tariq@carnival.com','03001112233',3,'2024-02-01',45000.00,'ACTIVE'),
('Usman Javed','35202-5678901-5','usman.javed@carnival.com','03002223344',4,'2024-05-20',50000.00,'ACTIVE'),
('Maria Iqbal','35202-6789012-6','maria.iqbal@carnival.com','03003334455',5,'2022-11-11',95000.00,'ACTIVE');

-- 3. Performers (employee_id must already exist and be unique — Sara=2, Bilal=3 are performers by role)
INSERT INTO Performers (employee_id, stage_name, specialty, experience_years) VALUES
(2,'Sara the Flame','Fire Breathing',5),
(3,'Bilal the Flyer','Trapeze Artist',7);

-- 4. Venues
INSERT INTO Venues (venue_name, location, capacity, status) VALUES
('Big Top Tent','Main Fairground, Lahore',500,'AVAILABLE'),
('Side Stage','East Wing, Lahore',150,'AVAILABLE'),
('Family Arena','West Wing, Lahore',300,'AVAILABLE');

-- 5. Shows
INSERT INTO Shows (title, genre, duration_minutes, description, status) VALUES
('Fire & Flight','Circus Acrobatics',60,'High-energy fire performance and aerial acrobatics.','ACTIVE'),
('Clown Carnival','Comedy',45,'Family-friendly comedy and clown acts.','ACTIVE'),
('Trapeze Dreams','Aerial',50,'Daring trapeze performances under the big top.','ACTIVE');

-- 6. ShowPerformers (junction table)
INSERT INTO ShowPerformers (show_id, performer_id, role_in_show) VALUES
(1,1,'Lead Fire Performer'),
(1,2,'Aerial Support'),
(3,2,'Lead Trapeze Artist');

-- 7. ShowSchedules (CHECK: end_time > start_time | UNIQUE: venue_id+date+start_time)
INSERT INTO ShowSchedules (show_id, venue_id, show_date, start_time, end_time, total_seats, available_seats, status) VALUES
(1,1,'2026-06-28','19:00:00','20:00:00',500,500,'SCHEDULED'),
(2,2,'2026-06-28','17:00:00','17:45:00',150,150,'SCHEDULED'),
(3,1,'2026-06-29','19:00:00','19:50:00',500,500,'SCHEDULED');

-- 8. Customers (email must be unique)
INSERT INTO Customers (full_name, email, phone, registration_date) VALUES
('Ahmed Siddiqui','ahmed.s@example.com','03101112233','2026-06-01'),
('Fatima Noor','fatima.noor@example.com','03112223344','2026-06-05'),
('Zain Malik','zain.malik@example.com','03123334455','2026-06-10');

-- 9. DiscountCodes (CHECK: 0–100 percent, valid_to > valid_from)
INSERT INTO DiscountCodes (code, discount_percent, valid_from, valid_to, max_uses, times_used) VALUES
('SUMMER10',10.00,'2026-06-01','2026-08-31',200,0),
('FAMILY20',20.00,'2026-06-15','2026-07-15',100,0);

-- 10. Tickets (UNIQUE: schedule_id+seat_number, discount_id is nullable)
INSERT INTO Tickets (schedule_id, customer_id, discount_id, seat_number, price) VALUES
(1, 1, 1, 'A12', 4500.00),
(1, 2, NULL, 'A13', 5000.00),
(2, 3, 2, 'B05', 2000.00);

-- After these 3 inserts: schedule 1's available_seats drops from 500 → 498,
-- schedule 2's drops from 150 → 149. Verify with:
SELECT schedule_id, total_seats, available_seats FROM ShowSchedules;
-- 11. Payments (ticket_id 1,2,3 assuming a fresh table — adjust if your IDs differ)
INSERT INTO Payments (ticket_id, amount, payment_method, status) VALUES
(1, 4500.00, 'CARD', 'SUCCESS'),
(2, 5000.00, 'CASH', 'SUCCESS'),
(3, 2000.00, 'ONLINE', 'SUCCESS');
-- 12. Concessions
INSERT INTO Concessions (item_name, category, price, stock_quantity) VALUES
('Popcorn','FOOD',300.00,200),
('Cotton Candy','FOOD',250.00,150),
('Carnival T-Shirt','MERCHANDISE',1200.00,80);

-- 13. Equipment
INSERT INTO Equipment (equipment_name, category, condition_status, purchase_date) VALUES
('Trapeze Rig','Aerial Equipment','GOOD','2022-05-01'),
('Fire Torches (Set of 6)','Fire Equipment','GOOD','2023-01-10'),
('Sound System','AV Equipment','NEEDS_REPAIR','2021-08-15');

-- 14. Inventory (CHECK: quantity_allocated > 0, return_date >= allocation_date or NULL)
INSERT INTO Inventory (equipment_id, show_id, quantity_allocated, allocation_date, return_date) VALUES
(1, 3, 1, '2026-06-29', NULL),
(2, 1, 1, '2026-06-28', NULL),
(3, 2, 1, '2026-06-28', '2026-06-28');

-- 15. MaintenanceRecords (performed_by must reference an Employee — Usman is the technician)
INSERT INTO MaintenanceRecords (equipment_id, performed_by, maintenance_date, description, cost) VALUES
(3, 5, '2026-06-20', 'Replaced faulty speaker wiring', 3500.00),
(1, 5, '2026-05-15', 'Routine safety inspection of rigging', 1000.00);

-- 16. Feedbacks (CHECK: rating between 1 and 5)
INSERT INTO Feedbacks (customer_id, show_id, rating, comments) VALUES
(1, 1, 5, 'Amazing fire performance, loved it!'),
(2, 2, 4, 'Kids enjoyed the clown act a lot.'),
(3, 1, 5, 'Best circus show I have seen.');

-- 17. Notifications
INSERT INTO Notifications (customer_id, type, message, status) VALUES
(1, 'EMAIL', 'Your ticket for Fire & Flight is confirmed!', 'SENT'),
(2, 'SMS', 'Reminder: Clown Carnival starts at 5 PM today.', 'SENT'),
(3, 'EMAIL', 'Thanks for your feedback on Trapeze Dreams!', 'PENDING');
-- 18. AuditLogs (manual seed entry)
INSERT INTO AuditLogs (table_name, action_type, performed_by, details) VALUES
('Venues', 'INSERT', 6, 'Added 3 new venues during initial setup');

-- Now actually fire trg_LogEmployeeAudit:
UPDATE Employees SET salary = 90000.00 WHERE employee_id = 1;

-- Check it logged itself automatically:
SELECT * FROM AuditLogs ORDER BY log_id DESC;