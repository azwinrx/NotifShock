package com.azwin.notifshock.View

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.azwin.notifshock.SirenService
import com.azwin.notifshock.ViewModel.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmView(viewModel: AlarmViewModel) {
    val context = LocalContext.current
    val keyword by viewModel.keyword.collectAsState()
    
    var isSaved by remember { mutableStateOf(false) }
    var isAlarmRinging by remember { mutableStateOf(false) }

    // Listen for alarm state changes from SirenService
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "ALARM_STATUS_CHANGED") {
                    isAlarmRinging = intent.getBooleanExtra("IS_PLAYING", false)
                }
            }
        }
        val filter = IntentFilter("ALARM_STATUS_CHANGED")
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Check Notification Listener permission status
    var isPermissionGranted by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                isPermissionGranted = enabledListeners?.contains(context.packageName) == true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NotifSh", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Logo",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Jangan Lewatkan Pesan Penting!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Configuration Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Konfigurasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Permission Request (Only visible if not granted)
                    if (!isPermissionGranted) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Wajib: Izinkan Akses Notifikasi")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Keyword Input
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { 
                            viewModel.updateKeyword(it)
                        },
                        enabled = !isSaved,
                        label = { Text("Nama Kontak/Group") },
                        placeholder = { Text("Contoh: Boss, Mas Yudi, dsb") },
                        leadingIcon = { Text("@", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    Text(
                        text = "Masukkan nama kontak/grup persis seperti di notifikasi.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save / Edit Button
                    Button(
                        onClick = { 
                            if (isSaved) {
                                isSaved = false // Enter edit mode
                            } else {
                                viewModel.saveKeyword()
                                isSaved = true // Enter read-only mode
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (!isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Edit else Icons.Default.Check,
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaved) "Ubah Target" else "2. Simpan Target")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Emergency Stop Button
            Button(
                onClick = {
                    if (isAlarmRinging) {
                        val stopIntent = Intent(context, SirenService::class.java)
                        stopIntent.action = "STOP_ALARM"
                        context.startService(stopIntent)
                        
                        // Optimistic UI update
                        isAlarmRinging = false
                    }
                },
                enabled = isAlarmRinging,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlarmRinging) MaterialTheme.colorScheme.error else Color.Gray,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(pressedElevation = 8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isAlarmRinging) "MATIKAN ALARM SEKARANG!" else "Alarm Siaga (Standby)", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
