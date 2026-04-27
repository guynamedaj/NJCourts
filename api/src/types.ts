export interface Ticket {
  id: string
  courtCode: string
  ticketPrefix: string
  sequenceNumber: string
  ticketNumber: string
  issuedAt: string
  officerName: string | null
  defendantName: string | null
  violationType: string
  vehicleMake: string | null
  vehicleModel: string | null
  vehicleColor: string | null
  licensePlate: string | null
  location: string | null
  notes: string | null

  courtDisplay: string
  caseDisplay: string
  plateState: string | null
  caseType: string
  caseStatus: string
  offenseDisplay: string

  warrantStatus: string | null
  activeWarrant: string | null
  timePayment: string | null
  bailStatus: string | null
  licenseSurrendered: string | null
  dateOfBirth: string | null
  dlNumber: string | null
  dlState: string | null
  dlExpiration: string | null
  defendantAddress: string | null
  commercialLicense: string | null
  restrictionClass: string | null
  restrictionType: string | null
}

export interface PhotoEvidence {
  id: string
  ticketId: string
  fileName: string
  uploadTimestamp: string
  s3Url: string
  previewUrl: string
}
