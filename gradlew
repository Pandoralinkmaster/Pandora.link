#!/bin/sh
#
# Pandora Gradle Wrapper
#

# Bestimme den Script-Ordner
APP_HOME="$(cd "$(dirname "$0")" && pwd)"

# Gradle Wrapper JAR
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Wenn kein JAR vorhanden: direkt über distributions URL herunterladen
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    GRADLE_VERSION="8.11.1"
    GRADLE_DIST="gradle-${GRADLE_VERSION}-bin.zip"
    GRADLE_CACHE="${HOME}/.gradle/wrapper/dists/${GRADLE_VERSION}"
    GRADLE_BIN="${GRADLE_CACHE}/gradle-${GRADLE_VERSION}/bin/gradle"

    if [ ! -f "$GRADLE_BIN" ]; then
        mkdir -p "$GRADLE_CACHE"
        echo "Lade Gradle ${GRADLE_VERSION} herunter..."
        curl -sL "https://services.gradle.org/distributions/${GRADLE_DIST}" \
            -o "${GRADLE_CACHE}/${GRADLE_DIST}"
        unzip -q "${GRADLE_CACHE}/${GRADLE_DIST}" -d "${GRADLE_CACHE}"
    fi

    exec "$GRADLE_BIN" "$@"
fi

# Normal über Java + Wrapper JAR
exec java \
    -classpath "$GRADLE_WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
