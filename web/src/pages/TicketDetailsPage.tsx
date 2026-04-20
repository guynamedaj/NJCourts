import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getTicket } from '../api/client'
import { plateNumberState } from '../mockData'
import { CollapsiblePanel } from '../components/CollapsiblePanel'
import { OverviewField } from '../components/OverviewField'
import { TicketTabBar } from '../components/TicketTabBar'
import type { Ticket } from '../types'

export function TicketDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [ticket, setTicket] = useState<Ticket | null | undefined>(undefined)
  const [error, setError] = useState<string | null>(null)
  const [defendantOpen, setDefendantOpen] = useState(true)
  const [vehicleOpen, setVehicleOpen] = useState(true)

  useEffect(() => {
    if (!id) {
      setTicket(null)
      return
    }
    let cancelled = false
    setTicket(undefined)
    setError(null)
    getTicket(id)
      .then((data) => {
        if (!cancelled) setTicket(data)
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load ticket')
          setTicket(null)
        }
      })
    return () => {
      cancelled = true
    }
  }, [id])

  if (ticket === undefined) {
    return (
      <div className="border border-gray-300 bg-white p-8 text-center">
        <p className="text-sm text-gray-600">Loading ticket…</p>
      </div>
    )
  }

  if (!ticket) {
    return (
      <div className="border border-gray-300 bg-white p-8 text-center">
        <h2 className="text-lg font-bold text-gray-900">Ticket not found</h2>
        <p className="mt-2 text-sm text-gray-600">
          {error ?? 'No citation matches this reference. Return to search and select a valid row.'}
        </p>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="mt-6 border border-gray-400 bg-[#f0f3f5] px-4 py-2 text-sm font-medium text-gray-900 hover:bg-[#e5e9ec]"
        >
          Go back
        </button>
      </div>
    )
  }

  const issued = new Date(ticket.issuedAt).toLocaleString(undefined, {
    dateStyle: 'short',
    timeStyle: 'short',
  })

  const anyPanelOpen = defendantOpen || vehicleOpen
  const toggleAllPanels = () => {
    const next = !anyPanelOpen
    setDefendantOpen(next)
    setVehicleOpen(next)
  }

  return (
    <div className="space-y-0">
      <div className="mb-3 flex flex-wrap items-center gap-2 text-sm">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="border border-gray-400 bg-white px-3 py-1.5 font-medium text-gray-900 hover:bg-gray-50"
        >
          ← Back to results
        </button>
        <Link to="/" className="font-normal text-[#0d9488] hover:underline">
          Case search
        </Link>
      </div>

      <section className="border border-gray-300 bg-white">
        <h2 className="border-b border-gray-300 bg-white px-4 py-3 text-base font-bold text-black">
          Case overview
        </h2>
        <div className="grid grid-cols-2 gap-x-8 gap-y-5 px-4 py-5 lg:grid-cols-4">
          <OverviewField label="Court" value={ticket.courtDisplay} highlight />
          <OverviewField label="Court code" value={ticket.courtCode} />
          <OverviewField label="Prefix" value={ticket.ticketPrefix} />
          <OverviewField label="Sequence number" value={ticket.sequenceNumber} />
          <OverviewField label="Citation number" value={ticket.ticketNumber} />
          <OverviewField label="Defendant name" value={ticket.defendantName} />
          <OverviewField label="Plate number/State" value={plateNumberState(ticket)} />
          <OverviewField label="Case type" value={ticket.caseType} />
          <OverviewField label="Case status" value={ticket.caseStatus} />
          <OverviewField label="Warrant status" value={ticket.warrantStatus} />
          <OverviewField label="Active warrant" value={ticket.activeWarrant} />
          <OverviewField label="Offense" value={ticket.offenseDisplay} />
          <OverviewField label="Time payment" value={ticket.timePayment} />
          <OverviewField label="Bail status" value={ticket.bailStatus} />
          <OverviewField label="License surrendered" value={ticket.licenseSurrendered} />
          <OverviewField label="Eligible for bail waiver" value="---" />
        </div>
        <div className="border-t border-gray-300 bg-white px-4 py-3">
          <p className="text-xs font-bold text-gray-800">Quick links</p>
          <div className="mt-1 flex flex-wrap gap-x-4 gap-y-1">
            <Link
              to={`/ticket/${ticket.id}/evidence`}
              className="text-sm font-normal text-[#0d9488] hover:underline"
            >
              Assessed Totals and Payments
            </Link>
            <Link
              to={`/ticket/${ticket.id}/evidence`}
              className="text-sm font-normal text-[#0d9488] hover:underline"
            >
              Photo evidence
            </Link>
          </div>
        </div>
      </section>

      <div className="mt-3">
        <TicketTabBar ticketId={ticket.id} active="summary" />
      </div>

      <div className="mt-0 border border-gray-300 border-t-0 bg-white">
        <div className="flex justify-end border-b border-gray-300 bg-white px-3 py-1.5">
          <button
            type="button"
            onClick={toggleAllPanels}
            className="text-sm font-normal text-[#0d9488] hover:underline"
          >
            Expand / Collapse
          </button>
        </div>

        <CollapsiblePanel
          title="Defendant information"
          open={defendantOpen}
          onOpenChange={setDefendantOpen}
        >
          <div className="grid grid-cols-1 gap-x-8 gap-y-5 sm:grid-cols-2 lg:grid-cols-4">
            <OverviewField label="Name" value={ticket.defendantName} />
            <OverviewField label="Date of birth" value={ticket.dateOfBirth} />
            <OverviewField
              label="DL number/state"
              value={`${ticket.dlNumber} ${ticket.dlState}`}
            />
            <OverviewField label="DL expiration date" value={ticket.dlExpiration} />
            <OverviewField label="Commercial license" value={ticket.commercialLicense} />
            <OverviewField label="Address" value={ticket.defendantAddress} />
            <OverviewField label="Restriction class" value={ticket.restrictionClass} />
            <OverviewField label="Restriction type" value={ticket.restrictionType} />
          </div>
        </CollapsiblePanel>

        <CollapsiblePanel
          title="Citation & vehicle information"
          open={vehicleOpen}
          onOpenChange={setVehicleOpen}
        >
          <div className="grid grid-cols-1 gap-x-8 gap-y-5 sm:grid-cols-2 lg:grid-cols-4">
            <OverviewField label="Vehicle make" value={ticket.vehicleMake} />
            <OverviewField label="Vehicle model" value={ticket.vehicleModel} />
            <OverviewField label="Vehicle color" value={ticket.vehicleColor} />
            <OverviewField label="Plate number/State" value={plateNumberState(ticket)} />
            <OverviewField label="Violation" value={ticket.violationType} highlight />
            <OverviewField label="Offense (statute)" value={ticket.offenseDisplay} />
            <OverviewField label="Date & time issued" value={issued} />
            <OverviewField label="Location" value={ticket.location} />
            <OverviewField label="Issuing officer" value={ticket.officerName} />
            <OverviewField label="Citation / ticket #" value={ticket.ticketNumber} />
            <OverviewField label="Officer notes" value={ticket.notes} />
          </div>
        </CollapsiblePanel>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        <Link
          to={`/ticket/${ticket.id}/evidence`}
          className="inline-flex items-center justify-center border border-[#0f766e] bg-[#0d9488] px-4 py-2 text-sm font-semibold text-white hover:bg-[#0f766e]"
        >
          View photo evidence
        </Link>
      </div>
    </div>
  )
}
