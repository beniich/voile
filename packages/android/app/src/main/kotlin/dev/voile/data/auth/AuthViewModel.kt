package dev.voile.data.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel d'authentification.
 * Expose un état UI simple pour l'écran de login.
 */
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    data class UiState(
        val user: UserInfo? = null,
        val isAuthenticated: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val magicLinkSent: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Observe l'état de la session Supabase.
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        _uiState.value = _uiState.value.copy(
                            user = status.session.user,
                            isAuthenticated = true,
                            isLoading = false,
                            error = null,
                        )
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (status.isSignOut) {
                            // Déconnexion explicite : reset complet
                            _uiState.value = UiState()
                        } else {
                            // Pas connecté mais pas de signout : on garde l'UI
                            _uiState.value = _uiState.value.copy(
                                user = null,
                                isAuthenticated = false,
                                isLoading = false,
                            )
                        }
                    }
                    is SessionStatus.LoadingFromStorage -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Session expirée, veuillez vous reconnecter",
                            isAuthenticated = false,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun sendMagicLink(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email requis")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email invalide")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.signInWithMagicLink(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        magicLinkSent = true,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur d'envoi du lien",
                    )
                }
        }
    }

    fun completeGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.completeGoogleSignIn(idToken)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur Google Sign-In",
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetMagicLinkSent() {
        _uiState.value = _uiState.value.copy(magicLinkSent = false)
    }
}
