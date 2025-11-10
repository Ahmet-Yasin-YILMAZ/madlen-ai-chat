export default function MessageBubble({ role, text }) {
  const isUser = role === 'user';
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-2 text-sm shadow
        ${isUser 
          ? 'bg-indigo-500 text-white rounded-br-none' 
          : 'bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-bl-none'}
        `}
      >
        {text}
      </div>
    </div>
  );
}
