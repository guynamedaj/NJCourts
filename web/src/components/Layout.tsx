import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'

export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-[#eef2f5] text-gray-900">
      <header className="border-b border-gray-400 bg-[#d6e1e8]">
        <div className="mx-auto max-w-[1200px] px-4 py-2.5 sm:px-5">
          <h1 className="text-center text-[15px] font-bold leading-tight text-black sm:text-left sm:text-base">
            <Link to="/" className="text-black hover:text-[#0d9488]">
              Person Case Search and Manage (PCSAM) - Full version
            </Link>
          </h1>
        </div>
      </header>
      <main className="mx-auto w-full max-w-[1200px] flex-1 px-4 py-4 sm:px-5 sm:py-5">
        {children}
      </main>
      <footer className="border-t border-gray-300 bg-white py-2 text-center text-[11px] text-gray-500">
        Mock data — replace <code className="text-gray-700">mockData.ts</code> with production API
        calls.
      </footer>
    </div>
  )
}
