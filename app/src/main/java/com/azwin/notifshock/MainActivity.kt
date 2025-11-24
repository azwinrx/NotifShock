package com.azwin.notifshock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.azwin.notifshock.Model.AlarmModel
import com.azwin.notifshock.View.AlarmView
import com.azwin.notifshock.ViewModel.AlarmViewModel
import com.azwin.notifshock.ui.theme.NotifShockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inisialisasi Model & ViewModel
        val repository = AlarmModel(this)
        val viewModel = AlarmViewModel(repository)

        enableEdgeToEdge()
        setContent {
            NotifShockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Panggil View utama kita
                    AlarmView(viewModel)
                }
            }
        }
    }
}
