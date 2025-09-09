package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class Medication(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val form: MedicationForm,
    val strength: String,
    val dosage: String,
    @SerialName("start_date")
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    @SerialName("end_date")
    @Serializable(with = DateSerializer::class)
    val endDate: Date? = null,
    @SerialName("is_ongoing")
    val isOngoing: Boolean = false,
    @SerialName("times_per_day")
    val timesPerDay: Int,
    @SerialName("specific_times")
    val specificTimes: List<String>, // e.g., ["08:00", "14:00", "20:00"]
    @SerialName("meal_timing")
    val mealTiming: MealTiming? = null,
    @SerialName("reminders_enabled")
    val remindersEnabled: Boolean = true,
    @SerialName("reminder_type")
    val reminderType: ReminderType = ReminderType.DEFAULT,
    @SerialName("prescribed_by")
    val prescribedBy: String? = null,
    @SerialName("prescription_file_url")
    val prescriptionFileUrl: String? = null,
    val notes: String? = null,
    @SerialName("refill_count")
    val refillCount: Int? = null,
    val category: MedicationCategory? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class MedicationForm {
    TABLET,
    CAPSULE,
    SYRUP,
    INJECTION,
    CREAM,
    DROPS,
    PATCH,
    OTHER
}

@Serializable
enum class MealTiming {
    BEFORE_MEAL,
    AFTER_MEAL,
    WITH_MEAL,
    ON_EMPTY_STOMACH,
    ANYTIME
}

@Serializable
enum class ReminderType {
    DEFAULT,
    SILENT,
    LOUD
}

@Serializable
enum class MedicationCategory {
    DIABETES,
    HEART,
    BLOOD_PRESSURE,
    PAIN_RELIEF,
    VITAMINS,
    ANTIBIOTICS,
    MENTAL_HEALTH,
    RESPIRATORY,
    DIGESTIVE,
    GENERAL
}
