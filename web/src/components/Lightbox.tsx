import { useEffect } from 'react'
import type { PhotoEvidence } from '../types'

interface LightboxProps {
  photo: PhotoEvidence | null
  onClose: () => void
}

export function Lightbox({ photo, onClose }: LightboxProps) {
  useEffect(() => {
    if (!photo) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [photo, onClose])

  if (!photo) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/85 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-label="Photo evidence full view"
      onClick={onClose}
    >
      <div
        className="relative max-h-[90vh] max-w-5xl overflow-hidden rounded-lg border border-slate-600 bg-slate-900 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-2 top-2 z-10 border border-teal-500 bg-teal-700 px-2 py-1 text-xs font-medium text-white hover:bg-teal-800"
        >
          Close
        </button>
        <img
          src={photo.previewUrl}
          alt={photo.fileName}
          className="max-h-[80vh] w-full object-contain"
        />
        <div className="border-t border-slate-700 bg-slate-900 px-4 py-3 text-left text-xs text-slate-300">
          <p className="font-medium text-slate-100">{photo.fileName}</p>
          <p className="mt-1 break-all font-mono text-[11px] text-slate-400">{photo.s3Url}</p>
          <p className="mt-1 text-slate-500">
            Uploaded{' '}
            {new Date(photo.uploadTimestamp).toLocaleString(undefined, {
              dateStyle: 'medium',
              timeStyle: 'short',
            })}
          </p>
        </div>
      </div>
    </div>
  )
}
