package edu.njit.njcourts.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Task 7: RoomDB Entity for Tickets.
 */
@Entity(tableName = "tickets")
public class TicketEntity {
    @PrimaryKey
    @NonNull
    public String ticketNumber;
    
    public String violation;
    public String vehicleSummary;
    
    // Status fields: LOCAL_ONLY, SYNCED, FAILED
    public String syncStatus;

    public TicketEntity(@NonNull String ticketNumber, String violation, String vehicleSummary, String syncStatus) {
        this.ticketNumber = ticketNumber;
        this.violation = violation;
        this.vehicleSummary = vehicleSummary;
        this.syncStatus = syncStatus;
    }
}
