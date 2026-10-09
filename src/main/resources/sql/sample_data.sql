-- ============================================================================
-- Hospital Management System - Sample Demonstration Data
-- ============================================================================

USE hospital_management;

-- ----------------------------------------------------------------------------
-- 1. Roles
-- ----------------------------------------------------------------------------
INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'ADMIN', 'System Administrator with full hospital management privileges'),
(2, 'DOCTOR', 'Medical Doctor with clinical, diagnostic and prescription access'),
(3, 'RECEPTIONIST', 'Front-desk staff for patient intake and appointment booking'),
(4, 'PHARMACIST', 'Pharmacy staff for inventory and prescription dispensing'),
(5, 'LAB_STAFF', 'Laboratory technician for lab test orders and result entry'),
(6, 'BILLING_STAFF', 'Accounts and billing staff for invoices and payment processing')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ----------------------------------------------------------------------------
-- 2. Users (Passwords: admin123, doctor123, recep123, pharm123, lab123, bill123)
-- ----------------------------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, full_name, email, phone, role_id, is_active) VALUES
(1, 'admin', '$2a$12$Y3zMgVnwEvzePx8GbK1/Luvei.EjjXJwccZx2wiu4vTFMMsihJay.', 'Dr. Alexander Vance (Administrator)', 'admin@apexcarehospital.com', '+1-555-0100', 1, TRUE),
(2, 'dr.smith', '$2a$12$KJ7AScwsyUFFH5pX8ToQWOWIQx.ou5QsfFYv6diXEKcPXLDfMq8sy', 'Dr. Evelyn Smith, MD', 'evelyn.smith@apexcarehospital.com', '+1-555-0101', 2, TRUE),
(3, 'dr.jones', '$2a$12$KJ7AScwsyUFFH5pX8ToQWOWIQx.ou5QsfFYv6diXEKcPXLDfMq8sy', 'Dr. Marcus Jones, MD', 'marcus.jones@apexcarehospital.com', '+1-555-0102', 2, TRUE),
(4, 'receptionist1', '$2a$12$ws2qSxCHobjU/lwNizEy7ea1mAN6oL/Eako3bSETMOZKsmbtGmpLW', 'Sarah Jenkins', 'sarah.reception@apexcarehospital.com', '+1-555-0103', 3, TRUE),
(5, 'pharmacist1', '$2a$12$R7zgzSaxo.TLf29nh7h1hO2JrIp.vVDM495LQyB.BY9tmP7WEBRlK', 'David Miller, PharmD', 'david.pharmacy@apexcarehospital.com', '+1-555-0104', 4, TRUE),
(6, 'labstaff1', '$2a$12$NTHhjN6sSq3PkGTrSOfRUOa344TrxLzioZwE9azze9TxNN0/F5NZO', 'Rachel Adams', 'rachel.lab@apexcarehospital.com', '+1-555-0105', 5, TRUE),
(7, 'billing1', '$2a$12$O2pOynkGVVdRaV4Wk28F0.pMrQBP1.LH8F5EKurB1GOAJmUGPu/pW', 'Michael Scott', 'michael.billing@apexcarehospital.com', '+1-555-0106', 6, TRUE)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- ----------------------------------------------------------------------------
-- 3. Departments
-- ----------------------------------------------------------------------------
INSERT INTO departments (department_id, name, description, is_active) VALUES
(1, 'Cardiology', 'Heart and cardiovascular care, diagnostics and interventions', TRUE),
(2, 'Pediatrics', 'Comprehensive healthcare for infants, children, and adolescents', TRUE),
(3, 'Orthopedics', 'Musculoskeletal system, joints, bones, and trauma management', TRUE),
(4, 'Neurology', 'Diagnosis and treatment of neurological and spinal conditions', TRUE),
(5, 'General Medicine', 'Primary adult healthcare, general checkups and preventative medicine', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ----------------------------------------------------------------------------
-- 4. Doctors
-- ----------------------------------------------------------------------------
INSERT INTO doctors (doctor_id, employee_id, user_id, full_name, department_id, specialization, qualification, contact_number, email, consultation_fee, status) VALUES
(1, 'DOC-001', 2, 'Dr. Evelyn Smith', 1, 'Interventional Cardiology', 'MD, FACC, Harvard Medical School', '+1-555-0101', 'evelyn.smith@apexcarehospital.com', 120.00, 'ACTIVE'),
(2, 'DOC-002', 3, 'Dr. Marcus Jones', 2, 'Pediatric Pulmonology', 'MD, FAAP, Johns Hopkins', '+1-555-0102', 'marcus.jones@apexcarehospital.com', 95.00, 'ACTIVE'),
(3, 'DOC-003', NULL, 'Dr. Priya Sharma', 3, 'Orthopedic Surgery & Sports Medicine', 'MS Ortho, MCh, Oxford University', '+1-555-0107', 'priya.sharma@apexcarehospital.com', 110.00, 'ACTIVE'),
(4, 'DOC-004', NULL, 'Dr. Robert Chen', 5, 'Internal Medicine', 'MD, Internal Medicine, Stanford University', '+1-555-0108', 'robert.chen@apexcarehospital.com', 80.00, 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- ----------------------------------------------------------------------------
-- 5. Staff
-- ----------------------------------------------------------------------------
INSERT INTO staff (staff_id, employee_id, user_id, full_name, department_id, role_title, contact_number, email, status, hire_date) VALUES
(1, 'STF-001', 4, 'Sarah Jenkins', 5, 'Chief Receptionist', '+1-555-0103', 'sarah.reception@apexcarehospital.com', 'ACTIVE', '2023-01-15'),
(2, 'STF-002', 5, 'David Miller', NULL, 'Lead Pharmacist', '+1-555-0104', 'david.pharmacy@apexcarehospital.com', 'ACTIVE', '2022-06-10'),
(3, 'STF-003', 6, 'Rachel Adams', NULL, 'Senior Lab Technologist', '+1-555-0105', 'rachel.lab@apexcarehospital.com', 'ACTIVE', '2023-03-20'),
(4, 'STF-004', 7, 'Michael Scott', NULL, 'Billing Coordinator', '+1-555-0106', 'michael.billing@apexcarehospital.com', 'ACTIVE', '2022-11-01')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- ----------------------------------------------------------------------------
-- 6. Doctor Schedules
-- ----------------------------------------------------------------------------
INSERT INTO doctor_schedules (schedule_id, doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, max_patients, is_active) VALUES
(1, 1, 'MONDAY', '09:00:00', '13:00:00', 30, 8, TRUE),
(2, 1, 'WEDNESDAY', '09:00:00', '13:00:00', 30, 8, TRUE),
(3, 1, 'FRIDAY', '14:00:00', '18:00:00', 30, 8, TRUE),
(4, 2, 'TUESDAY', '10:00:00', '16:00:00', 20, 15, TRUE),
(5, 2, 'THURSDAY', '10:00:00', '16:00:00', 20, 15, TRUE),
(6, 4, 'MONDAY', '08:00:00', '16:00:00', 20, 20, TRUE)
ON DUPLICATE KEY UPDATE is_active = VALUES(is_active);

-- ----------------------------------------------------------------------------
-- 7. Patients
-- ----------------------------------------------------------------------------
INSERT INTO patients (patient_id, patient_code, first_name, last_name, date_of_birth, gender, contact_number, email, address, emergency_contact_name, emergency_contact_phone, blood_group, allergies, medical_history, is_active) VALUES
(1, 'PAT-1001', 'John', 'Doe', '1985-04-12', 'Male', '+1-555-1111', 'john.doe@gmail.com', '124 Maple St, Springfield', 'Jane Doe (Wife)', '+1-555-1112', 'O+', 'Penicillin', 'Mild hypertension since 2021', TRUE),
(2, 'PAT-1002', 'Emily', 'Clark', '1992-09-24', 'Female', '+1-555-2222', 'emily.clark@outlook.com', '56 Elm Ave, Metropolis', 'Peter Clark (Brother)', '+1-555-2223', 'A+', 'None', 'Asthma triggered by pollen', TRUE),
(3, 'PAT-1003', 'Robert', 'Taylor', '1968-11-03', 'Male', '+1-555-3333', 'robert.taylor@yahoo.com', '89 Pine Rd, Gotham', 'Mary Taylor (Daughter)', '+1-555-3334', 'B-', 'Sulfa drugs', 'Type 2 Diabetes, Coronary Stent in 2019', TRUE),
(4, 'PAT-1004', 'Sophia', 'Martinez', '2015-06-18', 'Female', '+1-555-4444', 'smartinez@parent.org', '32 Oak Blvd, Star City', 'Elena Martinez (Mother)', '+1-555-4445', 'AB+', 'Peanuts', 'Childhood eczema', TRUE),
(5, 'PAT-1005', 'William', 'Anderson', '1955-02-14', 'Male', '+1-555-5555', 'w.anderson@provider.net', '710 Birch Lane, Central City', 'Laura Anderson (Daughter)', '+1-555-5556', 'O-', 'NSAIDs (Aspirin)', 'Chronic kidney disease Stage 2', TRUE)
ON DUPLICATE KEY UPDATE first_name = VALUES(first_name);

-- ----------------------------------------------------------------------------
-- 8. Wards & Beds
-- ----------------------------------------------------------------------------
INSERT INTO wards (ward_id, ward_name, ward_type, total_beds, daily_rate, description) VALUES
(1, 'General Medical Ward A', 'General', 6, 120.00, 'Shared acute care medical recovery ward with continuous nurse monitoring'),
(2, 'Cardiac Intensive Care (ICU)', 'ICU', 4, 450.00, 'Critical care monitoring with advanced cardiac telemetry and ventilators'),
(3, 'Pediatric Ward B', 'Pediatric', 4, 180.00, 'Child-friendly recovery rooms with parental bedside accommodation'),
(4, 'Deluxe Private Suite 101', 'Private', 2, 350.00, 'Private patient room with en-suite amenities, TV and visitor seating')
ON DUPLICATE KEY UPDATE daily_rate = VALUES(daily_rate);

INSERT INTO beds (bed_id, ward_id, bed_number, is_occupied, is_active) VALUES
(1, 1, 'G-101', TRUE, TRUE),
(2, 1, 'G-102', FALSE, TRUE),
(3, 1, 'G-103', FALSE, TRUE),
(4, 1, 'G-104', FALSE, TRUE),
(5, 1, 'G-105', FALSE, TRUE),
(6, 1, 'G-106', FALSE, TRUE),
(7, 2, 'ICU-01', TRUE, TRUE),
(8, 2, 'ICU-02', FALSE, TRUE),
(9, 2, 'ICU-03', FALSE, TRUE),
(10, 2, 'ICU-04', FALSE, TRUE),
(11, 3, 'PED-01', FALSE, TRUE),
(12, 3, 'PED-02', FALSE, TRUE),
(13, 4, 'PVT-01', FALSE, TRUE),
(14, 4, 'PVT-02', FALSE, TRUE)
ON DUPLICATE KEY UPDATE bed_number = VALUES(bed_number);

-- ----------------------------------------------------------------------------
-- 9. Active Inpatient Admissions
-- ----------------------------------------------------------------------------
INSERT INTO admissions (admission_id, admission_number, patient_id, bed_id, attending_doctor_id, admission_date, diagnosis, status, daily_rate) VALUES
(1, 'ADM-2026-001', 3, 7, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Unstable Angina, observation for acute coronary syndrome', 'ADMITTED', 450.00),
(2, 'ADM-2026-002', 1, 1, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 'Acute Bacterial Pneumonia with dehydration', 'ADMITTED', 120.00)
ON DUPLICATE KEY UPDATE diagnosis = VALUES(diagnosis);

-- ----------------------------------------------------------------------------
-- 10. Appointments
-- ----------------------------------------------------------------------------
INSERT INTO appointments (appointment_id, appointment_number, patient_id, doctor_id, appointment_date, appointment_time, end_time, status, reason, notes) VALUES
(1, 'APT-2026-101', 1, 1, CURDATE(), '09:00:00', '09:30:00', 'SCHEDULED', 'Routine cardiac follow-up and ECG check', 'Patient reported mild palpitations'),
(2, 'APT-2026-102', 2, 4, CURDATE(), '10:00:00', '10:20:00', 'CHECKED_IN', 'Persistent cough and respiratory wheezing', 'In waiting lounge'),
(3, 'APT-2026-103', 4, 2, CURDATE(), '11:00:00', '11:20:00', 'SCHEDULED', 'Childhood wellness exam and vaccination', 'Brought immunization booklet'),
(4, 'APT-2026-104', 5, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '10:30:00', 'SCHEDULED', 'Hypertension medication adjustment review', 'Recent BP reading 145/92')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ----------------------------------------------------------------------------
-- 11. Laboratory Tests Catalog
-- ----------------------------------------------------------------------------
INSERT INTO lab_tests (test_id, test_code, test_name, category, price, normal_range, unit, description, is_active) VALUES
(1, 'LAB-CBC', 'Complete Blood Count (CBC)', 'Hematology', 35.00, 'RBC: 4.5-5.9, WBC: 4.5-11.0', '10^3/uL', 'Full panel including RBC, WBC, Hemoglobin, Hematocrit and Platelet count', TRUE),
(2, 'LAB-LIPID', 'Comprehensive Lipid Profile', 'Biochemistry', 50.00, 'Chol: <200, HDL: >40, LDL: <100', 'mg/dL', 'Total Cholesterol, HDL, LDL, VLDL, and Triglycerides', TRUE),
(3, 'LAB-LFT', 'Liver Function Test (LFT)', 'Biochemistry', 60.00, 'ALT: 7-56, AST: 10-40, Bilirubin: 0.1-1.2', 'U/L', 'Enzymes, Bilirubin, and protein levels assessing hepatic health', TRUE),
(4, 'LAB-HBA1C', 'Glycated Hemoglobin (HbA1c)', 'Biochemistry', 40.00, 'Normal: <5.7%, Prediabetes: 5.7-6.4%', '%', 'Average blood sugar levels across the past 3 months', TRUE),
(5, 'LAB-CREAT', 'Serum Creatinine & eGFR', 'Biochemistry', 30.00, '0.7 - 1.3', 'mg/dL', 'Renal clearance biomarker and glomerular filtration rate', TRUE),
(6, 'LAB-CXR', 'Chest X-Ray (PA View)', 'Radiology', 75.00, 'Lungs clear, cardiac shadow normal', '', 'High resolution chest radiograph for lung and heart visualization', TRUE),
(7, 'LAB-ECG', 'Electrocardiogram (12-Lead ECG)', 'Cardiology', 45.00, 'Normal Sinus Rhythm, PR: 0.12-0.20s', '', '12-lead electrical conduction recording of the heart muscle', TRUE)
ON DUPLICATE KEY UPDATE price = VALUES(price);

-- ----------------------------------------------------------------------------
-- 12. Lab Orders & Results
-- ----------------------------------------------------------------------------
INSERT INTO lab_orders (order_id, order_number, patient_id, doctor_id, order_date, status, priority, clinical_notes) VALUES
(1, 'LBO-2026-001', 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'COMPLETED', 'URGENT', 'Chest discomfort evaluation; check cardiac enzymes and lipid profile'),
(2, 'LBO-2026-002', 1, 4, NOW(), 'REQUESTED', 'ROUTINE', 'Assess inflammatory markers and complete hemogram')
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO lab_results (result_id, order_id, test_id, result_value, reference_range, unit, is_abnormal, remarks, performed_by_user_id, result_date) VALUES
(1, 1, 2, '242.0', '< 200.0', 'mg/dL', TRUE, 'Elevated total cholesterol and LDL (158 mg/dL)', 6, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 1, 7, 'Sinus Tachycardia with ST depressions in V4-V6', 'Normal Sinus Rhythm', '', TRUE, 'Signs of myocardial strain, cardiology consult advised', 6, DATE_SUB(NOW(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE result_value = VALUES(result_value);

-- ----------------------------------------------------------------------------
-- 13. Medicines & Batches
-- ----------------------------------------------------------------------------
INSERT INTO medicines (medicine_id, code, name, generic_name, category, manufacturer, unit, purchase_price, selling_price, reorder_level, is_active) VALUES
(1, 'MED-AMOX-500', 'Amoxicillin 500mg', 'Amoxicillin', 'Antibiotic', 'Pfizer Inc.', 'Capsule', 0.40, 0.85, 50, TRUE),
(2, 'MED-PARA-500', 'Paracetamol 500mg', 'Acetaminophen', 'Antipyretic', 'GSK Healthcare', 'Tablet', 0.08, 0.25, 100, TRUE),
(3, 'MED-IBUP-400', 'Ibuprofen 400mg', 'Ibuprofen', 'Analgesic', 'Bayer Pharma', 'Tablet', 0.15, 0.45, 60, TRUE),
(4, 'MED-ATOR-20', 'Atorvastatin 20mg', 'Atorvastatin Calcium', 'Cardiovascular', 'Sun Pharma', 'Tablet', 0.60, 1.35, 40, TRUE),
(5, 'MED-METF-500', 'Metformin 500mg ER', 'Metformin Hydrochloride', 'Antidiabetic', 'Merck KGaA', 'Tablet', 0.20, 0.50, 80, TRUE),
(6, 'MED-OMEP-20', 'Omeprazole 20mg', 'Omeprazole', 'Gastrointestinal', 'AstraZeneca', 'Capsule', 0.30, 0.75, 40, TRUE),
(7, 'MED-AZIT-500', 'Azithromycin 500mg', 'Azithromycin', 'Antibiotic', 'Cipla Ltd', 'Tablet', 1.10, 2.40, 30, TRUE),
(8, 'MED-SALB-INH', 'Salbutamol Inhaler 100mcg', 'Albuterol Sulfate', 'Respiratory', 'Cipla Ltd', 'Inhaler', 4.50, 9.80, 15, TRUE)
ON DUPLICATE KEY UPDATE selling_price = VALUES(selling_price);

INSERT INTO medicine_batches (batch_id, medicine_id, batch_number, expiry_date, quantity, purchase_price, selling_price) VALUES
(1, 1, 'AMX-2026A', '2027-08-31', 120, 0.40, 0.85),
(2, 2, 'PAR-2026B', '2028-01-31', 250, 0.08, 0.25),
(3, 3, 'IBU-2025X', '2026-11-30', 85, 0.15, 0.45), -- Near expiry (useful for alert demo!)
(4, 4, 'ATO-2027C', '2027-12-31', 90, 0.60, 1.35),
(5, 5, 'MET-2027D', '2027-05-31', 140, 0.20, 0.50),
(6, 6, 'OME-2026F', '2027-03-31', 15, 0.30, 0.75),  -- Low stock (reorder level is 40, qty is 15!)
(7, 8, 'SAL-2027E', '2027-09-30', 8, 4.50, 9.80)     -- Low stock (reorder level is 15, qty is 8!)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- ----------------------------------------------------------------------------
-- 14. Medical Records & Prescriptions
-- ----------------------------------------------------------------------------
INSERT INTO medical_records (record_id, patient_id, doctor_id, visit_date, symptoms, clinical_observations, diagnosis, treatment_plan, follow_up_date) VALUES
(1, 3, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Substernal chest tightness radiating to left shoulder on moderate exertion, dyspnea', 'BP: 152/94 mmHg, Pulse: 88 bpm regular, S1/S2 heard, no murmurs', 'Acute Exacerbation of Coronary Artery Disease with Angina Pectoris', 'Admitted to Cardiac ICU; start dual antiplatelet, high-dose statin, cardiac telemetry', DATE_ADD(CURDATE(), INTERVAL 14 DAY))
ON DUPLICATE KEY UPDATE diagnosis = VALUES(diagnosis);

INSERT INTO prescriptions (prescription_id, prescription_number, patient_id, doctor_id, record_id, prescription_date, diagnosis, notes, is_dispensed) VALUES
(1, 'RX-2026-001', 3, 1, 1, CURDATE(), 'Coronary Artery Disease & Dyslipidemia', 'Take Atorvastatin daily at bedtime. Avoid strenuous exertion.', TRUE)
ON DUPLICATE KEY UPDATE diagnosis = VALUES(diagnosis);

INSERT INTO prescription_items (item_id, prescription_id, medicine_id, medicine_name, dosage, frequency, duration, quantity, instructions) VALUES
(1, 1, 4, 'Atorvastatin 20mg', '20mg', '0-0-1 at bedtime', '30 days', 30, 'Take with water after dinner'),
(2, 1, 6, 'Omeprazole 20mg', '20mg', '1-0-0 before breakfast', '14 days', 14, 'Take 30 mins before morning meal')
ON DUPLICATE KEY UPDATE dosage = VALUES(dosage);

-- ----------------------------------------------------------------------------
-- 15. Invoices & Payments
-- ----------------------------------------------------------------------------
INSERT INTO invoices (invoice_id, invoice_number, patient_id, admission_id, issue_date, subtotal, discount_percent, discount_amount, tax_percent, tax_amount, total_amount, paid_amount, balance_amount, payment_status, notes, created_by_user_id) VALUES
(1, 'INV-2026-001', 3, 1, CURDATE(), 620.00, 5.00, 31.00, 5.00, 29.45, 618.45, 300.00, 318.45, 'PARTIAL', 'ICU Stay (1 day) + Cardiology Consultation + Lab investigations', 7),
(2, 'INV-2026-002', 2, NULL, CURDATE(), 115.00, 0.00, 0.00, 5.00, 5.75, 120.75, 120.75, 0.00, 'PAID', 'Outpatient general medicine consultation + medication', 7)
ON DUPLICATE KEY UPDATE total_amount = VALUES(total_amount);

INSERT INTO invoice_items (item_id, invoice_id, item_type, description, quantity, unit_price, total_price) VALUES
(1, 1, 'ROOM_CHARGE', 'ICU Room & Bed Charge (1 Day)', 1, 450.00, 450.00),
(2, 1, 'CONSULTATION', 'Cardiology Specialist Consultation (Dr. Evelyn Smith)', 1, 120.00, 120.00),
(3, 1, 'LAB_TEST', 'Diagnostic Lab Profile (Lipid + ECG)', 1, 50.00, 50.00),
(4, 2, 'CONSULTATION', 'Internal Medicine Consultation (Dr. Robert Chen)', 1, 80.00, 80.00),
(5, 2, 'PHARMACY', 'Prescription Medication Dispense', 1, 35.00, 35.00)
ON DUPLICATE KEY UPDATE total_price = VALUES(total_price);

INSERT INTO payments (payment_id, invoice_id, payment_number, payment_date, amount, payment_method, transaction_ref, notes, received_by_user_id) VALUES
(1, 1, 'PAY-2026-001', NOW(), 300.00, 'CARD', 'TXN-9847291-VISA', 'Advance deposit at ICU admission', 7),
(2, 2, 'PAY-2026-002', NOW(), 120.75, 'UPI', 'UPI-REF-34827192', 'Full payment settled via mobile scan', 7)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

-- ----------------------------------------------------------------------------
-- 16. Audit Logs
-- ----------------------------------------------------------------------------
INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, ip_address) VALUES
(1, 'admin', 'SYSTEM_INIT', 'DATABASE', 'ALL', 'Hospital Management System initialized with demonstration dataset', '127.0.0.1'),
(4, 'receptionist1', 'CREATE', 'PATIENT', 'PAT-1001', 'Registered patient John Doe', '192.168.1.10'),
(2, 'dr.smith', 'CREATE', 'ADMISSION', 'ADM-2026-001', 'Admitted patient Robert Taylor to Cardiac ICU Bed 7', '192.168.1.15'),
(7, 'billing1', 'CREATE', 'PAYMENT', 'PAY-2026-001', 'Processed advance payment of $300.00 for INV-2026-001', '192.168.1.20');
