import { Router, type Request, type Response } from 'express'
import multer from 'multer'
import { randomUUID } from 'crypto'
import { pool } from '../db'
import { storePhoto } from '../storage'

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 512 * 1024 },
})

export const uploadRouter = Router()

uploadRouter.post('/', upload.single('photo'), async (req: Request, res: Response) => {
  const { ticketNumber, timestamp } = req.body ?? {}
  const file = req.file

  if (!ticketNumber || !timestamp || !file) {
    res.status(400).json({ error: 'ticketNumber, timestamp, and photo are required' })
    return
  }

  try {
    const ticketQuery = await pool.query<{ id: string }>(
      'SELECT "id" FROM tickets WHERE "ticketNumber" = $1 OR "id" = $1 LIMIT 1',
      [ticketNumber],
    )
    if (ticketQuery.rows.length === 0) {
      res.status(404).json({ error: 'Ticket not found for provided ticketNumber' })
      return
    }
    const ticketId = ticketQuery.rows[0].id

    const existingQuery = await pool.query<{
      id: string
      ticketId: string
      fileName: string
      uploadTimestamp: string
      s3Key: string
    }>(
      `SELECT "id", "ticketId", "fileName", "uploadTimestamp", "s3Key"
       FROM photo_evidence
       WHERE "ticketId" = $1 AND "fileName" = $2 AND "uploadTimestamp" = $3
       LIMIT 1`,
      [ticketId, file.originalname, String(timestamp)],
    )
    if (existingQuery.rows.length > 0) {
      const row = existingQuery.rows[0]
      res.status(200).json({
        id: row.id,
        ticketId: row.ticketId,
        fileName: row.fileName,
        uploadTimestamp: row.uploadTimestamp,
        idempotent: true,
      })
      return
    }

    const stored = await storePhoto(ticketId, file.originalname, file.buffer, file.mimetype)
    const record = {
      id: `p-${randomUUID().slice(0, 8)}`,
      ticketId,
      fileName: file.originalname,
      uploadTimestamp: String(timestamp),
      s3Key: stored.s3Key,
    }

    await pool.query(
      `INSERT INTO photo_evidence (
        "id", "ticketId", "fileName", "uploadTimestamp", "s3Key"
      ) VALUES ($1,$2,$3,$4,$5)`,
      [record.id, record.ticketId, record.fileName, record.uploadTimestamp, record.s3Key],
    )

    res.status(201).json({
      id: record.id,
      ticketId: record.ticketId,
      fileName: record.fileName,
      uploadTimestamp: record.uploadTimestamp,
      s3Url: stored.s3Url,
      previewUrl: stored.s3Url,
    })
  } catch (err) {
    console.error('[upload] failed:', err)
    res.status(500).json({ error: 'Upload failed', detail: err instanceof Error ? err.message : String(err) })
  }
})
