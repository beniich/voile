package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.*

@Composable
fun AppNavigation(viewModel: VoileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val needsVpnPermission by viewModel.needsVpnPermission.collectAsState()

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted()
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    LaunchedEffect(needsVpnPermission) {
        needsVpnPermission?.let { intent ->
            vpnPermissionLauncher.launch(intent)
        }
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomNav = currentRoute in listOf("home", "servers", "analytics", "settings")
    // Note: StatusPill gère maintenant Error state dans HomeScreen.kt

    if (currentUser == null) {
        AuthScreen(viewModel)
    } else {
        Scaffold(
            bottomBar = { if (showBottomNav) BottomNav(navController) },
            containerColor = Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "welcome",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("welcome") { 
                    WelcomeScreen(onStartClick = {
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }) 
                }
                composable("home") { HomeScreen(viewModel, onNavigateToServers = { navController.navigate("servers") }) }
                composable("servers") { ServersScreen(viewModel) }
                composable("analytics") { AnalyticsScreen(viewModel) }
                composable("settings") { SettingsScreen(viewModel) }
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Surface.copy(alpha = 0.8f))
            .border(1.dp, BorderSoft.copy(alpha = 0.5f))
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomNavItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = "Accueil",
            selected = currentRoute == "home",
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            icon = { Icon(Icons.Outlined.Language, contentDescription = "Servers") },
            label = "Serveurs",
            selected = currentRoute == "servers",
            onClick = {
                if (currentRoute != "servers") navController.navigate("servers")
            },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Analytics") },
            label = "Statistiques",
            selected = currentRoute == "analytics",
            onClick = {
                if (currentRoute != "analytics") navController.navigate("analytics")
            },
            modifier = Modifier.weight(1f)
        )
        BottomNavItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = "Paramètres",
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") navController.navigate("settings")
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) Secured else TextMuted
    val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .then(
                if (selected) {
                    Modifier.background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Secured.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                } else {
                    Modifier
                }
            )
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Secured)
                    .align(Alignment.TopCenter)
            )
        }
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides color) {
                icon()
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = color,
                fontWeight = fontWeight
            )
        }
    }
}
