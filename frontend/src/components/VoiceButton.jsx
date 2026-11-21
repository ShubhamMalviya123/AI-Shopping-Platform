import { useState } from "react";

/** Voice input using the browser's Web Speech API (works in Chrome and Edge). */
export default function VoiceButton({ onText }) {
  const [on, setOn] = useState(false);
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) return null;
  const start = () => {
    const r = new SR();
    r.lang = "en-IN";
    r.onresult = (e) => onText(e.results[0][0].transcript);
    r.onend = () => setOn(false);
    r.onerror = () => setOn(false);
    setOn(true);
    r.start();
  };
  return <button type="button" className={"chip mic" + (on ? " on" : "")} onClick={start}>{on ? "Listening…" : "🎤 Speak"}</button>;
}
