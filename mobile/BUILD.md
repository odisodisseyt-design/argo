# Сборка ARGO (Expo / React Native)

Это новый клиент ARGO. Старый Kotlin-клиент остаётся в каталоге `../` как резервный.

## Требования

- Node.js 22+, npm 10+
- JDK 17 (`/opt/jdk17/jdk-17.0.21.1+2`)
- Android SDK (`/opt/android-sdk`), platform 35, build-tools 35.0.0
- `EXPO_PUBLIC_API_URL`, `EXPO_PUBLIC_WS_URL` в окружении на этапе сборки

## Подготовка

```sh
npm install
# postinstall автоматически чинит expo-font 58.0.12 (отсутствует version в publication)
EXPO_PUBLIC_API_URL=https://v3310666.hosted-by-vdsina.ru \
EXPO_PUBLIC_WS_URL=wss://v3310666.hosted-by-vdsina.ru \
npx expo prebuild --platform android --no-install
```

Подпись: `android/keystore.properties` и `android/release.jks` копируются из старого клиента.
`versionCode` и `versionName` задаются в `android/app/build.gradle`.

## Сборка

```sh
cd android
ANDROID_HOME=/opt/android-sdk \
JAVA_HOME=/opt/jdk17/jdk-17.0.21.1+2 \
PATH=/opt/jdk17/jdk-17.0.21.1+2/bin:$PATH \
./gradlew assembleRelease --no-daemon
```

APK: `android/app/build/outputs/apk/release/app-release.apk`.

## Проверка перед публикацией

```sh
npx tsc --noEmit
sha256sum android/app/build/outputs/apk/release/app-release.apk
```
