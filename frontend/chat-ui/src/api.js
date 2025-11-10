// src/api.js
const BASE = "http://localhost:8083";

// --- modeller ---
export async function fetchModels() {
  const res = await fetch(`${BASE}/api/chat/models`);
  if (!res.ok) {
    throw new Error("Modeller alınamadı");
  }
  return res.json();
}

// --- mesaj gönder (JSON) ---
export async function sendMessage(body) {
  const res = await fetch(`${BASE}/api/chat/message`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    throw new Error("Mesaj gönderilemedi");
  }

  // backend ChatReplyDto dönüyor:
  // { sessionId, reply, createdAt }
  return res.json();
}

// --- dosyalı mesaj ---
export async function sendMessageWithFile({ file, message, model, sessionId }) {
  const fd = new FormData();
  fd.append("file", file);
  fd.append("message", message);
  if (model) fd.append("model", model);
  if (sessionId) fd.append("sessionId", sessionId);

  const res = await fetch(`${BASE}/api/chat/message-with-file`, {
    method: "POST",
    body: fd,
  });
  if (!res.ok) {
    throw new Error("Dosyalı mesaj gönderilemedi");
  }
  return res.json(); // bu endpoint ChatResponse dönüyor, onda response alanı var
}

// --- oturumlar ---
export async function fetchSessions() {
  const res = await fetch(`${BASE}/api/sessions`);
  if (!res.ok) {
    throw new Error("Oturumlar alınamadı");
  }
  return res.json();
}

export async function createSession() {
  const res = await fetch(`${BASE}/api/sessions`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
  if (!res.ok) {
    throw new Error("Oturum oluşturulamadı");
  }
  return res.json();
}

export async function renameSession(id, name) {
  const res = await fetch(`${BASE}/api/sessions/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name }),
  });
  if (!res.ok) {
    throw new Error("Oturum adı değiştirilemedi");
  }
}

export async function deleteSession(id) {
  const res = await fetch(`${BASE}/api/sessions/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) {
    throw new Error("Oturum silinemedi");
  }
}

// --- seçilen oturumun mesajları ---
export async function fetchSessionMessages(sessionId) {
  const res = await fetch(`${BASE}/api/sessions/${sessionId}/messages`);

  // oturum yoksa frontend’i patlatma
  if (res.status === 404) {
    return [];
  }

  if (!res.ok) {
    throw new Error("Mesajlar alınamadı");
  }

  // bazı durumlarda body boş olabilir
  const text = await res.text();
  if (!text) {
    return [];
  }

  return JSON.parse(text);
}
