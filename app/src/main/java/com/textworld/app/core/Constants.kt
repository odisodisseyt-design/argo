package com.textworld.app.core

object Constants {
    const val THEME_INTERPOLATION_MIN_MS = 500L
    const val THEME_INTERPOLATION_MAX_MS = 900L
    const val WS_RECONNECT_BASE_MS = 800L
    const val WS_RECONNECT_MAX_MS = 30_000L
    const val LONG_POLL_INTERVAL_MS = 3_000L
    const val DEFAULT_PACE_MS = 18L
    const val SECURE_PREFS_NAME = "tw_secure_prefs"
    const val SETTINGS_PREFS_NAME = "tw_settings"
    const val TOKEN_KEY = "session_token"
    const val USER_ID_KEY = "user_id"
    const val USER_NAME_KEY = "user_name"
    const val UPDATE_FILE_NAME = "text-world-update.apk"
}
