import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { AppStateProvider } from './context/AppStateContext'
import { SearchTicketsPage } from './pages/SearchTicketsPage'
import { TicketDetailsPage } from './pages/TicketDetailsPage'
import { PhotoEvidencePage } from './pages/PhotoEvidencePage'

function App() {
  return (
    <AppStateProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<SearchTicketsPage />} />
          <Route path="/ticket/:id" element={<TicketDetailsPage />} />
          <Route path="/ticket/:id/evidence" element={<PhotoEvidencePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </AppStateProvider>
  )
}

export default App
