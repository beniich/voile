package dev.voile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import dev.voile.core.tokens.VoileColors
import dev.voile.data.auth.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(VoileColors.bg))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Logo
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(VoileColors.secured).copy(alpha = 0.14f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color(VoileColors.secured),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Title
            Text(
                text = "Bienvenue sur Voile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(VoileColors.textPrimary),
            )

            // Subtitle
            Text(
                text = "Connectez-vous pour synchroniser vos serveurs et votre historique.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(VoileColors.textSecondary),
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            if (state.magicLinkSent) {
                // Confirmation screen
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(VoileColors.secured).copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(VoileColors.secured)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = Color(VoileColors.secured),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Lien envoyé à",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(VoileColors.textMuted),
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(VoileColors.textPrimary),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Vérifiez votre boîte mail et cliquez sur le lien.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(VoileColors.textSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = {
                                viewModel.resetMagicLinkSent()
                                email = ""
                            }
                        ) {
                            Text("Autre email", color = Color(VoileColors.secured))
                        }
                    }
                }
            } else {
                // Magic link form
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; viewModel.dismissError() },
                        label = { Text("Adresse email") },
                        placeholder = { Text("vous@exemple.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Send,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Champ email" },
                        shape = RoundedCornerShape(10.dp),
                    )

                    state.error?.let { err ->
                        Text(
                            text = err,
                            color = Color(VoileColors.danger),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Button(
                        onClick = { viewModel.sendMagicLink(email) },
                        enabled = !state.isLoading && email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(VoileColors.secured),
                            contentColor = Color(VoileColors.bg),
                        ),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(VoileColors.bg),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Envoi…")
                        } else {
                            Icon(Icons.Default.Mail, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Recevoir un lien magique", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Separator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(VoileColors.borderSoft)
                    )
                    Text(
                        text = "ou",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(VoileColors.textMuted),
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(VoileColors.borderSoft)
                    )
                }

                // OAuth buttons
                OAuthButton(
                    text = "Continuer avec Google",
                    icon = Icons.Default.Mail, // Placeholder — utiliser logo Google
                    onClick = { /* Déclencher Google Sign-In flow */ },
                    enabled = !state.isLoading,
                )

                OAuthButton(
                    text = "Continuer avec Apple",
                    icon = Icons.Outlined.Lock,
                    onClick = { /* Apple Sign-In */ },
                    enabled = !state.isLoading,
                )
            }
        }
    }
}

@Composable
private fun OAuthButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(VoileColors.textPrimary),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, Color(VoileColors.border)
        ),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
