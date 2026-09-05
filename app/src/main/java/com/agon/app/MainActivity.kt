package com.agon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.agon.app.data.AppViewModel
import com.agon.app.ui.components.AdminGate
import com.agon.app.ui.screens.BrandScreen
import com.agon.app.ui.screens.DashboardScreen
import com.agon.app.ui.screens.GuideScreen
import com.agon.app.ui.screens.QrScreen
import com.agon.app.ui.screens.ReviewScreen
import com.agon.app.ui.theme.AgonAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AgonAppTheme {
                MainApp()
            }
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val CUSTOMER_TAB = Tab("review", "Review", Icons.Default.RateReview)
private val OWNER_TABS = listOf(
    Tab("qr", "QR Codes", Icons.Default.QrCode2),
    Tab("stats", "Analytics", Icons.Default.BarChart),
    Tab("brand", "Brand", Icons.Default.Palette),
    Tab("guide", "Flow", Icons.Default.Map)
)

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val vm: AppViewModel = viewModel()
    var adminUnlocked by remember { mutableStateOf(false) }
    var previewCampaign by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNav(navController, adminUnlocked) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "review",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("review") {
                ReviewScreen(vm = vm, campaignId = previewCampaign)
            }
            composable("qr") {
                AdminGate(vm, adminUnlocked, { adminUnlocked = true }) {
                    QrScreen(vm = vm, onPreviewCustomer = { id ->
                        previewCampaign = id
                        navController.navigate("review") { launchSingleTop = true }
                    })
                }
            }
            composable("stats") {
                AdminGate(vm, adminUnlocked, { adminUnlocked = true }) {
                    DashboardScreen(vm = vm)
                }
            }
            composable("brand") {
                AdminGate(vm, adminUnlocked, { adminUnlocked = true }) {
                    BrandScreen(vm = vm)
                }
            }
            composable("guide") {
                GuideScreen(
                    onGoCustomer = {
                        previewCampaign = null
                        navController.navigate("review") { launchSingleTop = true }
                    },
                    onGoQr = { navController.navigate("qr") { launchSingleTop = true } }
                )
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController, adminUnlocked: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(CUSTOMER_TAB) + OWNER_TABS
    NavigationBar {
        tabs.forEach { t ->
            NavigationBarItem(
                icon = { Icon(t.icon, contentDescription = t.label) },
                label = { Text(t.label) },
                selected = currentRoute == t.route,
                onClick = {
                    navController.navigate(t.route) {
                        popUpTo("review")
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
