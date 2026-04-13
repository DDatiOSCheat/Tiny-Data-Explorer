package com.example.rootforgedataexplorer.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.rememberNavigationBarState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.rootforgedataexplorer.R
import com.example.rootforgedataexplorer.ui.components.LoadingScreen
import com.example.rootforgedataexplorer.ui.theme.RootForgeDataExplorerTheme
import com.example.rootforgedataexplorer.ui.utils.icons

@Composable
fun MainScreen() {
    val scaffoldState = rememberScaffoldState()
    val tabState = remember { mutableStateOf(0) }
    
    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomNavigation(tabState)
        }
    ) { padding ->
        RootForgeDataExplorerTheme {
            when (tabState.value) {
                0 -> AppsScreen(modifier = Modifier.padding(padding))
                1 -> FileBrowserScreen(modifier = Modifier.padding(padding))
                2 -> ToolsScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun BottomNavigation(tabState: androidx.compose.runtime.MutableState<Int>) {
    NavigationBar {
        NavigationBarItem(
            selected = tabState.value == 0,
            onClick = { tabState.value = 0 },
            icon = { icons.AppIcon() },
            label = { Text(text = "Apps") }
        )
        
        NavigationBarItem(
            selected = tabState.value == 1,
            onClick = { tabState.value = 1 },
            icon = { icons.FolderIcon() },
            label = { Text(text = "File Browser") }
        )
        
        NavigationBarItem(
            selected = tabState.value == 2,
            onClick = { tabState.value = 2 },
            icon = { icons.ToolsIcon() },
            label = { Text(text = "Tools") }
        )
    }
}