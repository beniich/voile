package dev.voile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.voile.ui.VoileApp
import dev.voile.ui.theme.VoileTheme
import dev.voile.ui.viewmodel.VoileViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: VoileViewModel

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted(this)
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = VoileViewModel(
            application = application,
            prefs = (application as VoileApplication).prefs,
            warpRepo = (application as VoileApplication).warpRepo,
        )

        // Observe l'intent VPN en attente
        val state by viewModel.uiState.collectAsState()
        state.pendingVpnIntent?.let { intent ->
            vpnPermissionLauncher.launch(intent)
            viewModel.onVpnPermissionGranted(this) // Reset avant relance
        }

        setContent {
            VoileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VoileApp(viewModel = viewModel)
                }
            }
        }
    }
}
