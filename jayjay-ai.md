# JayJay AI – Technische Dokumentation

## Übersicht

JayJay ist die persönliche KI-Steuerung von **Finn Jona Lischke**.  
JayJay ist weiblich. JayJay gehört Finn.  
JayJay hört **ausschließlich** auf Finn's Stimme.

---

## Architektur

```
Mikrofon (immer aktiv)
    ↓
VAD (Voice Activity Detection)
    → RMS-Analyse (CPU-schonend, 50ms-Polling)
    ↓
CEO-Stimme-Erkennung (lokal, kein Netzwerk)
    → MFCC-Vektorvergleich mit Voice-Print
    → Konfidenz-Schwelle: 82%
    ↓
Wake-Word-Erkennung ("Pandemonium")
    ↓
Speech-to-Text (OpenAI Whisper, 16kHz, 3 Sek)
    ↓
Befehlsverarbeitung:
    → Gelernter Befehl? → Direktausführung
    → Systembefehl? → direkt (Status, Mesh, Lock)
    → Wissensfrage? → aus Wissensbasis
    → Alles andere → OpenAI GPT-4o-mini
    ↓
Antwort + Speichern (JayJay-Task in DB)
```

---

## Stimm-Erkennung

### Einlern-Phase
- Minimum **3 Sprach-Samples** (empfohlen: 5)
- Jedes Sample: beliebiger Text (z.B. "Pandemonium", Zählen 1-10)
- MFCC-Extraktion → 39 Koeffizienten × 50 Frames
- Mittelwert-Vektorberechnung → Voice-Print
- Gespeichert: `/Pandora/jayjay/ceo_voice_print.json` (AES-256 verschlüsselt)

### Erkennungs-Phase (auf jedem Gerät)
- Voice-Print wird via Mesh an alle Pandora-Geräte verteilt
- Lokale Erkennung — kein Netzwerk notwendig
- Kosinus-Ähnlichkeit zwischen aktuellem Audio und Voice-Print
- Threshold: 0.82 (einstellbar)

### Gesperrter Bildschirm
- `JayJayVoiceService` läuft als `Foreground Service` mit `foregroundServiceType="microphone"`
- `START_STICKY` — wird automatisch neu gestartet
- Autostart nach Boot via `BootReceiver`
- VAD schläft 99% der Zeit — minimaler Akkuverbrauch

---

## Lern-System

### Direktes Lernen
```
"JayJay, lerne: [Thema] ist [Inhalt]"
```
→ Sofort in Wissensbasis gespeichert  
→ `/Pandora/jayjay/jayjay_memory.json`

### Web-Recherche
```
"JayJay, recherchiere über [Thema]"
```
1. Wikipedia (de.wikipedia.org) → Artikel-Zusammenfassung
2. GPT-4o-mini → Präzise Zusammenfassung auf Deutsch
3. Gespeichert mit Konfidenz-Score + Quelle
4. Thema wird in `interestTopics` aufgenommen

### Befehle lernen
```
"JayJay, wenn ich '[Trigger]' sage, dann [Aktion]"
```
→ Trigger wird dauerhaft erkannt und führt Aktion aus

### Auto-Lernen
- Läuft **stündlich** im Hintergrund
- Aktualisiert alle bekannten Themen von Finn
- Max. 5 Themen pro Stunde (Rate-Limiting)
- Läuft nur wenn Pandora aktiv ist

---

## Daten auf M.2 SSD (exFAT)

```
/Pandora/jayjay/
├── ceo_voice_print.json      ← Stimm-Fingerabdruck (AES-256)
├── jayjay_memory.json        ← Wissensbasis + Befehle + Präferenzen
└── tasks/                    ← Aufgaben-Log
```

---

## Sicherheitsregeln

- ❌ Keine Nutzung ohne CEO-Freigabe (HostVisibilityGate)
- ❌ Keine vollständige Datenbank an Clients
- ❌ Keine geheimen Schlüssel verteilen
- ❌ Kein anderer Nutzer kann JayJay Befehle geben
- ✅ Nur notwendige Datenfragmente an Compute-Nodes
- ✅ Verschlüsselte Aufgabenpakete
- ✅ Ergebnisse zurück an CEO-Handy

---

## OpenAI Integration

| Dienst | Modell | Verwendung |
|--------|--------|------------|
| Chat | gpt-4o-mini | Alle Konversationen |
| STT | whisper-1 | Spracherkennung (16kHz, de) |
| (Optional) | gpt-4o | Komplexe Analysen (manuell aktivierbar) |

**API-Key:** `OPENAI_API_KEY` Umgebungsvariable
