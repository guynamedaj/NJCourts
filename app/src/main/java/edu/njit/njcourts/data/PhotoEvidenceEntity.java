package edu.njit.njcourts.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import edu.njit.njcourts.models.PhotoMetadata;

/**
 * Task 7: RoomDB Entity for Photo Evidence.
 * Links to a TicketEntity via ticketNumber.
 */
@Entity(
    tableName = "photo_evidence",
    foreignKeys = @ForeignKey(
        entity = TicketEntity.class,
        parentColumns = "ticketNumber",
        childColumns = "ticketNumber",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("ticketNumber")}
)
public class PhotoEvidenceEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String ticketNumber;
    
    // Metadata from Task 8
    public String originalFilename;
    public long timestamp;
    public String validationStatus;
    public String localFilePath; // If storing as file
    
    // Status for Task 7/33
    public String syncStatus; // LOCAL_ONLY, SYNCED, FAILED

    /**
     * Stores the actual compressed photo data (< 250KB).
     */
    public byte[] photoBlob;

    public PhotoEvidenceEntity(String ticketNumber, String originalFilename, long timestamp, 
                                String validationStatus, String syncStatus, byte[] photoBlob) {
        this.ticketNumber = ticketNumber;
        this.originalFilename = originalFilename;
        this.timestamp = timestamp;
        this.validationStatus = validationStatus;
        this.syncStatus = syncStatus;
        this.photoBlob = photoBlob;
    }
}
