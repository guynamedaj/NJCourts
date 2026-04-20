import type { ReactNode } from 'react'

export function CollapsiblePanel({
  title,
  children,
  open,
  onOpenChange,
}: {
  title: string
  children: ReactNode
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  return (
    <div className="border border-gray-200 border-t-0 bg-white first:border-t">
      <div className="flex items-center justify-between gap-2 border-b border-gray-200 bg-[#e8ecef] px-3 py-2">
        <button
          type="button"
          onClick={() => onOpenChange(!open)}
          className="flex min-w-0 flex-1 items-center gap-2 text-left"
          aria-expanded={open}
        >
          <span className="text-gray-700" aria-hidden>
            {open ? '▼' : '▶'}
          </span>
          <span className="text-sm font-bold text-gray-900">{title}</span>
        </button>
        <button
          type="button"
          onClick={() => onOpenChange(!open)}
          className="shrink-0 text-sm font-normal text-[#0d9488] hover:underline"
        >
          Expand / Collapse
        </button>
      </div>
      {open ? <div className="p-4">{children}</div> : null}
    </div>
  )
}
