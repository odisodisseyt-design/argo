package com.textworld.app

import com.textworld.app.data.model.Features
import com.textworld.app.data.model.Illustration
import com.textworld.app.data.model.Presentation
import com.textworld.app.data.model.UiManifest
import com.textworld.app.data.settings.AppSettings
import com.textworld.app.presentation.renderer.PresentationRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresentationRendererTest {

    private val renderer = PresentationRenderer()

    @Test
    fun same_seed_produces_same_scene_key() {
        val presentation = Presentation(
            illustration = Illustration(seed = 84_021L, subject = "courtyard_at_dusk"),
        )
        renderer.submit(presentation, UiManifest(), AppSettings())
        val first = renderer.state.value.sceneKey
        renderer.submit(presentation.copy(), UiManifest(), AppSettings())
        assertEquals(first, renderer.state.value.sceneKey)
    }

    @Test
    fun dread_darkens_vignette_more_than_calm() {
        renderer.submit(Presentation(mood = "calm"), UiManifest(), AppSettings())
        val calm = renderer.state.value.vignetteAlpha
        renderer.submit(Presentation(mood = "dread"), UiManifest(), AppSettings())
        assertTrue(renderer.state.value.vignetteAlpha > calm)
        assertTrue(renderer.state.value.desaturation > 0f)
    }

    @Test
    fun disabled_features_disable_particles_and_parallax() {
        val presentation = Presentation(
            particles = com.textworld.app.data.model.Particles("ember", 0.8f, 0.5f),
            parallax = com.textworld.app.data.model.Parallax("forge", "trees", "grass"),
        )
        renderer.submit(presentation, UiManifest(features = Features(parallax = true)), AppSettings())
        assertTrue(renderer.state.value.particles.type == "ember")

        renderer.submit(
            presentation,
            UiManifest(features = Features(parallax = false)),
            AppSettings(particles = false),
        )
        assertEquals("none", renderer.state.value.particles.type)
        assertEquals("none", renderer.state.value.parallaxFar)
    }
}
