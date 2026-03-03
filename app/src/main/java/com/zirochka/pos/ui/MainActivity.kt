package com.zirochka.pos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zirochka.pos.domain.model.OrderLine
import com.zirochka.pos.ui.screen.admin.AdminScreen
import com.zirochka.pos.ui.screen.admin.AdminViewModel
import com.zirochka.pos.ui.screen.cart.CartScreen
import com.zirochka.pos.ui.screen.cart.CartViewModel
import com.zirochka.pos.ui.screen.menu.MenuScreen
import com.zirochka.pos.ui.screen.menu.MenuViewModel
import com.zirochka.pos.ui.screen.orders.OrdersScreen
import com.zirochka.pos.ui.screen.orders.OrdersViewModel
import com.zirochka.pos.ui.theme.ZirochkaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZirochkaTheme {
                PosApp()
            }
        }
    }
}

data class Destination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun PosApp(navController: NavHostController = rememberNavController()) {
    val cartViewModel: CartViewModel = hiltViewModel()
    val menuViewModel: MenuViewModel = hiltViewModel()
    val ordersViewModel: OrdersViewModel = hiltViewModel()
    val adminViewModel: AdminViewModel = hiltViewModel()
    val destinations = listOf(
        Destination("menu", "Меню", Icons.Default.Restaurant),
        Destination("cart", "Кошик", Icons.Default.ShoppingCart),
        Destination("orders", "Замовлення", Icons.Default.List),
        Destination("admin", "Адмін", Icons.Default.AdminPanelSettings),
    )
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        icon = { androidx.compose.material3.Icon(destination.icon, null) },
                        label = { Text(destination.label) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "menu",
            modifier = Modifier.padding(padding)
        ) {
            composable("menu") {
                MenuScreen(
                    viewModel = menuViewModel,
                    onAdd = { cartViewModel.add(it) }
                )
            }
            composable("cart") {
                val scope = rememberCoroutineScope()
                CartScreen(
                    viewModel = cartViewModel,
                    onSubmit = { lines: List<OrderLine>, total ->
                        scope.launch { ordersViewModel.createOrder(lines, total) }
                    }
                )
            }
            composable("orders") {
                OrdersScreen(viewModel = ordersViewModel)
            }
            composable("admin") {
                AdminScreen(viewModel = adminViewModel)
            }
        }
    }
}
