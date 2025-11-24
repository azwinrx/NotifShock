package com.azwin.notifshock

import android.content.Intent
import android.media.AudioAttributes
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

        // Filter for Telegram packages (official and variants)
        if (packageName.contains("telegram", ignoreCase = true)) {

            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: ""
            val targetKeyword = repository.getTargetKeyword()

            Log.d("TeleSiren", "Notification received: $title | Target: $targetKeyword")

            // Match notification title with user-defined keyword
            if (targetKeyword.isNotEmpty() && title.contains(targetKeyword, ignoreCase = true)) {
                triggerAlarm()
            }
        }
    }

    private fun triggerAlarm() {
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
            Log.d("TeleSiren", "Alarm triggered")
            
        } catch (e: Exception) {
            Log.e("TeleSiren", "Failed to play alarm: ${e.message}", e)
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
            Log.d("TeleSiren", "Alarm stopped")
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
