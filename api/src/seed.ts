import { pool } from './db'
import type { Ticket } from './types'

const DEMO_COURT = '1214'

type TicketCore = Pick<
  Ticket,
  | 'id'
  | 'courtCode'
  | 'ticketPrefix'
  | 'sequenceNumber'
  | 'issuedAt'
  | 'officerName'
  | 'violationType'
  | 'vehicleMake'
  | 'vehicleModel'
  | 'vehicleColor'
  | 'licensePlate'
  | 'location'
  | 'notes'
>

function buildTicket(core: TicketCore, overrides: Partial<Ticket> = {}): Ticket {
  const ticketNumber = `${core.courtCode} ${core.ticketPrefix} ${core.sequenceNumber}`
  return {
    ...core,
    ticketNumber,
    defendantName: null,
    courtDisplay: `${core.courtCode} - Jersey city municipal court`,
    caseDisplay: ticketNumber,
    plateState: 'NJ',
    caseType: 'Parking',
    caseStatus: 'Open',
    offenseDisplay: `39:4-138 - ${core.violationType}`,
    warrantStatus: null,
    activeWarrant: null,
    timePayment: null,
    bailStatus: null,
    licenseSurrendered: null,
    dateOfBirth: null,
    dlNumber: null,
    dlState: null,
    dlExpiration: null,
    defendantAddress: null,
    commercialLicense: null,
    restrictionClass: null,
    restrictionType: null,
    ...overrides,
  }
}

const SEED_TICKETS: Ticket[] = [
  buildTicket({ id: 't-001', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260001', issuedAt: '2026-03-12T14:22:00-04:00', officerName: 'Officer M. Chen', violationType: 'Expired meter', vehicleMake: 'Honda', vehicleModel: 'Civic', vehicleColor: 'Silver', licensePlate: 'NJ\u00b7K42\u00b77XR', location: '12 Market St, Newark \u2014 Zone C', notes: 'Meter expired 47 minutes; vehicle confirmed on plate lookup.' }),
  buildTicket({ id: 't-002', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260002', issuedAt: '2026-03-14T09:05:00-04:00', officerName: 'Officer R. Patel', violationType: 'No parking zone', vehicleMake: 'Toyota', vehicleModel: 'RAV4', vehicleColor: 'White', licensePlate: 'NJ\u00b7M91\u00b72PL', location: '400 Broad St \u2014 Fire hydrant buffer', notes: 'Posted signage visible; 15 ft from hydrant.' }),
  buildTicket({ id: 't-003', courtCode: DEMO_COURT, ticketPrefix: 'T01', sequenceNumber: '260003', issuedAt: '2026-03-15T16:40:00-04:00', officerName: 'Officer A. Brooks', violationType: 'Street cleaning', vehicleMake: 'Ford', vehicleModel: 'Escape', vehicleColor: 'Blue', licensePlate: 'NJ\u00b7T08\u00b74NQ', location: '88 University Ave \u2014 Alternate side', notes: 'Sweeping window active per posted schedule.' }),
  buildTicket({ id: 't-004', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260004', issuedAt: '2026-03-18T11:18:00-04:00', officerName: 'Officer M. Chen', violationType: 'Handicap space', vehicleMake: 'Subaru', vehicleModel: 'Outback', vehicleColor: 'Green', licensePlate: 'NJ\u00b7P55\u00b78LM', location: 'City Hall garage, Level P1', notes: 'No placard displayed; enforcement photo sequence captured.' }),
  buildTicket({ id: 't-005', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260005', issuedAt: '2026-03-19T08:52:00-04:00', officerName: 'Officer L. Nguyen', violationType: 'Bus stop', vehicleMake: 'Hyundai', vehicleModel: 'Elantra', vehicleColor: 'Black', licensePlate: 'NJ\u00b7R33\u00b79KJ', location: 'Central Ave & Ferry St', notes: 'Vehicle overlapping marked bus stop zone.' }),
]

export async function seedIfEmpty(): Promise<void> {
  const { rows } = await pool.query<{ count: string }>('SELECT COUNT(*)::text AS count FROM tickets')
  const count = Number(rows[0]?.count ?? '0')
  if (count > 0) {
    console.log(`[seed] tickets table already has ${count} rows \u2014 skipping`)
    return
  }

  const client = await pool.connect()
  try {
    await client.query('BEGIN')

    for (const t of SEED_TICKETS) {
      await client.query(
        `INSERT INTO tickets (
          "id", "courtCode", "ticketPrefix", "sequenceNumber", "ticketNumber", "issuedAt",
          "officerName", "defendantName", "violationType", "vehicleMake", "vehicleModel",
          "vehicleColor", "licensePlate", "location", "notes", "courtDisplay", "caseDisplay",
          "plateState", "caseType", "caseStatus", "offenseDisplay", "warrantStatus", "activeWarrant",
          "timePayment", "bailStatus", "licenseSurrendered", "dateOfBirth", "dlNumber", "dlState",
          "dlExpiration", "defendantAddress", "commercialLicense", "restrictionClass", "restrictionType"
        ) VALUES (
          $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$30,$31,$32,$33,$34
        )`,
        [
          t.id, t.courtCode, t.ticketPrefix, t.sequenceNumber, t.ticketNumber, t.issuedAt,
          t.officerName, t.defendantName, t.violationType, t.vehicleMake, t.vehicleModel,
          t.vehicleColor, t.licensePlate, t.location, t.notes, t.courtDisplay, t.caseDisplay,
          t.plateState, t.caseType, t.caseStatus, t.offenseDisplay, t.warrantStatus, t.activeWarrant,
          t.timePayment, t.bailStatus, t.licenseSurrendered, t.dateOfBirth, t.dlNumber, t.dlState,
          t.dlExpiration, t.defendantAddress, t.commercialLicense, t.restrictionClass, t.restrictionType,
        ],
      )
    }

    await client.query('COMMIT')
    console.log(`[seed] inserted ${SEED_TICKETS.length} tickets (no seed evidence — upload photos via API)`)
  } catch (err) {
    await client.query('ROLLBACK')
    throw err
  } finally {
    client.release()
  }
}
