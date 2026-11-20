# Text World — Android-клиент

Клиентская оболочка для пошаговой текстовой RPG. Приложение **не содержит правил**,
не вызывает LLM, не считает вероятности и не хранит состояние мира. Сервер владеет
логикой; приложение только отправляет сигналы игрока, получает структурированные
события и красиво их отрисовывает.

Репозиторий сервера находится в каталоге `../`. Клиент должен работать с **любым**
паком, который отдаёт сервер: навигация и доступные экраны строятся из `ui_manifest`.

## Технологии

- Kotlin + Jetpack Compose, Material 3, minSdk 26, targetSdk 35.
- Ktor (HTTP/WebSocket/SSE), Coil, Room, Coroutines/Flow, kotlinx.serialization.
- EncryptedSharedPreferences (Android Keystore) для токена.
- Только open-source зависимости. Никаких Firebase, рекламы, аналитики, телеметрии,
  локальных LLM и проприетарных SDK.

## Сборка

Нужны JDK 17 и Android SDK (compileSdk 35).

```bash
cd android
./gradlew :app:assembleDebug      # отладочная APK
./gradlew :app:assembleRelease    # релизная APK (подпись см. ниже)
./gradlew :app:test               # тесты
```

Если `gradlew` отсутствует, сгенерируйте обёртку командой `gradle wrapper --gradle-version 8.9`
или откройте проект в Android Studio (File → Sync Project with Gradle Files).

## Подключение к серверу

Адрес по умолчанию: `https://v3310666.hosted-by-vdsina.ru`. Он задаётся в
`app/build.gradle.kts` как `DEFAULT_SERVER_URL` и может быть изменён в Настройках
(долгое нажатие на обложку → Настройки → «Адрес сервера»). Приложение использует
только HTTPS (`network_security_config.xml` запрещает cleartext).

При запуске выполняется `GET /api/health`:

- если сервер недоступен — экран «Книга закрыта» с кнопкой «Попробовать снова»;
- если `min_client_version` больше `versionCode` — обязательный экран обновления;
- если сохранён токен — тихий вход по нему (пароль на устройстве не хранится).

Вход: `POST /api/auth/login` (или `/api/auth/register`). Токен сохраняется в
`EncryptedSharedPreferences`, расшифровка — через ключ Android Keystore.

Ход: `POST /api/turn` → поток SSE `meta` / `delta` / `final`. Если SSE недоступен,
клиент разбирает обычный JSON-ответ. WebSocket `/ws/campaign/{id}?token=...`
доставляет `turn_started`, `awaiting_orders`, `turn_final`, `presence`, `rollback`,
`error`; при обрыве — экспоненциальный реконнект и long-poll fallback. Повторная
отправка приказа не дублируется: сервер хранит заказы по `(campaign, step, user)`.

## Подпись и OTA

**Имя пакета и ключ подписи должны быть постоянными.** Иначе Android сочтёт новую
APK другим приложением и не сохранит токен, ветки и настройки.

- `applicationId` = `com.textworld.app` (менять нельзя).
- Релизная подпись читается из `keystore.properties` в корне `android/`:

```properties
storeFile=../release.jks
storePassword=ВАШ_ПАРОЛЬ
keyAlias=textworld
keyPassword=ВАШ_ПАРОЛЬ
```

Пока файла нет, сборка использует debug-ключ **только для разработки**. Перед
публикацией создайте własный keystore и больше не меняйте его:

```bash
keytool -genkeypair -v -keystore release.jks -alias textworld \
  -keyalg RSA -keysize 2048 -validity 10000
```

### Самообновление

Опубликуйте `version.json` в корне публичного репозитория (шаблон — `version.json`
в этом каталоге). Экран «О приложении» → «Проверить обновления» сравнит версии,
покажет changelog, скачает APK, проверит SHA-256 и запросит установку через
`Intent.ACTION_VIEW`. Для Android 8+ клиент корректно проводит пользователя через
«Установка неизвестных приложений», а на Android 14+ требует `REQUEST_INSTALL_PACKAGES`.
Данные приложения при обновлении **не очищаются**.

Замените `OWNER/REPO` в `app/build.gradle.kts` (`UPDATE_VERSION_URL`, `UPDATE_REPO_URL`)
на свой репозиторий до релиза.

## Приватность

- Токен — в зашифрованных preferences, пароль не сохраняется.
- Никаких аналитических/рекламных SDK и telemetry.
- `client-log` отправляет только короткие тексты ошибок, никогда — промпты, логи
  и внутренние идентификаторы.
- Обновление проверяется по SHA-256 до установки.

## Структура

```
app/src/main/java/com/textworld/app/
  core/            константы, Outcome
  data/
    model/         whitelist DTO (только чтение того, что прислал сервер)
    network/       Ktor: REST, SSE, WebSocket, self-update
    token/         EncryptedSharedPreferences
    settings/      настройки + активная кампания
    repository/    единое состояние UI
  di/              ручной DI-контейнер
  presentation/
    renderer/      PresentationRenderer — единственная точка разбора presentation
    theme/         тёплая «Книга»
    components/    частицы, параллакс, виньетка, системное окно, микро-анимации
    gateways/      Voice / Image / AmbientAudio / Haptics (по умолчанию выключены)
  ui/              MainViewModel и экраны
```

Подробности — в [ARCHITECTURE.md](ARCHITECTURE.md).

## Лицензия

MIT, см. [LICENSE](LICENSE). Сторонние ассеты не используются; см.
[ASSET_LICENSES.md](ASSET_LICENSES.md).

## Превью

Каталог `docs/previews/` содержит описание требуемых кадров и инструкцию, как их
снять с реального экрана/эмулятора. Готовые GIF-файлы создаются при релизе из
записанных сценариев — в репозиторий коммитятся реальные снимки приложения.
