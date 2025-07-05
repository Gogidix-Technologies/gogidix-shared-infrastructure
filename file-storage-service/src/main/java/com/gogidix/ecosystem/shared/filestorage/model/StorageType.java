package com.gogidix.ecosystem.shared.filestorage.model;

/**
 * Enum representing different storage backend types
 */
public enum StorageType {
    LOCAL("local"),
    AWS_S3("aws_s3"),
    AZURE_BLOB("azure_blob"),
    GOOGLE_CLOUD("google_cloud"),
    FTP("ftp");

    private final String value;

    StorageType(String value) {
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
