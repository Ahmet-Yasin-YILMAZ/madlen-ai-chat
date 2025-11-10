export default function ModelSelect({ models, selectedModel, onChange }) {
  return (
    <select
      value={selectedModel || ''}
      onChange={(e) => onChange(e.target.value)}
      className="border border-slate-200 dark:border-slate-700 rounded-md px-2 py-1 bg-white dark:bg-slate-800 text-sm"
    >
      <option value="">(Backend default)</option>
      {models.map((m) => (
        <option key={m.id} value={m.id}>
          {m.name || m.id}
        </option>
      ))}
    </select>
  );
}
