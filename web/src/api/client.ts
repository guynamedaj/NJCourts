import type { PhotoEvidence, Ticket } from '../types'

const baseUrl = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:3000/api'

export class ApiError extends Error {
  status: number
  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string): Promise<T> {
  const res = await fetch(`${baseUrl}${path}`)
  if (!res.ok) {
    throw new ApiError(`API ${path} failed: ${res.status}`, res.status)
  }
  return (await res.json()) as T
}

export function getTickets(): Promise<Ticket[]> {
  return request<Ticket[]>('/tickets')
}

export async function getTicket(id: string): Promise<Ticket | null> {
  try {
    return await request<Ticket>(`/tickets/${encodeURIComponent(id)}`)
  } catch (err) {
    if (err instanceof ApiError && err.status === 404) return null
    throw err
  }
}

export async function getTicketEvidence(id: string): Promise<PhotoEvidence[]> {
  try {
    return await request<PhotoEvidence[]>(`/tickets/${encodeURIComponent(id)}/evidence`)
  } catch (err) {
    if (err instanceof ApiError && err.status === 404) return []
    throw err
  }
}
