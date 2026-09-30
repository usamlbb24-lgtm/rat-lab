#!/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"

# Cari gradle wrapper jar
if [ ! -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading gradle-wrapper.jar..."
    mkdir -p "$DIR/gradle/wrapper"
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar" \
        -o "$DIR/gradle/wrapper/gradle-wrapper.jar"
fi

# Eksekusi gradle wrapper
exec java -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
