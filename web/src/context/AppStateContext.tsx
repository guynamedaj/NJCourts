import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { getTickets } from '../api/client'
import type { SortDir, SortKey, Ticket } from '../types'

export interface SearchFilters {
  keyword: string
  ticketNumber: string
  licensePlate: string
  violationType: string
  vehicle: string
  dateFrom: string
  dateTo: string
}

const defaultFilters: SearchFilters = {
  keyword: '',
  ticketNumber: '',
  licensePlate: '',
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
      t.courtCode,
      t.ticketPrefix,
      t.sequenceNumber,
      t.caseDisplay,
      t.licensePlate,
      t.defendantName,
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
  allTickets: Ticket[]
  violationTypes: string[]
  loading: boolean
  error: string | null
  refresh: () => void
}

const AppStateContext = createContext<AppStateValue | null>(null)

export function AppStateProvider({ children }: { children: ReactNode }) {
  const [filters, setFiltersState] = useState<SearchFilters>(defaultFilters)
  const [sortKey, setSortKey] = useState<SortKey>('issuedAt')
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const [allTickets, setAllTickets] = useState<Ticket[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    getTickets()
      .then((data) => {
        if (!cancelled) setAllTickets(data)
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load tickets')
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [refreshToken])

  const setFilters = useCallback((patch: Partial<SearchFilters>) => {
    setFiltersState((prev) => ({ ...prev, ...patch }))
  }, [])

  const resetFilters = useCallback(() => {
    setFiltersState(defaultFilters)
  }, [])

  const refresh = useCallback(() => {
    setRefreshToken((n) => n + 1)
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
    const filtered = allTickets.filter((t) => ticketMatchesFilters(t, filters))
    return sortTickets(filtered, sortKey, sortDir)
  }, [allTickets, filters, sortKey, sortDir])

  const violationTypes = useMemo(() => {
    const set = new Set(allTickets.map((t) => t.violationType))
    return [...set].sort((a, b) => a.localeCompare(b))
  }, [allTickets])

  const value = useMemo(
    () => ({
      filters,
      setFilters,
      resetFilters,
      sortKey,
      sortDir,
      setSort,
      filteredTickets,
      allTickets,
      violationTypes,
      loading,
      error,
      refresh,
    }),
    [
      filters,
      setFilters,
      resetFilters,
      sortKey,
      sortDir,
      setSort,
      filteredTickets,
      allTickets,
      violationTypes,
      loading,
      error,
      refresh,
    ],
  )

  return (
    <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>
  )
}

/** Context hook exported alongside provider; see react-refresh/only-export-components */
// eslint-disable-next-line react-refresh/only-export-components -- paired hook for AppStateProvider
export function useAppState() {
  const ctx = useContext(AppStateContext)
  if (!ctx) throw new Error('useAppState must be used within AppStateProvider')
  return ctx
}
