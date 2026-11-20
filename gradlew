#!/usr/bin/env sh
# Minimal wrapper helper. The binary distribution is declared in
# gradle/wrapper/gradle-wrapper.properties; Android Studio downloads it on sync.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle not found. Install Gradle 8.9 or open this project in Android Studio" >&2
echo "(File > Sync Project with Gradle Files) to download the wrapper distribution." >&2
exit 1
