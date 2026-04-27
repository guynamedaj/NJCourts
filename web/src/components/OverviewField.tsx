export function OverviewField({
  label,
  value,
  highlight,
}: {
  label: string
  value: string
  highlight?: boolean
}) {
  return (
    <div className="min-w-0">
      <p className="text-xs font-normal text-gray-600">{label}</p>
      <p
        className={`mt-0.5 break-words text-sm leading-tight ${
          highlight ? 'font-medium text-[#0d9488]' : 'font-normal text-gray-900'
        }`}
      >
        {value}
      </p>
    </div>
  )
}
