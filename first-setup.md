# Pandora – Erster Start

## Voraussetzungen

- Samsung Galaxy S24 Ultra (CEO Host)
- M.2 SSD, **formatiert als exFAT** (USB-C OTG-Adapter)
- Android Studio Koala+ (zum APK-Bauen)
- Optional: Ledger Nano S/X/S+ für Bitcoin
- Optional: Orbot (Tor-App) aus dem F-Droid / Play Store

---

## Schritt-für-Schritt

### 1. APK bauen

```bash
cd pandora/pandora-android
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### 2. Installieren

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Oder: APK-Datei auf das Gerät kopieren und manuell installieren (Unbekannte Quellen erlauben).

### 3. Modus wählen

Beim ersten Start erscheint der Setup-Bildschirm:

→ **CEO Host Mode** wählen (Samsung Galaxy S24 Ultra)

### 4. CEO registrieren

- Name: `Finn Jona Lischke` (vorausgefüllt)
- CEO-PIN: mindestens 6 Stellen (merken! – ist der Host-Schlüssel)

### 5. M.2 SSD (exFAT) freigeben

1. SSD über USB-C OTG-Adapter anschließen
2. "📂 SSD auswählen" tippen
3. Im Android-Datei-Browser: SSD-Laufwerk öffnen
4. Auf "Diesen Ordner verwenden" tippen
5. Berechtigung bestätigen

**Was passiert automatisch:**
```
/Pandora/                 ← Pandora-Stammordner
/Pandora/master_config.json
/Pandora/database/        ← DB-Backups (SQLite liegt intern)
/Pandora/receipts/
/Pandora/products/
/Pandora/logs/
/Pandora/backups/         ← Automatische DB-Backups alle 15 Min
/Pandora/jayjay/          ← JayJay Voice-Print + Wissensbasis
/Pandora/wallet/          ← Bitcoin-Wallet (AES-256 verschlüsselt)
... + weitere Ordner
```

> **exFAT-Hinweis:** SQLite (Room-Datenbank) liegt im internen App-Speicher, da exFAT kein Datei-Locking für SQLite unterstützt. Alle anderen Pandora-Daten werden direkt auf der SSD gespeichert.

### 6. JayJay Voice-Print einlernen

1. JayJay-Tab öffnen → "Stimme" Tab
2. "Stimme einlernen" tippen
3. 3 Sprach-Samples aufnehmen (z.B. "Pandemonium", Zählungen, etc.)
4. Voice-Print wird auf SSD gespeichert und an alle Geräte verteilt

**Ab jetzt:**
- Sage "**Pandemonium**" auf **jedem** Pandora-Gerät
- JayJay erkennt dich und hört nur auf dich — auch bei gesperrtem Bildschirm

### 7. Host aktivieren

- Dashboard → "Entsperren" (PIN oder Biometrie)
- Host ist jetzt aktiv — andere Geräte können sich verbinden

---

## Weitere Geräte koppeln

1. `Pandora.apk` auf dem anderen Gerät installieren
2. Gewünschten Modus wählen (Client, Mesh-Node, etc.)
3. CEO-Host muss aktiv sein
4. Gerät erscheint im Admin-Panel → CEO autorisiert es

---

## Befehle für JayJay (Beispiele)

```
"Pandemonium"                              → JayJay aktivieren
"JayJay, was ist der Status?"             → Netzwerk-Status
"JayJay, recherchiere über Bitcoin"       → Auto-Recherche + Lernen
"JayJay, lerne: Mesh hat 3 Knoten"        → Direktes Wissen speichern
"JayJay, wenn ich 'Zahlen' sage, zeig die Einnahmen"
"JayJay, sperre den Host"                 → Host sofort sperren
"JayJay, Mesh-Status"                     → Alle Netzwerkknoten
```

---

## Troubleshooting

| Problem | Lösung |
|---------|--------|
| SSD wird nicht erkannt | OTG-Adapter prüfen, exFAT neu formatieren |
| JayJay hört nicht | Mikrofon-Berechtigung erteilen |
| Host immer gesperrt | CEO-PIN eingeben oder Voice-Print einlernen |
| Andere Geräte sehen "host_offline" | Host entsperren im Dashboard |
| Orbot nicht aktiv | Orbot-App installieren + VPN-Berechtigung erteilen |
