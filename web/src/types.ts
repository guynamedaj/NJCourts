export interface Ticket {
  id: string
  ticketNumber: string
  issuedAt: string
  officerName: string
  suspectName: string
  violationType: string
  vehicleMake: string
  vehicleModel: string
  vehicleColor: string
  /** Stored citation plate; displayed with state in overview */
  licensePlate: string
  location: string
  notes: string

  /** PCSAM-style: court line (shown in teal) */
  courtDisplay: string
  /** Display case id e.g. P01 260001 */
  caseDisplay: string
  plateState: string
  caseType: string
  caseStatus: string
  warrantStatus: string
  activeWarrant: string
  /** e.g. "332-22 - Parking prohibited at all times" */
  offenseDisplay: string
  timePayment: string
  bailStatus: string
  licenseSurrendered: string
  dateOfBirth: string
  dlNumber: string
  dlState: string
  dlExpiration: string
  defendantAddress: string
  commercialLicense: string
  restrictionClass: string
  restrictionType: string
}

export interface PhotoEvidence {
  id: string
  ticketId: string
  fileName: string
  uploadTimestamp: string
  /** Simulated S3 object URL for future API integration */
  s3Url: string
  /** URL used for <img> (may differ from s3Url in mock data) */
  previewUrl: string
}

export type SortKey =
  | 'ticketNumber'
  | 'issuedAt'
  | 'suspectName'
  | 'licensePlate'
  | 'violationType'
  | 'vehicle'

export type SortDir = 'asc' | 'desc'
