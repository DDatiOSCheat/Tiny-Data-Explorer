package com.example.rootforgedataexplorer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.rootforgedataexplorer.ui.theme.RootForgeDataExplorerTheme
import com.example.rootforgedataexplorer.ui.components.LoadingScreen
import com.example.rootforgedataexplorer.ui.components.RootRequestScreen
import com.example.rootforgedataexplorer.ui.screens.MainScreen
import com.example.rootforgedataexplorer.utils.RootUtils

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            RootForgeDataExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        RootUtils.closeShell()
    }
}

@Composable
fun AppContent() {
    var isRootChecked by remember { mutableStateOf(false) }
    var isRooted by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        isChecking = true
        isRooted = RootUtils.isRootAvailable()
        isChecking = false
        isRootChecked = true
    }
    
    when {
        isChecking -> {
            LoadingScreen(message = "Đang kiểm tra quyền root...")
        }
        !isRootChecked || !isRooted -> {
            RootRequestScreen(
                onRequestRoot = {
                    // Yêu cầu root bằng cách thử lại
                    isRooted = RootUtils.isRootAvailable()
                    if (!isRooted) {
                        Toast.makeText(
                            getApplicationContext(),
                            "Cần quyền root để sử dụng app này!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
        else -> {
            MainScreen()
        }
    }
}