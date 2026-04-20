import 'dotenv/config'
import express, { type NextFunction, type Request, type Response } from 'express'
import cors from 'cors'
import { ticketsRouter } from './routes/tickets'
import { uploadRouter } from './routes/upload'
import { initSchema } from './db'
import { seedIfEmpty } from './seed'

const app = express()
const port = Number(process.env.PORT ?? 3000)

const allowedOrigins = (process.env.ALLOWED_ORIGINS ?? 'http://localhost:5173')
  .split(',')
  .map((o) => o.trim())
  .filter(Boolean)

app.use(cors({ origin: allowedOrigins }))
app.use(express.json())

function requireApiKey(req: Request, res: Response, next: NextFunction) {
  const expected = process.env.API_KEY
  if (!expected) return next()
  const key = req.header('x-api-key')
  if (key !== expected) {
    res.status(401).json({ error: 'Invalid or missing API key' })
    return
  }
  next()
}

app.get('/api/health', (_req, res) => {
  res.json({ ok: true, service: 'njcourts-api' })
})

app.use('/api/tickets', ticketsRouter)
app.use('/api/upload', requireApiKey, uploadRouter)

async function start() {
  await initSchema()
  await seedIfEmpty()
  app.listen(port, () => {
    console.log(`[api] listening on http://localhost:${port}`)
    console.log(`[api] allowed CORS origins: ${allowedOrigins.join(', ')}`)
  })
}

start().catch((err) => {
  console.error('[api] startup failed:', err)
  process.exit(1)
})
