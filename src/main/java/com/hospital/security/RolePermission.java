package com.hospital.security;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration of system roles and their associated permissions.
 */
public class RolePermission {

    public enum RoleType {
        ADMIN,
        DOCTOR,
        RECEPTIONIST,
        PHARMACIST,
        LAB_STAFF,
        BILLING_STAFF;

        public static RoleType fromString(String roleName) {
            if (roleName == null) return null;
            String normalized = roleName.trim().toUpperCase().replace(" ", "_");
            for (RoleType r : values()) {
                if (r.name().equals(normalized)) {
                    return r;
                }
            }
            return null;
        }
    }

    public enum Permission {
        // User & Staff Management
        MANAGE_USERS,
        MANAGE_STAFF,
        MANAGE_DOCTORS,
        MANAGE_DEPARTMENTS,
        VIEW_AUDIT_LOGS,
        VIEW_SYSTEM_REPORTS,

        // Patient Management
        REGISTER_PATIENT,
        UPDATE_PATIENT,
        VIEW_PATIENT,
        DEACTIVATE_PATIENT,

        // Appointment Management
        BOOK_APPOINTMENT,
        UPDATE_APPOINTMENT,
        CANCEL_APPOINTMENT,
        VIEW_APPOINTMENTS,

        // Inpatient & Ward Management
        MANAGE_ADMISSIONS,
        VIEW_ADMISSIONS,
        MANAGE_WARDS_BEDS,

        // Clinical / Medical Records
        CREATE_MEDICAL_RECORD,
        VIEW_MEDICAL_RECORD,
        CREATE_PRESCRIPTION,
        VIEW_PRESCRIPTION,

        // Laboratory Management
        REQUEST_LAB_TEST,
        ENTER_LAB_RESULT,
        VIEW_LAB_RESULTS,
        MANAGE_LAB_CATALOG,

        // Pharmacy & Inventory
        MANAGE_MEDICINES,
        MANAGE_INVENTORY,
        DISPENSE_MEDICINE,
        VIEW_PHARMACY,

        // Billing & Invoicing
        CREATE_INVOICE,
        COLLECT_PAYMENT,
        VIEW_INVOICES,
        APPLY_DISCOUNT
    }

    private static final Map<RoleType, Set<Permission>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // ADMIN has all permissions
        ROLE_PERMISSIONS.put(RoleType.ADMIN, EnumSet.allOf(Permission.class));

        // DOCTOR permissions
        ROLE_PERMISSIONS.put(RoleType.DOCTOR, EnumSet.of(
                Permission.VIEW_PATIENT,
                Permission.VIEW_APPOINTMENTS,
                Permission.UPDATE_APPOINTMENT,
                Permission.CREATE_MEDICAL_RECORD,
                Permission.VIEW_MEDICAL_RECORD,
                Permission.CREATE_PRESCRIPTION,
                Permission.VIEW_PRESCRIPTION,
                Permission.REQUEST_LAB_TEST,
                Permission.VIEW_LAB_RESULTS,
                Permission.VIEW_ADMISSIONS
        ));

        // RECEPTIONIST permissions
        ROLE_PERMISSIONS.put(RoleType.RECEPTIONIST, EnumSet.of(
                Permission.REGISTER_PATIENT,
                Permission.UPDATE_PATIENT,
                Permission.VIEW_PATIENT,
                Permission.BOOK_APPOINTMENT,
                Permission.UPDATE_APPOINTMENT,
                Permission.CANCEL_APPOINTMENT,
                Permission.VIEW_APPOINTMENTS,
                Permission.VIEW_ADMISSIONS,
                Permission.VIEW_INVOICES
        ));

        // PHARMACIST permissions
        ROLE_PERMISSIONS.put(RoleType.PHARMACIST, EnumSet.of(
                Permission.MANAGE_MEDICINES,
                Permission.MANAGE_INVENTORY,
                Permission.DISPENSE_MEDICINE,
                Permission.VIEW_PHARMACY,
                Permission.VIEW_PRESCRIPTION,
                Permission.VIEW_PATIENT
        ));

        // LAB_STAFF permissions
        ROLE_PERMISSIONS.put(RoleType.LAB_STAFF, EnumSet.of(
                Permission.MANAGE_LAB_CATALOG,
                Permission.ENTER_LAB_RESULT,
                Permission.VIEW_LAB_RESULTS,
                Permission.VIEW_PATIENT
        ));

        // BILLING_STAFF permissions
        ROLE_PERMISSIONS.put(RoleType.BILLING_STAFF, EnumSet.of(
                Permission.CREATE_INVOICE,
                Permission.COLLECT_PAYMENT,
                Permission.VIEW_INVOICES,
                Permission.APPLY_DISCOUNT,
                Permission.VIEW_PATIENT,
                Permission.VIEW_ADMISSIONS,
                Permission.VIEW_APPOINTMENTS
        ));
    }

    public static Set<Permission> getPermissions(RoleType role) {
        if (role == null) return Collections.emptySet();
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptySet());
    }

    public static boolean hasPermission(RoleType role, Permission permission) {
        if (role == null || permission == null) return false;
        Set<Permission> perms = ROLE_PERMISSIONS.get(role);
        return perms != null && perms.contains(permission);
    }
}
