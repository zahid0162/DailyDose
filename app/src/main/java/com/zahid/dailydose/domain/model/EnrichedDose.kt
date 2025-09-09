package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class EnrichedDose(
    val id: String? = null,
    val medicationId: String,
    val userId: String,
    @Serializable(with = DateSerializer::class)
    val doseTime: Date,
    val scheduledTime: String,
    val status: DoseStatus,
    @Serializable(with = DateSerializer::class)
    val takenAt: Date? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Medication information
    val medicationName: String,
    val medicationStrength: String,
    val medicationForm: String,
    val medicationDosage: String,
    val mealTiming: String?
)
