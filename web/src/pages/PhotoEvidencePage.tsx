import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getEvidenceForTicket, getTicketById, plateNumberState } from '../mockData'
import { Lightbox } from '../components/Lightbox'
import { OverviewField } from '../components/OverviewField'
import { TicketTabBar } from '../components/TicketTabBar'
import type { PhotoEvidence } from '../types'

export function PhotoEvidencePage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [lightbox, setLightbox] = useState<PhotoEvidence | null>(null)

  const ticket = id ? getTicketById(id) : undefined
  const photos = id ? getEvidenceForTicket(id) : []

  if (!ticket) {
    return (
      <div className="border border-gray-300 bg-white p-8 text-center">
        <h2 className="text-lg font-bold text-gray-900">Ticket not found</h2>
        <button
          type="button"
          onClick={() => navigate('/')}
          className="mt-6 border border-gray-400 bg-[#f0f3f5] px-4 py-2 text-sm font-medium text-gray-900 hover:bg-[#e5e9ec]"
        >
          Go to search
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center gap-2 text-sm">
        <button
          type="button"
          onClick={() => navigate(`/ticket/${ticket.id}`)}
          className="border border-gray-400 bg-white px-3 py-1.5 font-medium text-gray-900 hover:bg-gray-50"
        >
          ← Back to case summary
        </button>
        <Link to="/" className="font-normal text-[#0d9488] hover:underline">
          Case search
        </Link>
      </div>

      <section className="border border-gray-300 bg-white">
        <h2 className="border-b border-gray-300 px-4 py-3 text-base font-bold text-black">
          Photo evidence
        </h2>
        <p className="px-4 py-2 text-xs text-gray-600">
          <span className="font-mono text-gray-900">{ticket.ticketNumber}</span>
          {' · '}
          {ticket.caseDisplay}
          {' · '}
          {photos.length} file{photos.length === 1 ? '' : 's'} · Simulated S3 objects
        </p>
      </section>

      <TicketTabBar ticketId={ticket.id} active="evidence" />

      <section
        className="border border-gray-300 bg-white"
        aria-label="Citation and vehicle summary for this evidence"
      >
        <div className="border-b border-gray-300 bg-[#e8ecef] px-4 py-2">
          <h3 className="text-sm font-bold text-gray-900">Citation & vehicle</h3>
          <p className="text-xs text-gray-600">
            Context for the photos below — same citation as case summary.
          </p>
        </div>
        <div className="grid grid-cols-2 gap-x-6 gap-y-4 px-4 py-4 lg:grid-cols-4">
          <OverviewField
            label="Vehicle"
            value={`${ticket.vehicleColor} ${ticket.vehicleMake} ${ticket.vehicleModel}`}
          />
          <OverviewField label="Plate number / state" value={plateNumberState(ticket)} />
          <OverviewField label="Violation" value={ticket.violationType} highlight />
          <OverviewField label="Offense (statute)" value={ticket.offenseDisplay} />
          <OverviewField
            label="Date & time issued"
            value={new Date(ticket.issuedAt).toLocaleString(undefined, {
              dateStyle: 'short',
              timeStyle: 'short',
            })}
          />
          <OverviewField label="Location" value={ticket.location} />
          <OverviewField label="Defendant" value={ticket.suspectName} />
          <OverviewField label="Citation #" value={ticket.ticketNumber} />
        </div>
      </section>

      {photos.length === 0 ? (
        <div className="border border-dashed border-gray-400 bg-white px-6 py-16 text-center">
          <p className="text-sm font-bold text-gray-900">No photos attached</p>
          <p className="mt-2 text-sm text-gray-600">
            This citation has no evidence objects in the mock store. Try ticket PA-2024-88541 or
            PA-2024-88421 for samples with attachments.
          </p>
        </div>
      ) : (
        <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {photos.map((p) => (
            <li key={p.id}>
              <button
                type="button"
                onClick={() => setLightbox(p)}
                className="w-full overflow-hidden border border-gray-300 bg-white text-left transition hover:border-[#0d9488] focus:outline-none focus:ring-2 focus:ring-[#0d9488] focus:ring-offset-1"
              >
                <div className="aspect-[4/3] w-full overflow-hidden bg-gray-200">
                  <img
                    src={p.previewUrl}
                    alt=""
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="border-t border-gray-100 p-3">
                  <p className="truncate text-sm font-medium text-gray-900">{p.fileName}</p>
                  <p className="mt-1 text-xs text-gray-500">
                    {new Date(p.uploadTimestamp).toLocaleString(undefined, {
                      dateStyle: 'medium',
                      timeStyle: 'short',
                    })}
                  </p>
                  <p className="mt-1 truncate font-mono text-[10px] text-gray-400" title={p.s3Url}>
                    {p.s3Url}
                  </p>
                </div>
              </button>
            </li>
          ))}
        </ul>
      )}

      <Lightbox photo={lightbox} onClose={() => setLightbox(null)} />
    </div>
  )
}
