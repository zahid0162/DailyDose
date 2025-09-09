package com.zahid.dailydose.data.service

import com.zahid.dailydose.domain.model.Dose
import com.zahid.dailydose.domain.model.DoseStatus
import com.zahid.dailydose.domain.model.Medication
import java.text.SimpleDateFormat
import java.util.*

class DoseCalculationService {
    
    fun calculateTodaysDoses(medications: List<Medication>, userId: String): List<Dose> {
        val today = Calendar.getInstance()
        val doses = mutableListOf<Dose>()
        
        medications.forEach { medication ->
            if (isMedicationActiveToday(medication, today)) {
                val medicationDoses = calculateDosesForMedication(medication, today, userId)
                doses.addAll(medicationDoses)
            }
        }
        
        return doses.sortedBy { it.doseTime }
    }
    
    private fun isMedicationActiveToday(medication: Medication, today: Calendar): Boolean {
        val startDate = Calendar.getInstance().apply { time = medication.startDate }
        val endDate = medication.endDate?.let { 
            Calendar.getInstance().apply { time = it }
        }
        
        // Check if medication has started
        if (today.before(startDate)) return false
        
        // Check if medication has ended (if it has an end date)
        if (endDate != null && today.after(endDate)) return false
        
        return medication.isActive
    }
    
    private fun calculateDosesForMedication(
        medication: Medication, 
        today: Calendar, 
        userId: String
    ): List<Dose> {
        val doses = mutableListOf<Dose>()
        
        medication.specificTimes.forEach { timeString ->
            val doseTime = createDoseTime(today, timeString)
            
            val dose = Dose(
                medicationId = medication.id ?: "",
                userId = userId,
                doseTime = doseTime,
                scheduledTime = timeString,
                status = DoseStatus.UPCOMING // Initial status, will be updated based on logs and time
            )
            
            doses.add(dose)
        }
        
        return doses
    }
    
    private fun createDoseTime(today: Calendar, timeString: String): Date {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFormat.parse(timeString)
        val timeCalendar = Calendar.getInstance().apply {
            time?.let {
                this.time = it
            }
        }
        
        val doseCalendar = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return doseCalendar.time
    }
    
    private fun calculateDoseStatus(doseTime: Date): DoseStatus {
        val now = Date()
        val timeDifference = doseTime.time - now.time
        
        return when {
            timeDifference > 30 * 60 * 1000 -> DoseStatus.UPCOMING // More than 30 minutes in future
            timeDifference > -30 * 60 * 1000 -> DoseStatus.DUE // Within 30 minutes (past or future)
            else -> DoseStatus.MISSED // More than 30 minutes past
        }
    }
    
    fun updateDoseStatuses(doses: List<Dose>): List<Dose> {
        return doses.map { dose ->
            val newStatus = calculateDoseStatus(dose.doseTime)
            dose.copy(status = newStatus)
        }
    }
}
