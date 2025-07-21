package ai.gbox.chatdroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ai.gbox.chatdroid.MainActivity
import ai.gbox.chatdroid.R

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "chat_notifications"
        const val CHANNEL_NAME = "Chat Messages"
        const val CHANNEL_DESCRIPTION = "Notifications for new chat messages"
        const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showChatMessageNotification(
        chatTitle: String,
        messageContent: String,
        chatId: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            chatId?.let { putExtra("chat_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_menu_camera) // You might want to create a specific notification icon
            .setContentTitle(chatTitle)
            .setContentText(messageContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageContent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            android.util.Log.w("NotificationHelper", "Notification permission not granted", e)
        }
    }
    
    fun showUploadProgressNotification(
        filename: String,
        progress: Int,
        isCompleted: Boolean = false
    ) {
        val notificationId = 2001
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_menu_camera)
            .setContentTitle(if (isCompleted) "Upload Complete" else "Uploading File")
            .setContentText(filename)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!isCompleted)
            .apply {
                if (!isCompleted) {
                    setProgress(100, progress, false)
                }
            }
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            
            // Auto-dismiss after a few seconds if completed
            if (isCompleted) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }, 3000)
            }
        } catch (e: SecurityException) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted", e)
        }
    }
    
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
    
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    fun hasNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}