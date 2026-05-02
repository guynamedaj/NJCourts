export interface Ticket {
  id: string
  /** Court code; demo deployment uses 1214 */
  courtCode: string
  /** Prefix e.g. T01, T90, or P01–P99 */
  ticketPrefix: string
  /** Sequence number; begins with last two digits of issue year (e.g. 260001 for 2026) */
  sequenceNumber: string
  /** Full citation: "{courtCode} {ticketPrefix} {sequenceNumber}" */
  ticketNumber: string
  issuedAt: string
  officerName: string
  /**
   * Defendant name when known (e.g. from registration). Parking enforcement
   * typically cannot collect this — use "---" when unknown.
   */
  defendantName: string
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
  /** Usually same as ticket number for parking citations */
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

  /** Court appearance date as a pre-formatted string (e.g. "03/04/2026") to mirror Android display */
  courtDate: string
  /** Court appearance time as a pre-formatted string (e.g. "09:00 AM") to mirror Android display */
  courtTime: string
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
  | 'licensePlate'
  | 'violationType'
  | 'vehicle'

export type SortDir = 'asc' | 'desc'
