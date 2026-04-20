package edu.njit.njcourts.data;

/**
 * DTO for a photo_evidence row as returned by GET /tickets/:id/evidence.
 * s3Url and previewUrl are freshly-presigned URLs (1 hour TTL).
 */
public class ApiEvidence {
    public String id;
    public String ticketId;
    public String fileName;
    public String uploadTimestamp;
    public String s3Url;
    public String previewUrl;
}
