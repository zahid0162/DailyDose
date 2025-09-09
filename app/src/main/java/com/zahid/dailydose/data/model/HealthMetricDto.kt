package com.zahid.dailydose.data.model

import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HealthMetricDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val type: String,
    val value: Double,
    val unit: String,
    val notes: String? = null,
    @SerialName("recorded_at")
    val recordedAt: Long,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
) {
    companion object {
        fun fromHealthMetric(metric: HealthMetric): HealthMetricDto {
            return HealthMetricDto(
                id = metric.id,
                userId = metric.userId,
                type = metric.type.name,
                value = metric.value,
                unit = metric.unit,
                notes = metric.notes,
                recordedAt = metric.recordedAt,
                createdAt = metric.createdAt,
                updatedAt = metric.updatedAt
            )
        }
        
        fun toHealthMetric(dto: HealthMetricDto): HealthMetric {
            return HealthMetric(
                id = dto.id,
                userId = dto.userId,
                type = HealthMetricType.valueOf(dto.type),
                value = dto.value,
                unit = dto.unit,
                notes = dto.notes,
                recordedAt = dto.recordedAt,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}
