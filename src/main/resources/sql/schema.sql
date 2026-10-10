-- ============================================================================
-- Hospital Management System - Database Schema
-- Target Database: MySQL 8.0+ / H2 (MySQL Mode compatible)
-- Database Name: hospital_management
-- ============================================================================

CREATE DATABASE IF NOT EXISTS hospital_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_management;

-- ----------------------------------------------------------------------------
-- 1. Roles Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. Users Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. Departments Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. Doctors Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(30) NOT NULL UNIQUE,
    user_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 50.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. Staff Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(30) NOT NULL UNIQUE,
    user_id INT UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    department_id INT,
    role_title VARCHAR(50) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    hire_date DATE NOT NULL,
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_staff_department FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 6. Doctor Schedules Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS doctor_schedules (
    schedule_id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 20,
    max_patients INT NOT NULL DEFAULT 20,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 7. Patients Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_code VARCHAR(30) NOT NULL UNIQUE,
    user_id INT UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    blood_group VARCHAR(10),
    allergies TEXT,
    medical_history TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_patient_name (first_name, last_name),
    INDEX idx_patient_phone (contact_number)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 8. Appointments Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW
    reason VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT,
    INDEX idx_appt_date_doctor (doctor_id, appointment_date, appointment_time)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 9. Wards Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wards (
    ward_id INT AUTO_INCREMENT PRIMARY KEY,
    ward_name VARCHAR(50) NOT NULL UNIQUE,
    ward_type VARCHAR(30) NOT NULL, -- General, ICU, Pediatric, Maternity, Emergency, Private
    total_beds INT NOT NULL DEFAULT 0,
    daily_rate DECIMAL(10,2) NOT NULL DEFAULT 100.00,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 10. Beds Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS beds (
    bed_id INT AUTO_INCREMENT PRIMARY KEY,
    ward_id INT NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    is_occupied BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_ward_bed UNIQUE (ward_id, bed_number),
    CONSTRAINT fk_bed_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 11. Admissions Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admissions (
    admission_id INT AUTO_INCREMENT PRIMARY KEY,
    admission_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    bed_id INT NOT NULL,
    attending_doctor_id INT NOT NULL,
    admission_date DATETIME NOT NULL,
    discharge_date DATETIME NULL,
    diagnosis TEXT NOT NULL,
    discharge_summary TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ADMITTED', -- ADMITTED, DISCHARGED
    daily_rate DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_adm_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_adm_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE RESTRICT,
    CONSTRAINT fk_adm_doctor FOREIGN KEY (attending_doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 12. Medical Records Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medical_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    visit_date DATETIME NOT NULL,
    symptoms TEXT,
    clinical_observations TEXT,
    diagnosis TEXT NOT NULL,
    treatment_plan TEXT NOT NULL,
    follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_med_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_med_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 13. Prescriptions Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    record_id INT NULL,
    prescription_date DATE NOT NULL,
    diagnosis VARCHAR(255),
    notes TEXT,
    is_dispensed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rx_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rx_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT,
    CONSTRAINT fk_rx_record FOREIGN KEY (record_id) REFERENCES medical_records(record_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 14. Prescription Items Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prescription_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT NULL,
    medicine_id INT NULL,
    medicine_name VARCHAR(100) NOT NULL,
    dosage VARCHAR(50) NOT NULL,     -- e.g. 500mg
    frequency VARCHAR(50) NOT NULL,  -- e.g. 1-0-1 after food
    duration VARCHAR(50) NOT NULL,   -- e.g. 5 days
    quantity INT NOT NULL DEFAULT 1,
    instructions VARCHAR(255),
    CONSTRAINT fk_rxi_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 15. Medicines Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    category VARCHAR(50) NOT NULL, -- Antibiotic, Analgesic, Antipyretic, Antidiabetic, etc.
    manufacturer VARCHAR(100),
    unit VARCHAR(20) NOT NULL DEFAULT 'Tablet', -- Tablet, Capsule, Syrup, Injection, Ointment
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 20,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 16. Medicine Batches Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicine_batches (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_med_batch UNIQUE (medicine_id, batch_number),
    CONSTRAINT fk_batch_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 17. Inventory Transactions Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    batch_id INT NOT NULL,
    medicine_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- PURCHASE, DISPENSED, ADJUSTMENT, RETURN
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    reference_number VARCHAR(50),
    notes VARCHAR(255),
    performed_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_batch FOREIGN KEY (batch_id) REFERENCES medicine_batches(batch_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_med FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_user FOREIGN KEY (performed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 18. Laboratory Tests Catalog Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lab_tests (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    test_code VARCHAR(30) NOT NULL UNIQUE,
    test_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL, -- Hematology, Biochemistry, Microbiology, Radiology, Pathology
    price DECIMAL(10,2) NOT NULL,
    normal_range VARCHAR(100),
    unit VARCHAR(30),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 19. Laboratory Orders Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lab_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    order_date DATETIME NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'REQUESTED', -- REQUESTED, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED
    priority VARCHAR(15) NOT NULL DEFAULT 'ROUTINE', -- ROUTINE, URGENT, STAT
    clinical_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lo_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_lo_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 20. Laboratory Results Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lab_results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    test_id INT NOT NULL,
    result_value VARCHAR(100) NOT NULL,
    reference_range VARCHAR(100),
    unit VARCHAR(30),
    is_abnormal BOOLEAN NOT NULL DEFAULT FALSE,
    remarks TEXT,
    performed_by_user_id INT,
    result_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lr_order FOREIGN KEY (order_id) REFERENCES lab_orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_lr_test FOREIGN KEY (test_id) REFERENCES lab_tests(test_id) ON DELETE RESTRICT,
    CONSTRAINT fk_lr_user FOREIGN KEY (performed_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 21. Invoices Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    admission_id INT NULL,
    issue_date DATE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    balance_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID', -- UNPAID, PARTIAL, PAID, REFUNDED
    notes TEXT,
    created_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_admission FOREIGN KEY (admission_id) REFERENCES admissions(admission_id) ON DELETE SET NULL,
    CONSTRAINT fk_inv_creator FOREIGN KEY (created_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 22. Invoice Items Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    item_type VARCHAR(30) NOT NULL, -- CONSULTATION, ROOM_CHARGE, LAB_TEST, PHARMACY, PROCEDURE, MISC
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_ii_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 23. Payments Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    payment_number VARCHAR(30) NOT NULL UNIQUE,
    payment_date DATETIME NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL, -- CASH, CARD, UPI, BANK_TRANSFER, INSURANCE
    transaction_ref VARCHAR(100),
    notes VARCHAR(255),
    received_by_user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE RESTRICT,
    CONSTRAINT fk_pay_receiver FOREIGN KEY (received_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 24. Audit Logs Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_time (created_at),
    INDEX idx_audit_user (username)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 25. Patient Feedback Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient_feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    rating INT NOT NULL, -- 1 to 5 stars
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fb_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE CASCADE,
    CONSTRAINT fk_fb_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_fb_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    INDEX idx_fb_doctor (doctor_id),
    INDEX idx_fb_patient (patient_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 26. System Settings Table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

