package com.zahid.dailydose.data.model

import com.zahid.dailydose.domain.model.Dose
import com.zahid.dailydose.domain.model.DoseStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class DoseDto(
    val id: String? = null,
    @SerialName("medication_id")
    val medicationId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("dose_time")
    val doseTime: Long,
    @SerialName("scheduled_time")
    val scheduledTime: String,
    val status: String,
    @SerialName("taken_at")
    val takenAt: Long? = null,
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromDose(dose: Dose): DoseDto {
            return DoseDto(
                id = dose.id,
                medicationId = dose.medicationId,
                userId = dose.userId,
                doseTime = dose.doseTime.time,
                scheduledTime = dose.scheduledTime,
                status = dose.status.name,
                takenAt = dose.takenAt?.time,
                notes = dose.notes,
                createdAt = dose.createdAt,
                updatedAt = dose.updatedAt
            )
        }
        
        fun toDose(dto: DoseDto): Dose {
            return Dose(
                id = dto.id,
                medicationId = dto.medicationId,
                userId = dto.userId,
                doseTime = Date(dto.doseTime),
                scheduledTime = dto.scheduledTime,
                status = DoseStatus.valueOf(dto.status),
                takenAt = dto.takenAt?.let { Date(it) },
                notes = dto.notes,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}
