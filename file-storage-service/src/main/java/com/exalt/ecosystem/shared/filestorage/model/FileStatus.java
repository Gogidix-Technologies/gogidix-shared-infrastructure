package com.exalt.ecosystem.shared.filestorage.model;

/**
 * Enum representing the status of a file in the system
 */
public enum FileStatus {
    UPLOADING("uploading"),
    UPLOADED("uploaded"),
    PROCESSING("processing"),
    PROCESSED("processed"),
    FAILED("failed"),
    QUARANTINED("quarantined"),
    DELETED("deleted"),
    ARCHIVED("archived");

    private final String value;

    FileStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
