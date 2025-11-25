package com.azwin.notifshock

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.azwin.notifshock.Model.AlarmModel

class SirenService : NotificationListenerService() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var repository: AlarmModel

    override fun onCreate() {
        super.onCreate()
        repository = AlarmModel(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // Check if app detection is enabled
        val isTelegramEnabled = repository.isAppEnabled("APP_TELEGRAM")
        val isWhatsappEnabled = repository.isAppEnabled("APP_WHATSAPP")

        val isTelegramPackage = packageName.contains("telegram", ignoreCase = true)
        val isWhatsappPackage = packageName.contains("whatsapp", ignoreCase = true)

        if ((isTelegramPackage && isTelegramEnabled) || (isWhatsappPackage && isWhatsappEnabled)) {

            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val targetKeyword = repository.getTargetKeyword()

            Log.d("NotifSiren", "Notification received from $packageName: $title | Target: $targetKeyword")

            // Match notification title with user-defined keyword
            if (targetKeyword.isNotEmpty() && title.contains(targetKeyword, ignoreCase = true)) {
                triggerAlarm()
            }
        }
    }

    private fun triggerAlarm() {

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val streamType = AudioManager.STREAM_ALARM // Pastikan MediaPlayer Anda juga menggunakan stream ini

        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        audioManager.setStreamVolume(streamType, maxVolume, 0)

        // Prevent overlapping alarms
        if (mediaPlayer?.isPlaying == true) return

        try {
            // Use default alarm sound, fallback to ringtone if unavailable
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmSound)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true 
                prepare()
                start()
            }
            
            // Broadcast alarm state to UI
            sendBroadcast(Intent("ALARM_STATUS_CHANGED").putExtra("IS_PLAYING", true))
            Log.d("NotifSiren", "Alarm triggered")
            
        } catch (e: Exception) {
            Log.e("NotifSiren", "Failed to play alarm: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop command from UI
        if (intent?.action == "STOP_ALARM") {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            
            // Notify UI that alarm has stopped
            sendBroadcast(Intent("ALARM_STATUS_CHANGED").putExtra("IS_PLAYING", false))
            Log.d("NotifSiren", "Alarm stopped")
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
