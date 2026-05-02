import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'

export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-[#eef2f5] text-gray-900">
      <header className="border-b border-gray-400 bg-[#d6e1e8]">
        <div className="mx-auto max-w-[1200px] px-4 py-2.5 sm:px-5">
          <h1 className="text-center text-[15px] font-bold leading-tight text-black sm:text-left sm:text-base">
            <Link to="/" className="text-black hover:text-[#0d9488]">
              Person Case Search and Manage (PCSAM)
            </Link>
          </h1>
        </div>
      </header>
      <main className="mx-auto w-full max-w-[1200px] flex-1 px-4 py-4 sm:px-5 sm:py-5">
        {children}
      </main>
    </div>
  )
}
