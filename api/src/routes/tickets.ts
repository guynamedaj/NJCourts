import { Router, type Request, type Response } from 'express'
import { pool } from '../db'
import { presignKey } from '../storage'

export const ticketsRouter = Router()

const SORT_COLUMNS: Record<string, string> = {
  ticketNumber: 'ticketNumber',
  issuedAt: 'issuedAt',
  licensePlate: 'licensePlate',
  violationType: 'violationType',
  vehicle: 'vehicleMake',
}

const PLACEHOLDER = '---'
const DISPLAY_NULLABLE_FIELDS = [
  'officerName',
  'defendantName',
  'vehicleMake',
  'vehicleModel',
  'vehicleColor',
  'licensePlate',
  'location',
  'notes',
  'plateState',
  'warrantStatus',
  'activeWarrant',
  'timePayment',
  'bailStatus',
  'licenseSurrendered',
  'dateOfBirth',
  'dlNumber',
  'dlState',
  'dlExpiration',
  'defendantAddress',
  'commercialLicense',
  'restrictionClass',
  'restrictionType',
  'violDate',
  'violTime',
  'courtDate',
  'courtTime',
] as const

function displayTicket(row: Record<string, unknown>): Record<string, unknown> {
  const out = { ...row }
  for (const field of DISPLAY_NULLABLE_FIELDS) {
    if (out[field] == null) out[field] = PLACEHOLDER
  }
  return out
}

interface EvidenceRow {
  id: string
  ticketId: string
  fileName: string
  uploadTimestamp: string
  s3Key: string
}

async function hydrateEvidenceUrls(rows: EvidenceRow[]) {
  return Promise.all(
    rows.map(async (row) => {
      const url = await presignKey(row.s3Key)
      return {
        id: row.id,
        ticketId: row.ticketId,
        fileName: row.fileName,
        uploadTimestamp: row.uploadTimestamp,
        s3Url: url,
        previewUrl: url,
      }
    }),
  )
}

ticketsRouter.get('/', async (req: Request, res: Response) => {
  const {
    keyword,
    ticketNumber,
    licensePlate,
    violationType,
    vehicle,
    dateFrom,
    dateTo,
    sortBy,
    sortDir,
  } = req.query

  const where: string[] = []
  const params: unknown[] = []

  if (typeof keyword === 'string' && keyword.trim()) {
    params.push(`%${keyword.trim()}%`)
    const i = params.length
    where.push(`("ticketNumber" ILIKE $${i} OR "caseDisplay" ILIKE $${i} OR "defendantName" ILIKE $${i} OR "licensePlate" ILIKE $${i} OR "violationType" ILIKE $${i} OR "offenseDisplay" ILIKE $${i})`)
  }
  if (typeof ticketNumber === 'string' && ticketNumber.trim()) {
    params.push(`%${ticketNumber.trim()}%`)
    where.push(`"ticketNumber" ILIKE $${params.length}`)
  }
  if (typeof licensePlate === 'string' && licensePlate.trim()) {
    params.push(`%${licensePlate.trim()}%`)
    where.push(`"licensePlate" ILIKE $${params.length}`)
  }
  if (typeof violationType === 'string' && violationType.trim() && violationType !== 'All types') {
    params.push(violationType.trim())
    where.push(`"violationType" = $${params.length}`)
  }
  if (typeof vehicle === 'string' && vehicle.trim()) {
    params.push(`%${vehicle.trim()}%`)
    const i = params.length
    where.push(`("vehicleMake" ILIKE $${i} OR "vehicleModel" ILIKE $${i} OR "vehicleColor" ILIKE $${i})`)
  }
  if (typeof dateFrom === 'string' && dateFrom.trim()) {
    params.push(dateFrom.trim())
    where.push(`"issuedAt" >= $${params.length}`)
  }
  if (typeof dateTo === 'string' && dateTo.trim()) {
    params.push(dateTo.trim())
    where.push(`"issuedAt" <= $${params.length}`)
  }

  const sortKey = typeof sortBy === 'string' ? sortBy : 'issuedAt'
  const sortColumn = SORT_COLUMNS[sortKey] ?? 'issuedAt'
  const sortOrder = String(sortDir ?? 'desc').toLowerCase() === 'asc' ? 'ASC' : 'DESC'

  const whereClause = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const sql = `SELECT * FROM tickets ${whereClause} ORDER BY "${sortColumn}" ${sortOrder}`

  try {
    const { rows } = await pool.query(sql, params)
    res.json(rows.map(displayTicket))
  } catch (err) {
    console.error('[tickets] list failed:', err)
    res.status(500).json({ error: 'Failed to list tickets' })
  }
})

ticketsRouter.get('/:id', async (req: Request, res: Response) => {
  try {
    const { rows } = await pool.query(
      'SELECT * FROM tickets WHERE "id" = $1 OR "ticketNumber" = $1 LIMIT 1',
      [req.params.id],
    )
    if (rows.length === 0) {
      res.status(404).json({ error: 'Ticket not found' })
      return
    }
    res.json(displayTicket(rows[0]))
  } catch (err) {
    console.error('[tickets] get failed:', err)
    res.status(500).json({ error: 'Failed to load ticket' })
  }
})

ticketsRouter.get('/:id/evidence', async (req: Request, res: Response) => {
  try {
    const ticketQuery = await pool.query<{ id: string }>(
      'SELECT "id" FROM tickets WHERE "id" = $1 OR "ticketNumber" = $1 LIMIT 1',
      [req.params.id],
    )
    if (ticketQuery.rows.length === 0) {
      res.status(404).json({ error: 'Ticket not found' })
      return
    }
    const ticketId = ticketQuery.rows[0].id

    const { rows } = await pool.query<EvidenceRow>(
      `SELECT "id", "ticketId", "fileName", "uploadTimestamp", "s3Key"
       FROM photo_evidence
       WHERE "ticketId" = $1
       ORDER BY "uploadTimestamp" ASC`,
      [ticketId],
    )

    const hydrated = await hydrateEvidenceUrls(rows)
    res.json(hydrated)
  } catch (err) {
    console.error('[tickets] evidence failed:', err)
    res.status(500).json({ error: 'Failed to load evidence' })
  }
})
