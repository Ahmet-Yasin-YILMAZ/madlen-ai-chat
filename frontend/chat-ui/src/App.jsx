import React, { useEffect, useState } from "react";
import {
  fetchModels,
  sendMessage,
  fetchSessions,
  renameSession,
  deleteSession,
  fetchSessionMessages,
  sendMessageWithFile,
  createSession,
} from "./api";

function App() {
  const [models, setModels] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [activeSessionId, setActiveSessionId] = useState(null);
  const [selectedModel, setSelectedModel] = useState("");
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [dark, setDark] = useState(true);
  const [loading, setLoading] = useState(false);

  // modelleri al
  useEffect(() => {
    (async () => {
      try {
        const data = await fetchModels();
        setModels(data);
      } catch (e) {
        console.warn("Model listesi alınamadı, boş liste kullanılacak");
        setModels([]);
      }
    })();
  }, []);

  // oturumları al
  const loadSessions = async () => {
    try {
      const data = await fetchSessions();
      setSessions(data);
    } catch (e) {
      console.warn("Oturumlar alınamadı:", e);
    }
  };

  useEffect(() => {
    loadSessions();
  }, []);

  // bir oturumu seçince mesajlarını da çek
  const handleSelectSession = async (session) => {
    setActiveSessionId(session.id);
    try {
      const msgs = await fetchSessionMessages(session.id);
      setMessages(
        msgs.map((m) => ({
          role: m.sender === "USER" ? "user" : "assistant",
          content: m.content,
        }))
      );
    } catch (e) {
      console.warn("Mesajlar alınamadı:", e);
      setMessages([]);
    }
  };

  // sadece metin gönder
  const handleSend = async () => {
    if (!input.trim()) return;
    setLoading(true);

    // kullanıcı mesajını ekrana koy
    setMessages((prev) => [
      ...prev,
      { role: "user", content: input, sessionId: activeSessionId },
    ]);

    try {
      const res = await sendMessage({
        message: input,
        model: selectedModel,
        sessionId: activeSessionId,
      });
      // backend -> { sessionId, reply, createdAt }

      // aktif oturumu backend'in döndürdüğüyle güncelle
      if (res.sessionId) {
        setActiveSessionId(res.sessionId);
      }

      // oturum listesi değişmiş olabilir, tekrar çek
      await loadSessions();

      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: res.reply ?? "Cevap alınamadı",
          sessionId: res.sessionId,
        },
      ]);
    } catch (e) {
      console.error(e);
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: "Bir hata oluştu",
          sessionId: activeSessionId,
        },
      ]);
    } finally {
      setInput("");
      setLoading(false);
    }
  };

  // dosya seçilince multipart gönder
  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // ekrana gösterelim
    setMessages((prev) => [
      ...prev,
      { role: "user", content: `📎 ${file.name}`, sessionId: activeSessionId },
    ]);

    try {
      const res = await sendMessageWithFile({
        file,
        message: input || "Dosya yüklendi.",
        model: selectedModel,
        sessionId: activeSessionId,
      });

      // bu endpoint eski ChatResponse dönüyor, onda response alanı var
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: res.response ?? "Dosya işlendi.",
          sessionId: res.sessionId ?? activeSessionId,
        },
      ]);

      // yine oturum listesi değişmiş olabilir
      await loadSessions();
    } catch (err) {
      console.error(err);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "Dosya gönderirken hata oldu." },
      ]);
    } finally {
      e.target.value = "";
    }
  };

  const handleRenameSession = async (session) => {
    const newName = prompt("Yeni oturum adı:", session.name || "");
    if (!newName) return;
    await renameSession(session.id, newName);
    await loadSessions(); // arayüzde güncellensin
  };

  const handleDeleteSession = async (session) => {
    if (!window.confirm("Bu oturumu silmek istiyor musun?")) return;
    await deleteSession(session.id);
    await loadSessions();
    if (activeSessionId === session.id) {
      setActiveSessionId(null);
      setMessages([]);
    }
  };

  return (
    <div
      className={
        dark
          ? "bg-slate-900 text-slate-100 h-screen flex"
          : "bg-slate-100 text-slate-900 h-screen flex"
      }
    >
      {/* Sidebar - oturumlar */}
      <div className="w-64 border-r border-slate-700 flex flex-col">
        <div className="flex items-center justify-between px-4 py-3">
          <span className="font-bold">Oturumlar</span>
          <button
            onClick={() => setDark((d) => !d)}
            className="text-xs px-2 py-1 rounded bg-slate-500/30"
          >
            {dark ? "Light" : "Dark"}
          </button>
        </div>

        {/* yeni oturum butonu */}
        <div className="px-4 pb-2">
          <button
            onClick={async () => {
              try {
                const s = await createSession();
                setActiveSessionId(s.id);
                setMessages([]);
                await loadSessions();
              } catch (e) {
                console.warn("Yeni oturum oluşturulamadı", e);
              }
            }}
            className="w-full text-sm bg-emerald-600/70 rounded py-1 mb-2"
          >
            + Yeni oturum
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {sessions.length === 0 && (
            <div className="px-4 text-sm text-slate-400">
              Henüz oturum yok. Mesaj yazınca oluşur.
            </div>
          )}
          {sessions.map((s) => (
            <div
              key={s.id}
              className={`px-4 py-2 text-sm flex items-center justify-between cursor-pointer ${
                activeSessionId === s.id ? "bg-slate-700/40" : ""
              }`}
              onClick={() => handleSelectSession(s)}
            >
              <span className="truncate">{s.name || "Adsız oturum"}</span>
              <div className="flex gap-1">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleRenameSession(s);
                  }}
                  className="text-xs"
                >
                  ✏️
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteSession(s);
                  }}
                  className="text-xs text-red-400"
                >
                  🗑
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Main chat area */}
      <div className="flex-1 flex flex-col">
        {/* üst bar: model seçimi */}
        <div className="border-b border-slate-700 px-4 py-2 flex items-center gap-3">
          <span className="text-sm">Model:</span>
          <select
            value={selectedModel}
            onChange={(e) => setSelectedModel(e.target.value)}
            className={
              dark
                ? "bg-slate-800 border border-slate-600 rounded px-2 py-1 text-sm"
                : "bg-white border rounded px-2 py-1 text-sm"
            }
          >
            <option value="">(backend default)</option>
            {models.length === 0 && (
              <option disabled>Backend model döndürmedi</option>
            )}
            {models.map((m) => (
              <option key={m.id} value={m.id}>
                {m.name != null ? `${m.name} (${m.id})` : m.id}
              </option>
            ))}
          </select>
        </div>

        {/* mesaj alanı */}
        <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-2">
          {messages.map((msg, idx) => (
            <div
              key={idx}
              className={
                msg.role === "user"
                  ? "self-end bg-blue-500 text-white px-3 py-2 rounded-lg max-w-[70%]"
                  : "self-start bg-slate-700/50 px-3 py-2 rounded-lg max-w-[70%]"
              }
            >
              {msg.content}
            </div>
          ))}
          {loading && (
            <div className="text-xs text-slate-400">Yazıyor...</div>
          )}
        </div>

        {/* input */}
        <div className="border-t border-slate-700 p-3 flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
            className={
              dark
                ? "flex-1 bg-slate-800 rounded px-3 py-2"
                : "flex-1 bg-white rounded px-3 py-2"
            }
            placeholder="Mesaj yaz..."
          />
          {/* dosya */}
          <label className="px-3 py-2 bg-slate-600/40 rounded cursor-pointer text-sm">
            📎
            <input type="file" className="hidden" onChange={handleFileChange} />
          </label>
          <button
            onClick={handleSend}
            disabled={loading}
            className="px-4 py-2 bg-emerald-500 rounded text-sm"
          >
            Gönder
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;
