package com.officecollab.documentcore.domain;

/** Mirrors the officecollab.document_permissions.permission check constraint. */
public enum PermissionLevel {
    OWNER("owner"),
    EDIT("edit"),
    COMMENT("comment"),
    VIEW("view");

    private final String dbValue;

    PermissionLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String dbValue() {
        return dbValue;
    }

    public static PermissionLevel fromDbValue(String value) {
        for (PermissionLevel level : values()) {
            if (level.dbValue.equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown permission level: " + value);
    }

    /** ONLYOFFICE editor mode for this POC is binary: owner/edit -> "edit", comment/view -> "view". */
    public boolean allowsEdit() {
        return this == OWNER || this == EDIT;
    }
}
