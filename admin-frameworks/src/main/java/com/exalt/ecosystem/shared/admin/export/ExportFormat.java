package com.exalt.ecosystem.shared.admin.export;

/**
 * Supported export formats.
 */
public enum ExportFormat {
    CSV("Comma-Separated Values", "text/csv", ".csv"),
    EXCEL_XLSX("Microsoft Excel (XLSX)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    PDF("Portable Document Format", "application/pdf", ".pdf"),
    JSON("JavaScript Object Notation", "application/json", ".json"),
    XML("eXtensible Markup Language", "application/xml", ".xml");

    private final String displayName;
    private final String mimeType;
    private final String fileExtension;

    ExportFormat(String displayName, String mimeType, String fileExtension) {
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static ExportFormat fromString(String value) {
        for (ExportFormat format : values()) {
            if (format.name().equalsIgnoreCase(value) || 
                format.getDisplayName().equalsIgnoreCase(value) ||
                format.getMimeType().equalsIgnoreCase(value) ||
                format.getFileExtension().equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
