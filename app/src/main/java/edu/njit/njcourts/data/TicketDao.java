package edu.njit.njcourts.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Task 7: RoomDB DAO.
 */
@Dao
public interface TicketDao {
    @Query("SELECT * FROM tickets")
    LiveData<List<TicketEntity>> getAllTickets();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTickets(List<TicketEntity> tickets);
    
    @Query("UPDATE tickets SET syncStatus = :status WHERE ticketNumber = :id")
    void updateSyncStatus(String id, String status);
}
