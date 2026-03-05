package edu.njit.njcourts.models;

/**
 * Task 8: Data Model for Photo Metadata.
 */
public class PhotoMetadata {
    private String originalFilename;
    private String ticketNumber;
    private long timestamp;
    private String validationStatus; // e.g., "PASSED", "REJECTED"
    private String localFilePath;

    public PhotoMetadata(String originalFilename, String ticketNumber, long timestamp, String validationStatus, String localFilePath) {
        this.originalFilename = originalFilename;
        this.ticketNumber = ticketNumber;
        this.timestamp = timestamp;
        this.validationStatus = validationStatus;
        this.localFilePath = localFilePath;
    }

    // Getters and Setters
    public String getOriginalFilename() { return originalFilename; }
    public String getTicketNumber() { return ticketNumber; }
    public long getTimestamp() { return timestamp; }
    public String getValidationStatus() { return validationStatus; }
    public String getLocalFilePath() { return localFilePath; }
}
