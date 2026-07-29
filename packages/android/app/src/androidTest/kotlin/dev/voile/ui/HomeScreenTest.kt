package dev.voile.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.WarpInfo
import dev.voile.tunnel.TunnelTelemetry
import dev.voile.ui.screens.HomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testServer = WarpInfo(
        id = 1, country = "France", city = "Paris",
        flag = "🇫🇷", ping = 12, load = 34,
    )

    private val testTelemetry = TunnelTelemetry(
        ip = "1.2.3.4", colo = "PAR",
        downloadMbps = 25.5, uploadMbps = 5.2,
        sessionDurationSec = 120L,
    )

    @Test
    fun homeScreen_displaysAllTelemetrySections() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Disconnected,
                        warpInfo = testServer,
                        realIp = "82.66.32.10",
                        telemetry = testTelemetry,
                        onToggleConnect = {},
                        onGoServers = {},
                    )
                }
            }
        }

        // Status pill
        composeTestRule.onNodeWithText("Déconnecté").assertIsDisplayed()

        // Telemetry labels
        composeTestRule.onNodeWithText("ADRESSE IP PUBLIQUE").assertIsDisplayed()
        composeTestRule.onNodeWithText("TÉLÉCHARGEMENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("ENVOI").assertIsDisplayed()
        composeTestRule.onNodeWithText("DURÉE DE SESSION").assertIsDisplayed()

        // Values
        composeTestRule.onNodeWithText("1.2.3.4").assertIsDisplayed()
        composeTestRule.onNodeWithText("25.5").assertIsDisplayed()
        composeTestRule.onNodeWithText("5.2").assertIsDisplayed()
        composeTestRule.onNodeWithText("00:02:00").assertIsDisplayed()
    }

    @Test
    fun homeScreen_connectButton_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Disconnected,
                        warpInfo = testServer,
                        realIp = null,
                        telemetry = TunnelTelemetry("—.—.—.—", "—", 0.0, 0.0, 0L),
                        onToggleConnect = { clicked = true },
                        onGoServers = {},
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Se connecter au serveur Paris")
            .performClick()

        assert(clicked) { "Le callback de connexion doit être appelé" }
    }

    @Test
    fun homeScreen_connected_showsBeforeAfterIP() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Connected(0L),
                        warpInfo = testServer,
                        realIp = "82.66.32.10",
                        telemetry = testTelemetry,
                        onToggleConnect = {},
                        onGoServers = {},
                    )
                }
            }
        }

        // Statut "Connexion sécurisée"
        composeTestRule.onNodeWithText("Connexion sécurisée").assertIsDisplayed()

        // Section "avant/après IP" doit être visible
        composeTestRule.onNodeWithText("VOTRE IP EST MAINTENANT").assertIsDisplayed()
        composeTestRule.onNodeWithText("82.66.32.10").assertIsDisplayed()
    }

    @Test
    fun homeScreen_connecting_showsNegociationLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Connecting,
                        warpInfo = testServer,
                        realIp = null,
                        telemetry = testTelemetry,
                        onToggleConnect = {},
                        onGoServers = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Négociation du tunnel").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Annuler la connexion à Paris")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_serverCard_navigatesToServers() {
        var navigated = false
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Disconnected,
                        warpInfo = testServer,
                        realIp = null,
                        telemetry = testTelemetry,
                        onToggleConnect = {},
                        onGoServers = { navigated = true },
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                "Changer de serveur, actuellement Paris"
            )
            .performClick()

        assert(navigated)
    }

    @Test
    fun homeScreen_disclaimerAlwaysVisible() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    HomeScreen(
                        tunnelState = VoileTunnelService.TunnelState.Disconnected,
                        warpInfo = testServer,
                        realIp = null,
                        telemetry = testTelemetry,
                        onToggleConnect = {},
                        onGoServers = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("SIMULATION — AUCUNE CONNEXION RÉELLE")
            .assertIsDisplayed()
    }
}
