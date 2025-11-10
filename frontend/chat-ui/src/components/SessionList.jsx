export default function SessionList({ sessions, activeSessionId, onSelect }) {
  return (
    <div className="space-y-2">
      <div className="text-xs font-semibold text-slate-500 dark:text-slate-300 uppercase tracking-wide">
        Oturumlar
      </div>
      <div className="flex gap-2 flex-wrap">
        {sessions.map((s) => (
          <button
            key={s.id}
            onClick={() => onSelect(s.id)}
            className={
              'px-3 py-1 rounded-md text-sm ' +
              (s.id === activeSessionId
                ? 'bg-indigo-500 text-white'
                : 'bg-slate-200 dark:bg-slate-800 text-slate-800 dark:text-slate-100')
            }
          >
            {s.label}
          </button>
        ))}
      </div>
    </div>
  );
}
