import { useRef } from 'react'
import { Link } from 'react-router-dom'

const tabs: {
  key: string
  label: string
  to?: (id: string) => string
  disabled?: boolean
}[] = [
  { key: 'summary', label: 'Case Summary', to: (id) => `/ticket/${id}` },
  { key: 'charges', label: 'Charges Disposition & Sentences', disabled: true },
  { key: 'payments', label: 'Payments', disabled: true },
  { key: 'notes', label: 'Notes', disabled: true },
  { key: 'audit', label: 'Audit History', disabled: true },
  { key: 'warrant', label: 'Warrant', disabled: true },
  { key: 'bail', label: 'Bail', disabled: true },
  { key: 'party', label: 'Party Information', disabled: true },
  { key: 'evidence', label: 'Photo evidence', to: (id) => `/ticket/${id}/evidence` },
]

const activeTabClass =
  'bg-[#0d9488] text-white shadow-[inset_0_-2px_0_0_rgba(0,0,0,0.08)]'
const inactiveTabClass = 'bg-white text-gray-800 hover:bg-gray-50'

export function TicketTabBar({
  ticketId,
  active,
}: {
  ticketId: string
  active: 'summary' | 'evidence'
}) {
  const scrollerRef = useRef<HTMLDivElement>(null)

  const scroll = (dir: -1 | 1) => {
    scrollerRef.current?.scrollBy({ left: dir * 200, behavior: 'smooth' })
  }

  return (
    <div className="flex items-stretch border border-gray-300 bg-white">
      <button
        type="button"
        onClick={() => scroll(-1)}
        className="hidden shrink-0 border-r border-gray-300 bg-[#f0f3f5] px-2 text-base text-gray-700 hover:bg-[#e5e9ec] sm:inline-flex sm:items-center"
        aria-label="Scroll tabs left"
      >
        ‹
      </button>
      <div
        ref={scrollerRef}
        className="flex min-w-0 flex-1 overflow-x-auto whitespace-nowrap"
        role="tablist"
      >
        {tabs.map((t) => {
          const isActive =
            (t.key === 'summary' && active === 'summary') ||
            (t.key === 'evidence' && active === 'evidence')

          if (t.disabled) {
            return (
              <span
                key={t.key}
                role="tab"
                aria-selected={false}
                aria-disabled
                className="inline-flex shrink-0 items-center border-r border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-600"
              >
                {t.label}
              </span>
            )
          }

          const to = t.to!(ticketId)
          return (
            <Link
              key={t.key}
              to={to}
              role="tab"
              aria-selected={isActive}
              className={`inline-flex shrink-0 items-center border-r border-gray-300 px-3 py-2.5 text-sm font-normal last:border-r-0 ${
                isActive ? activeTabClass : inactiveTabClass
              }`}
            >
              {t.label}
            </Link>
          )
        })}
      </div>
      <button
        type="button"
        onClick={() => scroll(1)}
        className="hidden shrink-0 border-l border-gray-300 bg-[#f0f3f5] px-2 text-base text-gray-700 hover:bg-[#e5e9ec] sm:inline-flex sm:items-center"
        aria-label="Scroll tabs right"
      >
        ›
      </button>
    </div>
  )
}
