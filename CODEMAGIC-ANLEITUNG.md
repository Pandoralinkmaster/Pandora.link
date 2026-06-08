# Pandora APK – In 5 Minuten fertig

## Was du brauchst
- Einen Browser
- Diese ZIP-Datei

---

## Schritt 1 — GitHub Account (kostenlos)

→ **https://github.com/signup**

Einfach registrieren, kostenlos, keine Kreditkarte.

---

## Schritt 2 — Neues Repository anlegen

1. → **https://github.com/new**
2. Repository name: `pandora`
3. **Private** auswählen ✓
4. Klick auf **"Create repository"**

---

## Schritt 3 — Dateien hochladen

1. Im neuen Repository auf **"uploading an existing file"** klicken
2. Diese ZIP entpacken → den Inhalt des Ordners `pandora-android/` auswählen
3. Alle Dateien reinziehen
4. Unten: **"Commit changes"** klicken

---

## Schritt 4 — Codemagic verbinden

1. → **https://codemagic.io** aufrufen
2. **"Sign up with GitHub"** klicken
3. GitHub-Account auswählen → Zugriff erlauben
4. Dein `pandora` Repository erscheint in der Liste
5. Klick auf **"Set up build"**
6. Codemagic erkennt die `codemagic.yaml` automatisch
7. Klick auf **"Start new build"**

---

## Schritt 5 — APK herunterladen

Der Build dauert ca. **5–8 Minuten**.

Du bekommst eine E-Mail an `finn.jona.lischke@gmail.com` wenn er fertig ist.

1. In Codemagic auf den fertigen Build klicken
2. Unter **"Artifacts"** → `app-debug.apk` herunterladen
3. Datei umbenennen in `Pandora.apk` (optional)

---

## Schritt 6 — Auf Samsung installieren

### Per USB (ADB) — schnellster Weg:
```
adb install -r Pandora.apk
```

### Ohne PC — direkt auf dem Samsung:
1. APK per E-Mail / Google Drive / USB-Stick auf das Handy
2. Datei im Samsung-Dateimanager antippen
3. **"Installieren"** tippen
4. Falls gefragt: Einstellungen → Apps → Sonderrechte → **"Unbekannte Quellen"** einschalten → zurück → Installieren

---

## Fertig ✓

Pandora öffnen →
1. **CEO Host Mode** wählen
2. CEO registrieren (Name + PIN)
3. M.2 SSD (exFAT) anschließen + freigeben
4. **"Pandemonium"** sagen → JayJay ist aktiv
