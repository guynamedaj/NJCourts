import type { PhotoEvidence, Ticket } from './types'

const bucket = 'pa-evidence-prod-mock'

/** Demo court code; handheld device issues 1214 / T90 per deployment notes */
export const DEMO_COURT_CODE = '1214'

function mockS3Key(ticketId: string, file: string) {
  return `s3://${bucket}/tickets/${ticketId}/evidence/${file}`
}

function mockS3Https(ticketId: string, file: string) {
  return `https://${bucket}.s3.amazonaws.com/tickets/${ticketId}/evidence/${file}`
}

function previewUrl(seed: string, w = 640, h = 480) {
  return `https://picsum.photos/seed/${encodeURIComponent(seed)}/${w}/${h}`
}

/** Compact plate for "X66KTS NJ" style overview */
export function plateCompact(plate: string): string {
  return plate.replace(/[^a-z0-9]/gi, '').toUpperCase()
}

export function plateNumberState(ticket: Ticket): string {
  return `${plateCompact(ticket.licensePlate)} ${ticket.plateState}`
}

export function formatTicketNumber(
  courtCode: string,
  ticketPrefix: string,
  sequenceNumber: string,
): string {
  return `${courtCode} ${ticketPrefix} ${sequenceNumber}`
}

type PcsamFields = Pick<
  Ticket,
  | 'courtDisplay'
  | 'caseDisplay'
  | 'plateState'
  | 'caseType'
  | 'caseStatus'
  | 'warrantStatus'
  | 'activeWarrant'
  | 'offenseDisplay'
  | 'timePayment'
  | 'bailStatus'
  | 'licenseSurrendered'
  | 'dateOfBirth'
  | 'dlNumber'
  | 'dlState'
  | 'dlExpiration'
  | 'defendantAddress'
  | 'commercialLicense'
  | 'restrictionClass'
  | 'restrictionType'
>

type TicketCore = Omit<Ticket, keyof PcsamFields | 'ticketNumber'>

function ticket(core: TicketCore, pcsam: Partial<PcsamFields> = {}): Ticket {
  const ticketNumber = formatTicketNumber(
    core.courtCode,
    core.ticketPrefix,
    core.sequenceNumber,
  )
  const defaults: PcsamFields = {
    courtDisplay: `${core.courtCode} - Jersey city municipal court`,
    caseDisplay: ticketNumber,
    plateState: 'NJ',
    caseType: 'Parking',
    caseStatus: 'Open',
    warrantStatus: '---',
    activeWarrant: 'No',
    offenseDisplay: `39:4-138 - ${core.violationType}`,
    timePayment: 'No',
    bailStatus: '---',
    licenseSurrendered: '---',
    dateOfBirth: '---',
    dlNumber: '---',
    dlState: '---',
    dlExpiration: '---',
    defendantAddress: '---',
    commercialLicense: '---',
    restrictionClass: '---',
    restrictionType: '---',
  }
  return { ...core, ticketNumber, ...defaults, ...pcsam }
}

/**
 * Demo prefixes: T01 and T90 (device issues 1214 + T90). Sequences 260001+ use year prefix 26 (2026).
 */
export const mockTickets: Ticket[] = [
  ticket(
    {
      id: 't-001',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260001',
      issuedAt: '2026-03-12T14:22:00-04:00',
      officerName: 'Officer M. Chen',
      defendantName: '---',
      violationType: 'Expired meter',
      vehicleMake: 'Honda',
      vehicleModel: 'Civic',
      vehicleColor: 'Silver',
      licensePlate: 'NJ·K42·7XR',
      location: '12 Market St, Newark — Zone C',
      notes: 'Meter expired 47 minutes; vehicle confirmed on plate lookup.',
    },
    {
      caseStatus: 'DL suspended',
      offenseDisplay: '39:4-138 - Parking at expired meter',
    },
  ),
  ticket(
    {
      id: 't-002',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260002',
      issuedAt: '2026-03-14T09:05:00-04:00',
      officerName: 'Officer R. Patel',
      defendantName: '---',
      violationType: 'No parking zone',
      vehicleMake: 'Toyota',
      vehicleModel: 'RAV4',
      vehicleColor: 'White',
      licensePlate: 'NJ·M91·2PL',
      location: '400 Broad St — Fire hydrant buffer',
      notes: 'Posted signage visible; 15 ft from hydrant.',
    },
    {
      offenseDisplay: '39:4-56 - Stopping or parking within 25 feet of fire hydrant',
    },
  ),
  ticket(
    {
      id: 't-003',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T01',
      sequenceNumber: '260003',
      issuedAt: '2026-03-15T16:40:00-04:00',
      officerName: 'Officer A. Brooks',
      defendantName: '---',
      violationType: 'Street cleaning',
      vehicleMake: 'Ford',
      vehicleModel: 'Escape',
      vehicleColor: 'Blue',
      licensePlate: 'NJ·T08·4NQ',
      location: '88 University Ave — Alternate side',
      notes: 'Sweeping window active per posted schedule.',
    },
    {
      offenseDisplay: '39:4-138.1 - Street cleaning / alternate side parking',
    },
  ),
  ticket(
    {
      id: 't-004',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260004',
      issuedAt: '2026-03-18T11:18:00-04:00',
      officerName: 'Officer M. Chen',
      defendantName: '---',
      violationType: 'Handicap space',
      vehicleMake: 'Subaru',
      vehicleModel: 'Outback',
      vehicleColor: 'Green',
      licensePlate: 'NJ·P55·8LM',
      location: 'City Hall garage, Level P1',
      notes: 'No placard displayed; enforcement photo sequence captured.',
    },
    {
      offenseDisplay: '39:4-197.9 - Unauthorized parking in handicapped space',
    },
  ),
  ticket(
    {
      id: 't-005',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260005',
      issuedAt: '2026-03-19T08:52:00-04:00',
      officerName: 'Officer L. Nguyen',
      defendantName: '---',
      violationType: 'Bus stop',
      vehicleMake: 'Hyundai',
      vehicleModel: 'Elantra',
      vehicleColor: 'Black',
      licensePlate: 'NJ·R33·9KJ',
      location: 'Central Ave & Ferry St',
      notes: 'Vehicle overlapping marked bus stop zone.',
    },
    {},
  ),
  ticket(
    {
      id: 't-006',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260006',
      issuedAt: '2026-03-20T13:07:00-04:00',
      officerName: 'Officer R. Patel',
      defendantName: '---',
      violationType: 'Expired meter',
      vehicleMake: 'Chevrolet',
      vehicleModel: 'Bolt',
      vehicleColor: 'Red',
      licensePlate: 'NJ·C77·1ZT',
      location: '22 Raymond Blvd — Zone A',
      notes: 'Digital meter session ended; grace period elapsed.',
    },
    {
      caseStatus: 'Pending payment plan',
    },
  ),
  ticket(
    {
      id: 't-007',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T01',
      sequenceNumber: '260007',
      issuedAt: '2026-03-21T07:35:00-04:00',
      officerName: 'Officer A. Brooks',
      defendantName: '---',
      violationType: 'Residential permit',
      vehicleMake: 'Jeep',
      vehicleModel: 'Grand Cherokee',
      vehicleColor: 'Gray',
      licensePlate: 'NJ·H88·3WS',
      location: 'Lincoln Park — Permit block 12',
      notes: 'No resident permit sticker or digital credential.',
    },
    {
      offenseDisplay: '39:4-138.2 - Residential permit zone violation',
    },
  ),
  ticket(
    {
      id: 't-008',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260008',
      issuedAt: '2026-03-22T15:29:00-04:00',
      officerName: 'Officer L. Nguyen',
      defendantName: '---',
      violationType: 'Double parking',
      vehicleMake: 'Nissan',
      vehicleModel: 'Altima',
      vehicleColor: 'White',
      licensePlate: 'NJ·D12·6VB',
      location: 'Springfield Ave commercial corridor',
      notes: 'Obstructed travel lane; hazard lights only.',
    },
    {},
  ),
  ticket(
    {
      id: 't-009',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260009',
      issuedAt: '2026-03-23T10:44:00-04:00',
      officerName: 'Officer M. Chen',
      defendantName: '---',
      violationType: 'Loading zone',
      vehicleMake: 'Ram',
      vehicleModel: '1500',
      vehicleColor: 'Black',
      licensePlate: 'NJ·L90·4CD',
      location: 'Halsey St — 20-minute loading',
      notes: 'Exceeded posted limit; no active commercial session.',
    },
    {},
  ),
  ticket(
    {
      id: 't-010',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260010',
      issuedAt: '2026-03-24T12:01:00-04:00',
      officerName: 'Officer R. Patel',
      defendantName: '---',
      violationType: 'Expired registration',
      vehicleMake: 'Volkswagen',
      vehicleModel: 'Jetta',
      vehicleColor: 'Blue',
      licensePlate: 'NJ·N44·2GH',
      location: 'Branch Brook Park perimeter',
      notes: 'Plate scan flagged expired registration — secondary verification.',
    },
    {
      caseStatus: 'Registration hold',
      offenseDisplay: '39:3-4 - Operating with expired registration',
    },
  ),
  ticket(
    {
      id: 't-011',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T01',
      sequenceNumber: '260011',
      issuedAt: '2026-03-25T09:17:00-04:00',
      officerName: 'Officer A. Brooks',
      defendantName: '---',
      violationType: 'No parking zone',
      vehicleMake: 'Mazda',
      vehicleModel: 'CX-5',
      vehicleColor: 'Silver',
      licensePlate: 'NJ·Q66·5RT',
      location: 'Park Ave — School zone',
      notes: 'Restricted hours in effect; signage photographed.',
    },
    {},
  ),
  ticket(
    {
      id: 't-012',
      courtCode: DEMO_COURT_CODE,
      ticketPrefix: 'T90',
      sequenceNumber: '260012',
      issuedAt: '2026-03-26T18:50:00-04:00',
      officerName: 'Officer L. Nguyen',
      defendantName: '---',
      violationType: 'Expired meter',
      vehicleMake: 'Tesla',
      vehicleModel: 'Model 3',
      vehicleColor: 'White',
      licensePlate: 'NJ·Z01·8YX',
      location: 'Ironbound — Zone F',
      notes: 'Fleet rental; operator not present at time of issue.',
    },
    {
      caseStatus: 'Awaiting operator ID',
    },
  ),
]

const mockEvidenceByTicket: Record<string, PhotoEvidence[]> = {
  't-001': [
    {
      id: 'p-001-a',
      ticketId: 't-001',
      fileName: 'meter_display_001.jpg',
      uploadTimestamp: '2026-03-12T14:23:12-04:00',
      s3Url: mockS3Https('t-001', 'meter_display_001.jpg'),
      previewUrl: previewUrl('t-001-meter'),
    },
    {
      id: 'p-001-b',
      ticketId: 't-001',
      fileName: 'plate_rear_001.jpg',
      uploadTimestamp: '2026-03-12T14:23:45-04:00',
      s3Url: mockS3Https('t-001', 'plate_rear_001.jpg'),
      previewUrl: previewUrl('t-001-plate'),
    },
  ],
  't-002': [
    {
      id: 'p-002-a',
      ticketId: 't-002',
      fileName: 'hydrant_context.jpg',
      uploadTimestamp: '2026-03-14T09:06:02-04:00',
      s3Url: mockS3Https('t-002', 'hydrant_context.jpg'),
      previewUrl: previewUrl('t-002-hydrant'),
    },
  ],
  't-004': [
    {
      id: 'p-004-a',
      ticketId: 't-004',
      fileName: 'handicap_sign_wide.jpg',
      uploadTimestamp: '2026-03-18T11:19:30-04:00',
      s3Url: mockS3Https('t-004', 'handicap_sign_wide.jpg'),
      previewUrl: previewUrl('t-004-sign'),
    },
    {
      id: 'p-004-b',
      ticketId: 't-004',
      fileName: 'dash_no_placard.jpg',
      uploadTimestamp: '2026-03-18T11:20:01-04:00',
      s3Url: mockS3Https('t-004', 'dash_no_placard.jpg'),
      previewUrl: previewUrl('t-004-dash'),
    },
    {
      id: 'p-004-c',
      ticketId: 't-004',
      fileName: 'plate_front.jpg',
      uploadTimestamp: '2026-03-18T11:20:44-04:00',
      s3Url: mockS3Https('t-004', 'plate_front.jpg'),
      previewUrl: previewUrl('t-004-plate'),
    },
  ],
  't-006': [
    {
      id: 'p-006-a',
      ticketId: 't-006',
      fileName: 'meter_session_expired.png',
      uploadTimestamp: '2026-03-20T13:08:11-04:00',
      s3Url: mockS3Https('t-006', 'meter_session_expired.png'),
      previewUrl: previewUrl('t-006-meter'),
    },
  ],
  't-010': [
    {
      id: 'p-010-a',
      ticketId: 't-010',
      fileName: 'registration_flag_scan.jpg',
      uploadTimestamp: '2026-03-24T12:02:00-04:00',
      s3Url: mockS3Https('t-010', 'registration_flag_scan.jpg'),
      previewUrl: previewUrl('t-010-scan'),
    },
  ],
}

export function getMockS3Uri(ticketId: string, fileName: string) {
  return mockS3Key(ticketId, fileName)
}

export function getTicketById(id: string): Ticket | undefined {
  return mockTickets.find((t) => t.id === id)
}

export function getEvidenceForTicket(ticketId: string): PhotoEvidence[] {
  return mockEvidenceByTicket[ticketId] ?? []
}

export function getDistinctViolationTypes(): string[] {
  const set = new Set(mockTickets.map((t) => t.violationType))
  return [...set].sort((a, b) => a.localeCompare(b))
}
