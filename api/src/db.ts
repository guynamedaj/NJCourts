import { Pool } from 'pg'

if (!process.env.DATABASE_URL) {
  throw new Error('DATABASE_URL is required — add your Neon connection string to api/.env')
}

export const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
})

export async function initSchema(): Promise<void> {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS tickets (
      "id" TEXT PRIMARY KEY,
      "courtCode" TEXT NOT NULL,
      "ticketPrefix" TEXT NOT NULL,
      "sequenceNumber" TEXT NOT NULL,
      "ticketNumber" TEXT NOT NULL UNIQUE,
      "issuedAt" TEXT NOT NULL,
      "officerName" TEXT,
      "defendantName" TEXT,
      "violationType" TEXT NOT NULL,
      "vehicleMake" TEXT,
      "vehicleModel" TEXT,
      "vehicleColor" TEXT,
      "licensePlate" TEXT,
      "location" TEXT,
      "notes" TEXT,
      "courtDisplay" TEXT NOT NULL,
      "caseDisplay" TEXT NOT NULL,
      "plateState" TEXT,
      "caseType" TEXT NOT NULL DEFAULT 'Parking',
      "caseStatus" TEXT NOT NULL DEFAULT 'Open',
      "offenseDisplay" TEXT NOT NULL,
      "warrantStatus" TEXT,
      "activeWarrant" TEXT,
      "timePayment" TEXT,
      "bailStatus" TEXT,
      "licenseSurrendered" TEXT,
      "dateOfBirth" TEXT,
      "dlNumber" TEXT,
      "dlState" TEXT,
      "dlExpiration" TEXT,
      "defendantAddress" TEXT,
      "commercialLicense" TEXT,
      "restrictionClass" TEXT,
      "restrictionType" TEXT
    );
  `)

  // Additive migrations for tables that may predate newer columns.
  await pool.query(`ALTER TABLE tickets ADD COLUMN IF NOT EXISTS "violDate" TEXT;`)
  await pool.query(`ALTER TABLE tickets ADD COLUMN IF NOT EXISTS "violTime" TEXT;`)
  await pool.query(`ALTER TABLE tickets ADD COLUMN IF NOT EXISTS "courtDate" TEXT;`)
  await pool.query(`ALTER TABLE tickets ADD COLUMN IF NOT EXISTS "courtTime" TEXT;`)

  await pool.query(`
    CREATE TABLE IF NOT EXISTS photo_evidence (
      "id" TEXT PRIMARY KEY,
      "ticketId" TEXT NOT NULL REFERENCES tickets("id") ON DELETE CASCADE,
      "fileName" TEXT NOT NULL,
      "uploadTimestamp" TEXT NOT NULL,
      "s3Key" TEXT NOT NULL
    );
  `)

  await pool.query(`CREATE INDEX IF NOT EXISTS idx_evidence_ticket ON photo_evidence("ticketId");`)
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_tickets_issuedAt ON tickets("issuedAt");`)
  await pool.query(`CREATE INDEX IF NOT EXISTS idx_tickets_plate ON tickets("licensePlate");`)
}
