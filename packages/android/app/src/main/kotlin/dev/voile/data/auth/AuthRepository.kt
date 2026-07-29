package dev.voile.data.auth

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository d'authentification Voile.
 * Encapsule toute la logique d'auth Supabase (magic link, OAuth, signOut).
 */
class AuthRepository(private val client: SupabaseClient) {

    private val auth = client.auth

    /**
     * Flow de la session courante.
     * Émet Success(session) quand connecté, NotAuthenticated() sinon.
     */
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    /**
     * Flow de l'utilisateur courant (ou null).
     */
    val currentUser = auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user
            else -> null
        }
    }

    /**
     * Vérifie si une session est active.
     */
    fun isAuthenticated(): Boolean {
        return when (val status = auth.sessionStatus.value) {
            is SessionStatus.Authenticated -> status.session.user != null
            else -> false
        }
    }

    /**
     * Envoie un magic link à l'adresse email fournie.
     * L'utilisateur devra cliquer le lien reçu pour se connecter.
     */
    suspend fun signInWithMagicLink(email: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lance le flow OAuth Google.
     * Retourne l'Intent à démarrer via ActivityResultLauncher.
     */
    suspend fun signInWithGoogle(): Result<Intent?> {
        return try {
            // Pour Google OAuth, on délègue à l'appelant qui gère l'ActivityResultLauncher.
            // Le retour de cette méthode n'est qu'informatif — la vraie connexion
            // se fait via auth.signInWith(Google) après réception du callback.
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complète le flow OAuth Google avec le token ID reçu.
     */
    suspend fun completeGoogleSignIn(idToken: String): Result<Unit> {
        return try {
            auth.signInWith(Google) {
                this.idToken = idToken
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lance le flow OAuth Apple.
     */
    suspend fun signInWithApple(): Result<Unit> {
        return try {
            auth.signInWith(Apple)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Déconnecte l'utilisateur et invalide la session.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rafraîchit manuellement la session (utile si elle approche de l'expiration).
     */
    suspend fun refreshSession(): Result<Unit> {
        return try {
            auth.refreshCurrentSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Récupère le token d'accès courant pour les appels RPC authentifiés.
     */
    suspend fun getAccessToken(): String? {
        return try {
            auth.currentSessionOrNull()?.accessToken
        } catch (e: Exception) {
            null
        }
    }
}
