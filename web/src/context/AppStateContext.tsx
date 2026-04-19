import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { mockTickets } from '../mockData'
import type { SortDir, SortKey, Ticket } from '../types'

export interface SearchFilters {
  keyword: string
  ticketNumber: string
  licensePlate: string
  suspectName: string
  violationType: string
  vehicle: string
  dateFrom: string
  dateTo: string
}

const defaultFilters: SearchFilters = {
  keyword: '',
  ticketNumber: '',
  licensePlate: '',
  suspectName: '',
  violationType: '',
  vehicle: '',
  dateFrom: '',
  dateTo: '',
}

function normalizePlate(s: string) {
  return s.replace(/[^a-z0-9]/gi, '').toLowerCase()
}

function ticketMatchesFilters(t: Ticket, f: SearchFilters): boolean {
  const issued = new Date(t.issuedAt)
  if (f.dateFrom) {
    const start = new Date(f.dateFrom)
    start.setHours(0, 0, 0, 0)
    if (issued < start) return false
  }
  if (f.dateTo) {
    const end = new Date(f.dateTo)
    end.setHours(23, 59, 59, 999)
    if (issued > end) return false
  }
  if (f.violationType && t.violationType !== f.violationType) return false
  if (
    f.ticketNumber &&
    !t.ticketNumber.toLowerCase().includes(f.ticketNumber.trim().toLowerCase())
  ) {
    return false
  }
  if (f.licensePlate) {
    const q = normalizePlate(f.licensePlate)
    if (!normalizePlate(t.licensePlate).includes(q)) return false
  }
  if (
    f.suspectName &&
    !t.suspectName.toLowerCase().includes(f.suspectName.trim().toLowerCase())
  ) {
    return false
  }
  if (f.vehicle) {
    const blob = `${t.vehicleMake} ${t.vehicleModel} ${t.vehicleColor}`.toLowerCase()
    if (!blob.includes(f.vehicle.trim().toLowerCase())) return false
  }
  if (f.keyword) {
    const tokens = f.keyword
      .trim()
      .toLowerCase()
      .split(/\s+/)
      .filter(Boolean)
    const hay = [
      t.ticketNumber,
      t.caseDisplay,
      t.licensePlate,
      t.suspectName,
      t.violationType,
      t.offenseDisplay,
      t.courtDisplay,
      t.caseStatus,
      t.vehicleMake,
      t.vehicleModel,
      t.vehicleColor,
      t.location,
      t.officerName,
      t.notes,
      t.defendantAddress,
    ]
      .join(' ')
      .toLowerCase()
    if (tokens.some((tok) => !hay.includes(tok))) return false
  }
  return true
}

function sortTickets(list: Ticket[], key: SortKey, dir: SortDir): Ticket[] {
  const mul = dir === 'asc' ? 1 : -1
  const out = [...list]
  out.sort((a, b) => {
    let cmp = 0
    switch (key) {
      case 'ticketNumber':
        cmp = a.ticketNumber.localeCompare(b.ticketNumber, undefined, {
          numeric: true,
        })
        break
      case 'issuedAt':
        cmp = new Date(a.issuedAt).getTime() - new Date(b.issuedAt).getTime()
        break
      case 'suspectName':
        cmp = a.suspectName.localeCompare(b.suspectName)
        break
      case 'licensePlate':
        cmp = normalizePlate(a.licensePlate).localeCompare(
          normalizePlate(b.licensePlate),
        )
        break
      case 'violationType':
        cmp = a.violationType.localeCompare(b.violationType)
        break
      case 'vehicle': {
        const va = `${a.vehicleMake} ${a.vehicleColor} ${a.vehicleModel}`
        const vb = `${b.vehicleMake} ${b.vehicleColor} ${b.vehicleModel}`
        cmp = va.localeCompare(vb)
        break
      }
      default:
        cmp = 0
    }
    return cmp * mul
  })
  return out
}

interface AppStateValue {
  filters: SearchFilters
  setFilters: (patch: Partial<SearchFilters>) => void
  resetFilters: () => void
  sortKey: SortKey
  sortDir: SortDir
  setSort: (key: SortKey) => void
  filteredTickets: Ticket[]
}

const AppStateContext = createContext<AppStateValue | null>(null)

export function AppStateProvider({ children }: { children: ReactNode }) {
  const [filters, setFiltersState] = useState<SearchFilters>(defaultFilters)
  const [sortKey, setSortKey] = useState<SortKey>('issuedAt')
  const [sortDir, setSortDir] = useState<SortDir>('desc')

  const setFilters = useCallback((patch: Partial<SearchFilters>) => {
    setFiltersState((prev) => ({ ...prev, ...patch }))
  }, [])

  const resetFilters = useCallback(() => {
    setFiltersState(defaultFilters)
  }, [])

  const setSort = useCallback(
    (key: SortKey) => {
      if (sortKey !== key) {
        setSortKey(key)
        setSortDir('asc')
      } else {
        setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))
      }
    },
    [sortKey],
  )

  const filteredTickets = useMemo(() => {
    const filtered = mockTickets.filter((t) => ticketMatchesFilters(t, filters))
    return sortTickets(filtered, sortKey, sortDir)
  }, [filters, sortKey, sortDir])

  const value = useMemo(
    () => ({
      filters,
      setFilters,
      resetFilters,
      sortKey,
      sortDir,
      setSort,
      filteredTickets,
    }),
    [
      filters,
      setFilters,
      resetFilters,
      sortKey,
      sortDir,
      setSort,
      filteredTickets,
    ],
  )

  return (
    <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>
  )
}

export function useAppState() {
  const ctx = useContext(AppStateContext)
  if (!ctx) throw new Error('useAppState must be used within AppStateProvider')
  return ctx
}
