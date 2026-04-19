import type { PhotoEvidence, Ticket } from './types'

const bucket = 'pa-evidence-prod-mock'

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

function ticket(
  core: Omit<
    Ticket,
    keyof PcsamFields
  >,
  pcsam: Partial<PcsamFields> = {},
): Ticket {
  const defaults: PcsamFields = {
    courtDisplay: '0906 - Jersey city municipal court',
    caseDisplay: `P01 ${core.ticketNumber.replace(/\D/g, '').slice(-6)}`,
    plateState: 'NJ',
    caseType: 'Parking',
    caseStatus: 'Open',
    warrantStatus: '---',
    activeWarrant: 'No',
    offenseDisplay: `39:4-138 - ${core.violationType}`,
    timePayment: 'No',
    bailStatus: '---',
    licenseSurrendered: '---',
    dateOfBirth: '01/01/1980',
    dlNumber: 'W00000000000000',
    dlState: 'NJ',
    dlExpiration: '12/2028',
    defendantAddress: '100 Main St, Newark, NJ 07102',
    commercialLicense: '---',
    restrictionClass: 'D - Auto',
    restrictionType: '0 - None',
  }
  return { ...core, ...defaults, ...pcsam }
}

export const mockTickets: Ticket[] = [
  ticket(
    {
      id: 't-001',
      ticketNumber: 'PA-2024-88421',
      issuedAt: '2024-03-12T14:22:00-04:00',
      officerName: 'Officer M. Chen',
      suspectName: 'Jordan Ellis',
      violationType: 'Expired meter',
      vehicleMake: 'Honda',
      vehicleModel: 'Civic',
      vehicleColor: 'Silver',
      licensePlate: 'NJ·K42·7XR',
      location: '12 Market St, Newark — Zone C',
      notes: 'Meter expired 47 minutes; vehicle confirmed on plate lookup.',
    },
    {
      caseDisplay: 'P01 260001',
      caseStatus: 'DL suspended',
      offenseDisplay: '39:4-138 - Parking at expired meter',
      dateOfBirth: '03/15/1992',
      dlNumber: 'E12345678901234',
      dlExpiration: '08/2027',
      defendantAddress: '214 Clinton Ave, Newark, NJ 07108',
    },
  ),
  ticket(
    {
      id: 't-002',
      ticketNumber: 'PA-2024-88455',
      issuedAt: '2024-03-14T09:05:00-04:00',
      officerName: 'Officer R. Patel',
      suspectName: 'Sam Rivera',
      violationType: 'No parking zone',
      vehicleMake: 'Toyota',
      vehicleModel: 'RAV4',
      vehicleColor: 'White',
      licensePlate: 'NJ·M91·2PL',
      location: '400 Broad St — Fire hydrant buffer',
      notes: 'Posted signage visible; 15 ft from hydrant.',
    },
    {
      caseDisplay: 'P01 260055',
      offenseDisplay: '39:4-56 - Stopping or parking within 25 feet of fire hydrant',
      dateOfBirth: '11/22/1988',
      dlNumber: 'R98765432109876',
      dlExpiration: '05/2026',
      defendantAddress: '88 Ferry St, Newark, NJ 07105',
    },
  ),
  ticket(
    {
      id: 't-003',
      ticketNumber: 'PA-2024-88502',
      issuedAt: '2024-03-15T16:40:00-04:00',
      officerName: 'Officer A. Brooks',
      suspectName: 'Taylor Morgan',
      violationType: 'Street cleaning',
      vehicleMake: 'Ford',
      vehicleModel: 'Escape',
      vehicleColor: 'Blue',
      licensePlate: 'NJ·T08·4NQ',
      location: '88 University Ave — Alternate side',
      notes: 'Sweeping window active per posted schedule.',
    },
    {
      caseDisplay: 'P01 260502',
      offenseDisplay: '39:4-138.1 - Street cleaning / alternate side parking',
      dateOfBirth: '07/04/1995',
      defendantAddress: '45 Roseville Ave, Newark, NJ 07107',
    },
  ),
  ticket(
    {
      id: 't-004',
      ticketNumber: 'PA-2024-88541',
      issuedAt: '2024-03-18T11:18:00-04:00',
      officerName: 'Officer M. Chen',
      suspectName: 'Chris Okonkwo',
      violationType: 'Handicap space',
      vehicleMake: 'Subaru',
      vehicleModel: 'Outback',
      vehicleColor: 'Green',
      licensePlate: 'NJ·P55·8LM',
      location: 'City Hall garage, Level P1',
      notes: 'No placard displayed; enforcement photo sequence captured.',
    },
    {
      caseDisplay: 'P01 260541',
      offenseDisplay: '39:4-197.9 - Unauthorized parking in handicapped space',
      dateOfBirth: '09/19/1984',
      dlNumber: 'O55443322110099',
      dlExpiration: '03/2026',
      defendantAddress: '600 Springfield Ave, Newark, NJ 07103',
    },
  ),
  ticket(
    {
      id: 't-005',
      ticketNumber: 'PA-2024-88590',
      issuedAt: '2024-03-19T08:52:00-04:00',
      officerName: 'Officer L. Nguyen',
      suspectName: 'Priya Shah',
      violationType: 'Bus stop',
      vehicleMake: 'Hyundai',
      vehicleModel: 'Elantra',
      vehicleColor: 'Black',
      licensePlate: 'NJ·R33·9KJ',
      location: 'Central Ave & Ferry St',
      notes: 'Vehicle overlapping marked bus stop zone.',
    },
    {
      caseDisplay: 'P01 260590',
      dateOfBirth: '02/28/1990',
      defendantAddress: '12 James St, Jersey City, NJ 07302',
    },
  ),
  ticket(
    {
      id: 't-006',
      ticketNumber: 'PA-2024-88612',
      issuedAt: '2024-03-20T13:07:00-04:00',
      officerName: 'Officer R. Patel',
      suspectName: 'Alex Kim',
      violationType: 'Expired meter',
      vehicleMake: 'Chevrolet',
      vehicleModel: 'Bolt',
      vehicleColor: 'Red',
      licensePlate: 'NJ·C77·1ZT',
      location: '22 Raymond Blvd — Zone A',
      notes: 'Digital meter session ended; grace period elapsed.',
    },
    {
      caseDisplay: 'P01 260612',
      caseStatus: 'Pending payment plan',
      dateOfBirth: '12/01/1987',
      dlNumber: 'K11223344556677',
      dlExpiration: '11/2025',
      defendantAddress: '300 Central Ave, Newark, NJ 07103',
    },
  ),
  ticket(
    {
      id: 't-007',
      ticketNumber: 'PA-2024-88644',
      issuedAt: '2024-03-21T07:35:00-04:00',
      officerName: 'Officer A. Brooks',
      suspectName: 'Morgan Lee',
      violationType: 'Residential permit',
      vehicleMake: 'Jeep',
      vehicleModel: 'Grand Cherokee',
      vehicleColor: 'Gray',
      licensePlate: 'NJ·H88·3WS',
      location: 'Lincoln Park — Permit block 12',
      notes: 'No resident permit sticker or digital credential.',
    },
    {
      caseDisplay: 'P01 260644',
      offenseDisplay: '39:4-138.2 - Residential permit zone violation',
      dateOfBirth: '05/17/1979',
      defendantAddress: '9 Lincoln Park W, Newark, NJ 07104',
    },
  ),
  ticket(
    {
      id: 't-008',
      ticketNumber: 'PA-2024-88671',
      issuedAt: '2024-03-22T15:29:00-04:00',
      officerName: 'Officer L. Nguyen',
      suspectName: 'Jamie Ortiz',
      violationType: 'Double parking',
      vehicleMake: 'Nissan',
      vehicleModel: 'Altima',
      vehicleColor: 'White',
      licensePlate: 'NJ·D12·6VB',
      location: 'Springfield Ave commercial corridor',
      notes: 'Obstructed travel lane; hazard lights only.',
    },
    {
      caseDisplay: 'P01 260671',
      dateOfBirth: '08/30/1993',
      dlNumber: 'V99887766554433',
      dlExpiration: '01/2029',
      defendantAddress: '155 Springfield Ave, Newark, NJ 07103',
    },
  ),
  ticket(
    {
      id: 't-009',
      ticketNumber: 'PA-2024-88705',
      issuedAt: '2024-03-23T10:44:00-04:00',
      officerName: 'Officer M. Chen',
      suspectName: 'Riley Thompson',
      violationType: 'Loading zone',
      vehicleMake: 'Ram',
      vehicleModel: '1500',
      vehicleColor: 'Black',
      licensePlate: 'NJ·L90·4CD',
      location: 'Halsey St — 20-minute loading',
      notes: 'Exceeded posted limit; no active commercial session.',
    },
    {
      caseDisplay: 'P01 260705',
      dateOfBirth: '04/12/1986',
      defendantAddress: '42 Halsey St, Newark, NJ 07102',
    },
  ),
  ticket(
    {
      id: 't-010',
      ticketNumber: 'PA-2024-88738',
      issuedAt: '2024-03-24T12:01:00-04:00',
      officerName: 'Officer R. Patel',
      suspectName: 'Casey Nguyen',
      violationType: 'Expired registration',
      vehicleMake: 'Volkswagen',
      vehicleModel: 'Jetta',
      vehicleColor: 'Blue',
      licensePlate: 'NJ·N44·2GH',
      location: 'Branch Brook Park perimeter',
      notes: 'Plate scan flagged expired registration — secondary verification.',
    },
    {
      caseDisplay: 'P01 260738',
      caseStatus: 'Registration hold',
      offenseDisplay: '39:3-4 - Operating with expired registration',
      dateOfBirth: '06/25/1991',
      dlNumber: 'N44332211009988',
      dlExpiration: '02/2024',
      defendantAddress: '77 Park Ave, East Orange, NJ 07017',
    },
  ),
  ticket(
    {
      id: 't-011',
      ticketNumber: 'PA-2024-88761',
      issuedAt: '2024-03-25T09:17:00-04:00',
      officerName: 'Officer A. Brooks',
      suspectName: 'Dana Frost',
      violationType: 'No parking zone',
      vehicleMake: 'Mazda',
      vehicleModel: 'CX-5',
      vehicleColor: 'Silver',
      licensePlate: 'NJ·Q66·5RT',
      location: 'Park Ave — School zone',
      notes: 'Restricted hours in effect; signage photographed.',
    },
    {
      caseDisplay: 'P01 260761',
      dateOfBirth: '10/03/1983',
      defendantAddress: '220 Park Ave, Newark, NJ 07104',
    },
  ),
  ticket(
    {
      id: 't-012',
      ticketNumber: 'PA-2024-88802',
      issuedAt: '2024-03-26T18:50:00-04:00',
      officerName: 'Officer L. Nguyen',
      suspectName: 'Unknown / rental',
      violationType: 'Expired meter',
      vehicleMake: 'Tesla',
      vehicleModel: 'Model 3',
      vehicleColor: 'White',
      licensePlate: 'NJ·Z01·8YX',
      location: 'Ironbound — Zone F',
      notes: 'Fleet rental; operator not present at time of issue.',
    },
    {
      caseDisplay: 'P01 260802',
      caseStatus: 'Awaiting operator ID',
      dateOfBirth: '---',
      dlNumber: '---',
      dlState: '---',
      dlExpiration: '---',
      defendantAddress: '---',
      restrictionClass: '---',
      restrictionType: '---',
    },
  ),
]

const mockEvidenceByTicket: Record<string, PhotoEvidence[]> = {
  't-001': [
    {
      id: 'p-001-a',
      ticketId: 't-001',
      fileName: 'meter_display_001.jpg',
      uploadTimestamp: '2024-03-12T14:23:12-04:00',
      s3Url: mockS3Https('t-001', 'meter_display_001.jpg'),
      previewUrl: previewUrl('t-001-meter'),
    },
    {
      id: 'p-001-b',
      ticketId: 't-001',
      fileName: 'plate_rear_001.jpg',
      uploadTimestamp: '2024-03-12T14:23:45-04:00',
      s3Url: mockS3Https('t-001', 'plate_rear_001.jpg'),
      previewUrl: previewUrl('t-001-plate'),
    },
  ],
  't-002': [
    {
      id: 'p-002-a',
      ticketId: 't-002',
      fileName: 'hydrant_context.jpg',
      uploadTimestamp: '2024-03-14T09:06:02-04:00',
      s3Url: mockS3Https('t-002', 'hydrant_context.jpg'),
      previewUrl: previewUrl('t-002-hydrant'),
    },
  ],
  't-004': [
    {
      id: 'p-004-a',
      ticketId: 't-004',
      fileName: 'handicap_sign_wide.jpg',
      uploadTimestamp: '2024-03-18T11:19:30-04:00',
      s3Url: mockS3Https('t-004', 'handicap_sign_wide.jpg'),
      previewUrl: previewUrl('t-004-sign'),
    },
    {
      id: 'p-004-b',
      ticketId: 't-004',
      fileName: 'dash_no_placard.jpg',
      uploadTimestamp: '2024-03-18T11:20:01-04:00',
      s3Url: mockS3Https('t-004', 'dash_no_placard.jpg'),
      previewUrl: previewUrl('t-004-dash'),
    },
    {
      id: 'p-004-c',
      ticketId: 't-004',
      fileName: 'plate_front.jpg',
      uploadTimestamp: '2024-03-18T11:20:44-04:00',
      s3Url: mockS3Https('t-004', 'plate_front.jpg'),
      previewUrl: previewUrl('t-004-plate'),
    },
  ],
  't-006': [
    {
      id: 'p-006-a',
      ticketId: 't-006',
      fileName: 'meter_session_expired.png',
      uploadTimestamp: '2024-03-20T13:08:11-04:00',
      s3Url: mockS3Https('t-006', 'meter_session_expired.png'),
      previewUrl: previewUrl('t-006-meter'),
    },
  ],
  't-010': [
    {
      id: 'p-010-a',
      ticketId: 't-010',
      fileName: 'registration_flag_scan.jpg',
      uploadTimestamp: '2024-03-24T12:02:00-04:00',
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
