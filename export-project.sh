#!/bin/bash
# Pandora ZIP Export
# Erstellt Pandora.zip mit dem vollständigen Projekt
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
OUTPUT="$PROJECT_ROOT/../Pandora.zip"

echo "◈ PANDORA ZIP EXPORT"
echo "═══════════════════════════════════"
echo "Quelle: $PROJECT_ROOT"
echo "Ziel:   $OUTPUT"
echo ""

# Alte ZIP löschen
rm -f "$OUTPUT"

cd "$(dirname "$PROJECT_ROOT")"
PROJECT_DIR="$(basename "$PROJECT_ROOT")"

# ZIP erstellen (exkl. Build-Artefakte und Cache)
zip -r "$OUTPUT" "$PROJECT_DIR" \
    --exclude "$PROJECT_DIR/.git/*" \
    --exclude "$PROJECT_DIR/pandora-android/app/build/*" \
    --exclude "$PROJECT_DIR/pandora-android/build/*" \
    --exclude "$PROJECT_DIR/pandora-android/.gradle/*" \
    --exclude "$PROJECT_DIR/pandora-android/app/.gradle/*" \
    --exclude "$PROJECT_DIR/dist/*" \
    --exclude "*.class" \
    --exclude "*.dex" \
    --exclude "__pycache__/*" \
    2>/dev/null

SIZE=$(du -h "$OUTPUT" | cut -f1)
FILE_COUNT=$(unzip -l "$OUTPUT" | tail -1 | awk '{print $2}')

echo "✅ FERTIG!"
echo "   Datei: $OUTPUT"
echo "   Größe: $SIZE"
echo "   Dateien: $FILE_COUNT"
echo ""
echo "Entpacken und öffnen:"
echo "   unzip Pandora.zip"
echo "   → pandora/pandora-android in Android Studio öffnen"
echo ""
echo "APK bauen:"
echo "   cd pandora/pandora-android && ./gradlew assembleDebug"
