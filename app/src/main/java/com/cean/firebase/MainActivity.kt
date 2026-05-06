package com.cean.firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cean.firebase.ui.TareasScreen
import com.cean.firebase.ui.theme.FirebaseTheme
import com.cean.firebase.viewmodel.TareasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirebaseTheme() {
                val viewModel: TareasViewModel = viewModel()
                TareasScreen(viewModel = viewModel)
            }
        }
    }
}