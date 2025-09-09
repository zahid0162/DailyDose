package com.zahid.dailydose.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra("medication_id") ?: return
        val medicationName = intent.getStringExtra("medication_name") ?: return
        val notificationId = intent.getIntExtra("notification_id", 0)
        val timeIndex = intent.getIntExtra("time_index", 0)
        
        val notificationService = NotificationService(context)
        notificationService.showNotification(
            medicationId = medicationId,
            medicationName = medicationName,
            timeIndex = timeIndex,
            notificationId = notificationId
        )
    }
}
