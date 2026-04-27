import 'dotenv/config'
import { pool } from './db'

async function reset() {
  console.log('[reset] dropping tables...')
  await pool.query('DROP TABLE IF EXISTS photo_evidence CASCADE')
  await pool.query('DROP TABLE IF EXISTS tickets CASCADE')
  console.log('[reset] done. Restart the server to recreate schema and reseed.')
  await pool.end()
}

reset().catch((err) => {
  console.error('[reset] failed:', err)
  process.exit(1)
})
