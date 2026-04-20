import { useNavigate } from 'react-router-dom'
import { useAppState } from '../context/AppStateContext'
import type { SortKey } from '../types'

function SortHeader({
  label,
  column,
  currentKey,
  dir,
  onSort,
}: {
  label: string
  column: SortKey
  currentKey: SortKey
  dir: 'asc' | 'desc'
  onSort: (k: SortKey) => void
}) {
  const active = currentKey === column
  return (
    <th scope="col" className="px-3 py-3 text-left">
      <button
        type="button"
        onClick={() => onSort(column)}
        className={`group flex items-center gap-1 text-xs font-semibold uppercase tracking-wide ${
          active ? 'text-[#0f766e]' : 'text-gray-700 hover:text-gray-900'
        }`}
      >
        {label}
        {active && (
          <span className="text-[#0d9488]" aria-hidden>
            {dir === 'asc' ? '↑' : '↓'}
          </span>
        )}
        <span className="sr-only">
          {active ? `sorted ${dir === 'asc' ? 'ascending' : 'descending'}` : 'sort'}
        </span>
      </button>
    </th>
  )
}

export function SearchTicketsPage() {
  const navigate = useNavigate()
  const {
    filters,
    setFilters,
    resetFilters,
    filteredTickets,
    sortKey,
    sortDir,
    setSort,
    violationTypes,
    loading,
    error,
  } = useAppState()

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-base font-bold text-black sm:text-lg">Case search</h2>
        <p className="mt-1 text-sm text-gray-600">
          Search citations (court code 1214, prefix T01/T90, sequence 26xxxx), then open a row for
          case summary and evidence. Defendant identity is rarely known at issuance for parking.
        </p>
      </div>

      <section className="overflow-hidden border border-gray-300 bg-white">
        <div className="border-b border-gray-300 bg-[#e8ecef] px-4 py-2">
          <h3 className="text-sm font-bold text-gray-900">Filters</h3>
        </div>
        <div className="grid gap-4 p-4 sm:grid-cols-2 lg:grid-cols-3 sm:p-5">
          <label className="block text-xs font-medium text-gray-600">
            Keyword search
            <input
              type="search"
              value={filters.keyword}
              onChange={(e) => setFilters({ keyword: e.target.value })}
              placeholder="Court, prefix, sequence, plate, violation, notes…"
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm placeholder:text-gray-400 focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
          <label className="block text-xs font-medium text-gray-600">
            Ticket number
            <input
              type="text"
              value={filters.ticketNumber}
              onChange={(e) => setFilters({ ticketNumber: e.target.value })}
              placeholder="e.g. 1214 T90 260001"
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm placeholder:text-gray-400 focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
          <label className="block text-xs font-medium text-gray-600">
            License plate
            <input
              type="text"
              value={filters.licensePlate}
              onChange={(e) => setFilters({ licensePlate: e.target.value })}
              placeholder="Partial match OK"
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm placeholder:text-gray-400 focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
          <label className="block text-xs font-medium text-gray-600">
            Violation type
            <select
              value={filters.violationType}
              onChange={(e) => setFilters({ violationType: e.target.value })}
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            >
              <option value="">All types</option>
              {violationTypes.map((v) => (
                <option key={v} value={v}>
                  {v}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-xs font-medium text-gray-600">
            Vehicle (make / model / color)
            <input
              type="text"
              value={filters.vehicle}
              onChange={(e) => setFilters({ vehicle: e.target.value })}
              placeholder="e.g. Honda Civic silver"
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm placeholder:text-gray-400 focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
          <label className="block text-xs font-medium text-gray-600">
            Issued from
            <input
              type="date"
              value={filters.dateFrom}
              onChange={(e) => setFilters({ dateFrom: e.target.value })}
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
          <label className="block text-xs font-medium text-gray-600">
            Issued to
            <input
              type="date"
              value={filters.dateTo}
              onChange={(e) => setFilters({ dateTo: e.target.value })}
              className="mt-1 w-full border border-gray-400 bg-white px-3 py-2 text-sm text-gray-900 shadow-sm focus:border-[#0d9488] focus:outline-none focus:ring-1 focus:ring-[#0d9488]"
            />
          </label>
        </div>
        <div className="flex flex-wrap gap-2 border-t border-gray-300 px-4 py-3 sm:px-5">
          <button
            type="button"
            onClick={resetFilters}
            className="border border-gray-400 bg-white px-3 py-1.5 text-xs font-medium text-gray-800 hover:bg-gray-50"
          >
            Clear filters
          </button>
        </div>
      </section>

      <section className="overflow-hidden border border-gray-300 bg-white">
        <div className="border-b border-gray-300 bg-[#e8ecef] px-4 py-2.5">
          <p className="text-sm font-bold text-gray-900">
            Results{' '}
            <span className="font-normal text-gray-500">({filteredTickets.length})</span>
          </p>
        </div>
        {loading ? (
          <div className="px-6 py-16 text-center">
            <p className="text-sm text-gray-600">Loading tickets…</p>
          </div>
        ) : error ? (
          <div className="px-6 py-16 text-center">
            <p className="text-sm font-medium text-red-700">Failed to load tickets</p>
            <p className="mt-2 text-sm text-gray-600">{error}</p>
          </div>
        ) : filteredTickets.length === 0 ? (
          <div className="px-6 py-16 text-center">
            <p className="text-sm font-medium text-gray-800">No results found</p>
            <p className="mt-2 text-sm text-gray-600">
              Adjust your filters or clear them to see all sample citations.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-300 text-left text-sm">
              <thead className="bg-[#e8ecef]">
                <tr>
                  <SortHeader
                    label="Ticket #"
                    column="ticketNumber"
                    currentKey={sortKey}
                    dir={sortDir}
                    onSort={setSort}
                  />
                  <SortHeader
                    label="Date / time"
                    column="issuedAt"
                    currentKey={sortKey}
                    dir={sortDir}
                    onSort={setSort}
                  />
                  <SortHeader
                    label="Plate"
                    column="licensePlate"
                    currentKey={sortKey}
                    dir={sortDir}
                    onSort={setSort}
                  />
                  <SortHeader
                    label="Violation"
                    column="violationType"
                    currentKey={sortKey}
                    dir={sortDir}
                    onSort={setSort}
                  />
                  <SortHeader
                    label="Vehicle"
                    column="vehicle"
                    currentKey={sortKey}
                    dir={sortDir}
                    onSort={setSort}
                  />
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 bg-white">
                {filteredTickets.map((t) => (
                  <tr
                    key={t.id}
                    tabIndex={0}
                    onClick={() => navigate(`/ticket/${t.id}`)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        navigate(`/ticket/${t.id}`)
                      }
                    }}
                    className="cursor-pointer hover:bg-[#e8f5f4] focus:bg-[#d9efed] focus:outline-none"
                  >
                    <td className="whitespace-nowrap px-3 py-2.5 font-mono text-xs font-medium text-[#0d9488]">
                      {t.ticketNumber}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-gray-700">
                      {new Date(t.issuedAt).toLocaleString(undefined, {
                        dateStyle: 'short',
                        timeStyle: 'short',
                      })}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 font-mono text-xs text-gray-700">
                      {t.licensePlate}
                    </td>
                    <td className="px-3 py-2.5 text-gray-700">{t.violationType}</td>
                    <td className="px-3 py-2.5 text-gray-700">
                      {t.vehicleMake} {t.vehicleColor} {t.vehicleModel}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
