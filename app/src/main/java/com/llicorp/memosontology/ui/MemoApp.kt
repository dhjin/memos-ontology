package com.llicorp.memosontology.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.llicorp.memosontology.MemosApplication
import com.llicorp.memosontology.ui.detail.MemoDetailScreen
import com.llicorp.memosontology.ui.edit.MemoEditScreen
import com.llicorp.memosontology.ui.home.HomeScreen
import com.llicorp.memosontology.ui.nlq.NlqScreen
import com.llicorp.memosontology.ui.nlq.NlqViewModel
import com.llicorp.memosontology.ui.ontology.OntologyScreen
import com.llicorp.memosontology.ui.ontology.OntologyViewModel
import com.llicorp.memosontology.ui.settings.SettingsScreen

private object Routes {
    const val HOME = "home"
    const val NLQ = "nlq"
    const val ONTOLOGY = "ontology"
    const val DETAIL = "detail?id={id}"
    const val EDIT = "edit"
    const val EDIT_WITH_ID = "edit?id={id}"
    const val SETTINGS = "settings"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Memos", Icons.Default.Home),
    BottomTab(Routes.NLQ, "NLQ", Icons.Default.Search),
    BottomTab(Routes.ONTOLOGY, "온톨로지", Icons.Default.Info),
)

@Composable
fun MemoApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val container = (context.applicationContext as MemosApplication).container

    val memoViewModel: MemoViewModel = viewModel(
        factory = MemoViewModel.Factory(container.memoRepository, container.serverConfig)
    )
    val nlqViewModel: NlqViewModel = viewModel(
        factory = NlqViewModel.Factory(container.nlqRepository)
    )
    val ontologyViewModel: OntologyViewModel = viewModel(
        factory = OntologyViewModel.Factory(container.fusekiRepository)
    )

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.NLQ, Routes.ONTOLOGY)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = memoViewModel,
                    onAddClick = { navController.navigate(Routes.EDIT) },
                    onEditClick = { id -> navController.navigate("detail?id=$id") },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.DETAIL) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                MemoDetailScreen(
                    viewModel = memoViewModel,
                    ontologyViewModel = ontologyViewModel,
                    memoId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { editId -> navController.navigate("edit?id=$editId") }
                )
            }
            composable(Routes.NLQ) {
                NlqScreen(viewModel = nlqViewModel)
            }
            composable(Routes.ONTOLOGY) {
                OntologyScreen(viewModel = ontologyViewModel)
            }
            composable(Routes.EDIT_WITH_ID) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                MemoEditScreen(
                    viewModel = memoViewModel,
                    memoId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.EDIT) {
                MemoEditScreen(
                    viewModel = memoViewModel,
                    memoId = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = memoViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
