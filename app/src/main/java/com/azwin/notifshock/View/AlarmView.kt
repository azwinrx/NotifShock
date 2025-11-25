package com.azwin.notifshock.View

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    
    // State for app switches
    val isTelegramEnabled by viewModel.isTelegramEnabled.collectAsState()
    val isWhatsappEnabled by viewModel.isWhatsappEnabled.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    
    // Local state for UI (editing mode) - sync with ViewModel's lock state
    var isEditing by remember(isLocked) { mutableStateOf(!isLocked) }
    
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
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
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
                            Text("Izinkan Akses Notifikasi (Wajib)")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Application Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aktifkan Aplikasi:", style = MaterialTheme.typography.labelLarge)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Telegram Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Telegram", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isTelegramEnabled,
                            onCheckedChange = { viewModel.toggleTelegram(it) }
                        )
                    }

                    // WhatsApp Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WhatsApp", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isWhatsappEnabled,
                            onCheckedChange = { viewModel.toggleWhatsapp(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Keyword Input
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = { 
                            viewModel.updateKeyword(it)
                        },
                        enabled = isEditing,
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
                            if (isEditing) {
                                viewModel.saveKeyword()
                                viewModel.setLocked(true)
                                isEditing = false
                            } else {
                                viewModel.setLocked(false)
                                isEditing = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isEditing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (!isEditing) Icons.Default.Edit else Icons.Default.Check,
                            contentDescription = null, 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (!isEditing) "Ubah Target" else "Simpan Target")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stop Alarm Button
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            Button(
                onClick = {
                    val intent = Intent(context, SirenService::class.java).apply {
                        action = "STOP_ALARM"
                    }
                    context.startService(intent)
                },
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(if (isPressed) 0.95f else 1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Stop Alarm",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STOP ALARM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
