#!/bin/sh
# Pandora Gradle Wrapper
# Lädt Gradle automatisch herunter wenn nötig

GRADLE_VERSION=8.11.1
GRADLE_HOME="${HOME}/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

if [ ! -f "${GRADLE_HOME}/gradle-${GRADLE_VERSION}/bin/gradle" ]; then
    mkdir -p "${GRADLE_HOME}"
    curl -sL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
        -o "${GRADLE_HOME}/gradle-${GRADLE_VERSION}-bin.zip"
    unzip -q "${GRADLE_HOME}/gradle-${GRADLE_VERSION}-bin.zip" -d "${GRADLE_HOME}"
fi

exec "${GRADLE_HOME}/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
