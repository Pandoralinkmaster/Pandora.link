#!/bin/bash
# Pandora Android Build Script
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR/../pandora-android"
OUTPUT_DIR="$SCRIPT_DIR/../dist"

echo "◈ PANDORA BUILD"
echo "═══════════════════════════════════"
echo "Projekt: $PROJECT_DIR"
echo "Ausgabe: $OUTPUT_DIR"
echo ""

# Voraussetzungen prüfen
if ! command -v java &> /dev/null; then
    echo "❌ Java nicht gefunden. JDK 17+ installieren."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 17 ] 2>/dev/null; then
    echo "⚠ Java $JAVA_VER gefunden — JDK 17+ empfohlen"
fi

# Build-Typ aus Argument
BUILD_TYPE="${1:-debug}"
if [ "$BUILD_TYPE" == "release" ]; then
    echo "🔒 Release-Build (APK signieren nicht vergessen!)"
else
    echo "🔧 Debug-Build"
fi

mkdir -p "$OUTPUT_DIR"

cd "$PROJECT_DIR"

# Gradle Wrapper
if [ ! -f "gradlew" ]; then
    echo "❌ gradlew nicht gefunden in $PROJECT_DIR"
    exit 1
fi
chmod +x gradlew

echo ""
echo "▶ Baue Pandora.apk ($BUILD_TYPE)..."
if [ "$BUILD_TYPE" == "release" ]; then
    ./gradlew assembleRelease --no-daemon --info 2>&1 | tail -20
    APK="app/build/outputs/apk/release/app-release-unsigned.apk"
else
    ./gradlew assembleDebug --no-daemon 2>&1 | tail -20
    APK="app/build/outputs/apk/debug/app-debug.apk"
fi

if [ -f "$APK" ]; then
    cp "$APK" "$OUTPUT_DIR/Pandora.apk"
    SIZE=$(du -h "$OUTPUT_DIR/Pandora.apk" | cut -f1)
    echo ""
    echo "✅ FERTIG!"
    echo "   APK: $OUTPUT_DIR/Pandora.apk ($SIZE)"
    echo ""
    echo "Installieren:"
    echo "   adb install -r \"$OUTPUT_DIR/Pandora.apk\""
else
    echo "❌ Build fehlgeschlagen — APK nicht gefunden: $APK"
    exit 1
fi
