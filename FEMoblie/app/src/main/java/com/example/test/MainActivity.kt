package com.example.test

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.navigation.AppNavGraph
import com.example.test.ui.theme.AppTheme
import com.example.test.vm.SettingsViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val dark by settingsVm.darkMode.collectAsStateWithLifecycle()

            AppTheme(darkTheme = dark) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}
