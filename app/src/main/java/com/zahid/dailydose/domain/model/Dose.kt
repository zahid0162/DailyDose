package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class Dose(
    val id: String? = null,
    @SerialName("medication_id")
    val medicationId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("dose_time")
    @Serializable(with = DateSerializer::class)
    val doseTime: Date,
    @SerialName("scheduled_time")
    val scheduledTime: String, // e.g., "08:00"
    val status: DoseStatus,
    @SerialName("taken_at")
    @Serializable(with = DateSerializer::class)
    val takenAt: Date? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class DoseStatus {
    UPCOMING,    // Dose is scheduled for the future
    DUE,         // Dose is due now (within a reasonable time window)
    TAKEN,       // Dose has been taken
    MISSED,      // Dose was missed (time has passed without being taken)
    SKIPPED      // Dose was intentionally skipped
}
