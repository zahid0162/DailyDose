package com.zahid.dailydose.data.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zahid.dailydose.MainActivity
import com.zahid.dailydose.R
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.utils.NotificationPermissionHelper
import java.util.*

class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val CHANNEL_NAME = "Medication Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for medication reminders"
        
        private const val NOTIFICATION_ID_BASE = 1000
        private const val REQUEST_CODE_BASE = 2000
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleMedicationReminders(medication: Medication) {
        if (!medication.remindersEnabled) return
        
        // Check if notifications are enabled
        if (!NotificationPermissionHelper.areNotificationsEnabled(context)) {
            println("Notifications are not enabled for this app")
            return
        }
        
        // Check if exact alarms can be scheduled (Android 12+)
        if (!NotificationPermissionHelper.canScheduleExactAlarms(context)) {
            println("Cannot schedule exact alarms")
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val medicationId = medication.id ?: return
        
        // Cancel existing reminders for this medication
        cancelMedicationReminders(medicationId)
        
        val startDate = medication.startDate
        val endDate = medication.endDate
        
        medication.specificTimes.forEachIndexed { index, timeString ->
            val timeParts = timeString.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            val calendar = Calendar.getInstance().apply {
                time = startDate
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // If the start time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val notificationId = NOTIFICATION_ID_BASE + medicationId.hashCode() + index
            val requestCode = REQUEST_CODE_BASE + medicationId.hashCode() + index
            
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medication_id", medicationId)
                putExtra("medication_name", medication.name)
                putExtra("notification_id", notificationId)
                putExtra("time_index", index)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (medication.isOngoing || endDate == null) {
                // Schedule daily recurring reminder
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                // Schedule reminders from start to end date
                scheduleRemindersInRange(alarmManager, calendar, endDate, pendingIntent)
            }
        }
    }
    
    private fun scheduleRemindersInRange(
        alarmManager: AlarmManager,
        startCalendar: Calendar,
        endDate: Date,
        pendingIntent: PendingIntent
    ) {
        val currentCalendar = startCalendar.clone() as Calendar
        
        while (currentCalendar.timeInMillis <= endDate.time) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                currentCalendar.timeInMillis,
                pendingIntent
            )
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    fun cancelMedicationReminders(medicationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel all reminders for this medication (assuming max 10 times per day)
        repeat(10) { index ->
            val requestCode = REQUEST_CODE_BASE + medicationId.hashCode() + index
            val intent = Intent(context, MedicationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showNotification(
        medicationId: String,
        medicationName: String,
        timeIndex: Int,
        notificationId: Int
    ) {
        val motivationalMessage = getMotivationalMessage(medicationName, timeIndex)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("medication_id", medicationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(medicationName)
            .setContentText(motivationalMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .setLights(0xFF00FF00.toInt(), 1000, 1000)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        if (NotificationPermissionHelper.areNotificationsEnabled(context)){
            notificationManager.notify(notificationId, notification)
        }

    }
    
    private fun getMotivationalMessage(medicationName: String, timeIndex: Int): String {
        val messages = listOf(
            "Time for your $medicationName! Your health is your wealth 💊",
            "Don't forget your $medicationName! Every dose brings you closer to wellness 🌟",
            "Your $medicationName is ready! Stay consistent, stay healthy 💪",
            "Medication time! $medicationName is your ally in staying well 🏥",
            "Take your $medicationName now! Small steps lead to big improvements ✨",
            "Your $medicationName reminder! Consistency is key to recovery 🔑",
            "Time for $medicationName! You're doing great by staying on track 🎯",
            "Don't skip your $medicationName! Your future self will thank you 🙏",
            "Medication alert: $medicationName! Keep up the good work 🌈",
            "Your $medicationName is due! Every dose counts towards your health 🎉"
        )
        
        return messages[timeIndex % messages.size]
    }
}
