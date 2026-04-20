package edu.njit.njcourts.adapters;

import java.time.Instant;

import edu.njit.njcourts.data.ApiEvidence;
import edu.njit.njcourts.data.PhotoEvidenceEntity;

/**
 * Unified display model covering two sources of truth:
 *   - local photos captured on this device (Room, has photoBlob bytes)
 *   - photos fetched from the backend for this ticket (has s3Url/previewUrl)
 *
 * Either photoBlob or remoteUrl will be set. When both match the same file
 * (we de-dupe on fileName + timestamp), we prefer the local photo so the
 * thumbnail renders instantly from bytes.
 */
public final class EvidenceItem {
    public final String key;
    public final String fileName;
    public final long timestampMs;
    public final String syncStatus;
    public final byte[] photoBlob;
    public final String remoteUrl;
    public final PhotoEvidenceEntity localEntity;

    private EvidenceItem(
        String key, String fileName, long timestampMs, String syncStatus,
        byte[] photoBlob, String remoteUrl, PhotoEvidenceEntity localEntity
    ) {
        this.key = key;
        this.fileName = fileName;
        this.timestampMs = timestampMs;
        this.syncStatus = syncStatus;
        this.photoBlob = photoBlob;
        this.remoteUrl = remoteUrl;
        this.localEntity = localEntity;
    }

    public static EvidenceItem fromLocal(PhotoEvidenceEntity e) {
        return new EvidenceItem(
            "local-" + e.id,
            e.originalFilename,
            e.timestamp,
            e.syncStatus,
            e.photoBlob,
            null,
            e
        );
    }

    public static EvidenceItem fromRemote(ApiEvidence e) {
        long ts;
        try {
            ts = Instant.parse(e.uploadTimestamp).toEpochMilli();
        } catch (Exception ex) {
            ts = 0L;
        }
        return new EvidenceItem(
            "remote-" + e.id,
            e.fileName,
            ts,
            "SYNCED",
            null,
            e.s3Url,
            null
        );
    }

    public boolean canDelete() {
        return localEntity != null;
    }
}
