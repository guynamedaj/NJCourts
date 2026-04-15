package edu.njit.njcourts.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * Task 7: DAO for Photo Evidence.
 */
@Dao
public interface EvidenceDao {
    @Query("SELECT * FROM photo_evidence WHERE ticketNumber = :ticketNum")
    LiveData<List<PhotoEvidenceEntity>> getEvidenceForTicket(String ticketNum);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvidence(PhotoEvidenceEntity evidence);

    @Delete
    void deleteEvidence(PhotoEvidenceEntity evidence);

    @Query("SELECT COUNT(*) FROM photo_evidence WHERE ticketNumber = :ticketNum")
    LiveData<Integer> getEvidenceCount(String ticketNum);

    @Query("UPDATE photo_evidence SET syncStatus = :status WHERE id = :id")
    void updateSyncStatus(int id, String status);
}
