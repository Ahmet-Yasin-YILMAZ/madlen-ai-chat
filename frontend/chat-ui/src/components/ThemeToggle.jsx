import { useEffect, useState } from 'react';

export default function ThemeToggle() {
  const [dark, setDark] = useState(
    () => localStorage.getItem('theme') === 'dark'
  );

  useEffect(() => {
    if (dark) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [dark]);

  return (
    <button
      onClick={() => setDark(!dark)}
      className="inline-flex items-center gap-2 px-3 py-1 rounded-md bg-slate-200 dark:bg-slate-700 text-sm"
    >
      {dark ? '🌙 Gece' : '☀️ Gündüz'}
    </button>
  );
}
