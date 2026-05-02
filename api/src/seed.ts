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
    violDate: null,
    violTime: null,
    courtDate: null,
    courtTime: null,
    ...overrides,
  }
}

const SEED_TICKETS: Ticket[] = [
  buildTicket({ id: 't-001', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260001', issuedAt: '2026-03-12T14:22:00-04:00', officerName: 'Officer M. Chen', violationType: 'Expired meter', vehicleMake: 'Honda', vehicleModel: 'Civic', vehicleColor: 'Silver', licensePlate: 'NJ\u00b7K42\u00b77XR', location: '12 Market St, Newark \u2014 Zone C', notes: 'Meter expired 47 minutes; vehicle confirmed on plate lookup.' }),
  buildTicket({ id: 't-002', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260002', issuedAt: '2026-03-14T09:05:00-04:00', officerName: 'Officer R. Patel', violationType: 'No parking zone', vehicleMake: 'Toyota', vehicleModel: 'RAV4', vehicleColor: 'White', licensePlate: 'NJ\u00b7M91\u00b72PL', location: '400 Broad St \u2014 Fire hydrant buffer', notes: 'Posted signage visible; 15 ft from hydrant.' }),
  buildTicket({ id: 't-003', courtCode: DEMO_COURT, ticketPrefix: 'T01', sequenceNumber: '260003', issuedAt: '2026-03-15T16:40:00-04:00', officerName: 'Officer A. Brooks', violationType: 'Street cleaning', vehicleMake: 'Ford', vehicleModel: 'Escape', vehicleColor: 'Blue', licensePlate: 'NJ\u00b7T08\u00b74NQ', location: '88 University Ave \u2014 Alternate side', notes: 'Sweeping window active per posted schedule.' }),
  buildTicket({ id: 't-004', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260004', issuedAt: '2026-03-18T11:18:00-04:00', officerName: 'Officer M. Chen', violationType: 'Handicap space', vehicleMake: 'Subaru', vehicleModel: 'Outback', vehicleColor: 'Green', licensePlate: 'NJ\u00b7P55\u00b78LM', location: 'City Hall garage, Level P1', notes: 'No placard displayed; enforcement photo sequence captured.' }),
  buildTicket({ id: 't-005', courtCode: DEMO_COURT, ticketPrefix: 'T90', sequenceNumber: '260005', issuedAt: '2026-03-19T08:52:00-04:00', officerName: 'Officer L. Nguyen', violationType: 'Bus stop', vehicleMake: 'Hyundai', vehicleModel: 'Elantra', vehicleColor: 'Black', licensePlate: 'NJ\u00b7R33\u00b79KJ', location: 'Central Ave & Ferry St', notes: 'Vehicle overlapping marked bus stop zone.' }),

  // Android demo tickets — ticketNumber format must match what TicketSelectionActivity seeds.
  // viol/court date+time strings mirror the hardcoded values in TicketSelectionActivity.loadDemoTickets()
  // so the API-served tickets render identically to the offline-fallback ones.
  buildTicket(
    { id: 't-android-1', courtCode: '1111', ticketPrefix: 'D88', sequenceNumber: '260146', issuedAt: '2026-02-25T14:13:00-05:00', officerName: 'Officer J. Rivera', violationType: '19:2-3.6 PARKING PROHIBITED', vehicleMake: 'ACURA', vehicleModel: '2 DOOR', vehicleColor: 'BLUE', licensePlate: 'OUS70', location: 'MARKET ST', notes: 'Android demo ticket.' },
    { ticketNumber: '260146 - NJ | OUS70', offenseDisplay: '19:2-3.6 - Parking prohibited', violDate: '02/25/2026', violTime: '02:13 PM', courtDate: '03/04/2026', courtTime: '09:00 AM' },
  ),
  buildTicket(
    { id: 't-android-2', courtCode: '1214', ticketPrefix: 'P15', sequenceNumber: '260147', issuedAt: '2026-02-26T10:15:00-05:00', officerName: 'Officer K. Adams', violationType: '39:4-98 SPEEDING', vehicleMake: 'HONDA', vehicleModel: '4 DOOR', vehicleColor: 'SILVER', licensePlate: 'ABC12', location: 'BROAD ST', notes: 'Android demo ticket.' },
    { ticketNumber: '260147 - NJ | ABC12', offenseDisplay: '39:4-98 - Speeding', violDate: '02/26/2026', violTime: '10:15 AM', courtDate: '03/12/2026', courtTime: '01:00 PM' },
  ),
  buildTicket(
    { id: 't-android-3', courtCode: '1500', ticketPrefix: 'R22', sequenceNumber: '260148', issuedAt: '2026-02-27T23:45:00-05:00', officerName: 'Officer T. Ruiz', violationType: '39:4-138 FIRE HYDRANT', vehicleMake: 'FORD', vehicleModel: 'TRUCK', vehicleColor: 'WHITE', licensePlate: 'XYZ99', location: 'HIGH ST', notes: 'Android demo ticket.' },
    { ticketNumber: '260148 - NJ | XYZ99', offenseDisplay: '39:4-138 - Fire hydrant violation', violDate: '02/27/2026', violTime: '11:45 PM', courtDate: '03/15/2026', courtTime: '09:30 AM' },
  ),
]

export async function seedIfEmpty(): Promise<void> {
  let added = 0
  let backfilled = 0
  for (const t of SEED_TICKETS) {
    const result = await pool.query(
      `INSERT INTO tickets (
        "id", "courtCode", "ticketPrefix", "sequenceNumber", "ticketNumber", "issuedAt",
        "officerName", "defendantName", "violationType", "vehicleMake", "vehicleModel",
        "vehicleColor", "licensePlate", "location", "notes", "courtDisplay", "caseDisplay",
        "plateState", "caseType", "caseStatus", "offenseDisplay", "warrantStatus", "activeWarrant",
        "timePayment", "bailStatus", "licenseSurrendered", "dateOfBirth", "dlNumber", "dlState",
        "dlExpiration", "defendantAddress", "commercialLicense", "restrictionClass", "restrictionType",
        "violDate", "violTime", "courtDate", "courtTime"
      ) VALUES (
        $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23,$24,$25,$26,$27,$28,$29,$30,$31,$32,$33,$34,$35,$36,$37,$38
      ) ON CONFLICT ("ticketNumber") DO NOTHING`,
      [
        t.id, t.courtCode, t.ticketPrefix, t.sequenceNumber, t.ticketNumber, t.issuedAt,
        t.officerName, t.defendantName, t.violationType, t.vehicleMake, t.vehicleModel,
        t.vehicleColor, t.licensePlate, t.location, t.notes, t.courtDisplay, t.caseDisplay,
        t.plateState, t.caseType, t.caseStatus, t.offenseDisplay, t.warrantStatus, t.activeWarrant,
        t.timePayment, t.bailStatus, t.licenseSurrendered, t.dateOfBirth, t.dlNumber, t.dlState,
        t.dlExpiration, t.defendantAddress, t.commercialLicense, t.restrictionClass, t.restrictionType,
        t.violDate, t.violTime, t.courtDate, t.courtTime,
      ],
    )
    if (result.rowCount && result.rowCount > 0) {
      added++
      continue
    }

    // Row already existed (likely seeded before the new columns were added).
    // Backfill the four new fields ONLY where they're currently NULL \u2014 never
    // overwrite values an operator may have edited by hand.
    if (t.violDate || t.violTime || t.courtDate || t.courtTime) {
      const update = await pool.query(
        `UPDATE tickets
            SET "violDate"  = COALESCE("violDate",  $2),
                "violTime"  = COALESCE("violTime",  $3),
                "courtDate" = COALESCE("courtDate", $4),
                "courtTime" = COALESCE("courtTime", $5)
          WHERE "id" = $1
            AND ("violDate" IS NULL OR "violTime" IS NULL OR "courtDate" IS NULL OR "courtTime" IS NULL)`,
        [t.id, t.violDate, t.violTime, t.courtDate, t.courtTime],
      )
      if (update.rowCount && update.rowCount > 0) backfilled++
    }
  }
  if (added > 0) {
    console.log(`[seed] inserted ${added} new ticket(s); ${SEED_TICKETS.length - added} already present`)
  } else {
    console.log(`[seed] all ${SEED_TICKETS.length} seed tickets already present \u2014 skipping`)
  }
  if (backfilled > 0) {
    console.log(`[seed] backfilled viol/court date-time on ${backfilled} pre-existing ticket(s)`)
  }
}
