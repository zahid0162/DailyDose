package com.zahid.dailydose.data.model

import com.zahid.dailydose.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class MedicationDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val form: String,
    val strength: String,
    val dosage: String,
    @SerialName("start_date")
    val startDate: Long,
    @SerialName("end_date")
    val endDate: Long? = null,
    @SerialName("is_ongoing")
    val isOngoing: Boolean = false,
    @SerialName("times_per_day")
    val timesPerDay: Int,
    @SerialName("specific_times")
    val specificTimes: String, // JSON array as string
    @SerialName("meal_timing")
    val mealTiming: String? = null,
    @SerialName("reminders_enabled")
    val remindersEnabled: Boolean = true,
    @SerialName("reminder_type")
    val reminderType: String = "DEFAULT",
    @SerialName("prescribed_by")
    val prescribedBy: String? = null,
    @SerialName("prescription_file_url")
    val prescriptionFileUrl: String? = null,
    val notes: String? = null,
    @SerialName("refill_count")
    val refillCount: Int? = null,
    val category: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMedication(medication: Medication): MedicationDto {
            return MedicationDto(
                id = medication.id,
                userId = medication.userId,
                name = medication.name,
                form = medication.form.name,
                strength = medication.strength,
                dosage = medication.dosage,
                startDate = medication.startDate.time,
                endDate = medication.endDate?.time,
                isOngoing = medication.isOngoing,
                timesPerDay = medication.timesPerDay,
                specificTimes = kotlinx.serialization.json.Json.encodeToString(medication.specificTimes),
                mealTiming = medication.mealTiming?.name,
                remindersEnabled = medication.remindersEnabled,
                reminderType = medication.reminderType.name,
                prescribedBy = medication.prescribedBy,
                prescriptionFileUrl = medication.prescriptionFileUrl,
                notes = medication.notes,
                refillCount = medication.refillCount,
                category = medication.category?.name,
                isActive = medication.isActive,
                createdAt = medication.createdAt,
                updatedAt = medication.updatedAt
            )
        }
        
        fun toMedication(dto: MedicationDto): Medication {
            return Medication(
                id = dto.id,
                userId = dto.userId,
                name = dto.name,
                form = MedicationForm.valueOf(dto.form),
                strength = dto.strength,
                dosage = dto.dosage,
                startDate = Date(dto.startDate),
                endDate = dto.endDate?.let { Date(it) },
                isOngoing = dto.isOngoing,
                timesPerDay = dto.timesPerDay,
                specificTimes = kotlinx.serialization.json.Json.decodeFromString(dto.specificTimes),
                mealTiming = dto.mealTiming?.let { MealTiming.valueOf(it) },
                remindersEnabled = dto.remindersEnabled,
                reminderType = ReminderType.valueOf(dto.reminderType),
                prescribedBy = dto.prescribedBy,
                prescriptionFileUrl = dto.prescriptionFileUrl,
                notes = dto.notes,
                refillCount = dto.refillCount,
                category = dto.category?.let { MedicationCategory.valueOf(it) },
                isActive = dto.isActive,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}
