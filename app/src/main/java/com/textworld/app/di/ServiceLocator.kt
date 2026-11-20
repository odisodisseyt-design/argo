package com.textworld.app.di

import android.content.Context
import android.os.Build
import android.os.Vibrator
import com.textworld.app.data.network.RealtimeClient
import com.textworld.app.data.network.TextWorldApi
import com.textworld.app.data.network.UpdateRepository
import com.textworld.app.data.repository.GameRepository
import com.textworld.app.data.settings.SettingsStore
import com.textworld.app.data.token.SecureTokenStore
import com.textworld.app.presentation.gateways.AndroidHapticsGateway
import com.textworld.app.presentation.gateways.AmbientAudioGateway
import com.textworld.app.presentation.gateways.DisabledAmbientAudioGateway
import com.textworld.app.presentation.gateways.DisabledImageGateway
import com.textworld.app.presentation.gateways.DisabledVoiceGateway
import com.textworld.app.presentation.gateways.HapticsGateway
import com.textworld.app.presentation.gateways.ImageGateway
import com.textworld.app.presentation.gateways.VoiceGateway
import com.textworld.app.presentation.renderer.PresentationRenderer

/** Manual DI container — no proprietary frameworks, explicit lifetimes. */
class ServiceLocator(context: Context) {
    val tokenStore = SecureTokenStore(context)
    val settingsStore = SettingsStore(context)
    val renderer = PresentationRenderer()
    val api = TextWorldApi { tokenStore.token }
    val realtime = RealtimeClient { tokenStore.token }
    val updateRepository = UpdateRepository(context)

    val voiceGateway: VoiceGateway = DisabledVoiceGateway()
    val imageGateway: ImageGateway = DisabledImageGateway()
    val ambientAudioGateway: AmbientAudioGateway = DisabledAmbientAudioGateway()

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    val hapticsGateway: HapticsGateway = AndroidHapticsGateway(
        vibrator = vibrator,
        isAvailable = vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
    )

    val repository = GameRepository(
        api = api,
        tokenStore = tokenStore,
        settingsStore = settingsStore,
        renderer = renderer,
        realtime = realtime,
        haptics = hapticsGateway,
    )
}
