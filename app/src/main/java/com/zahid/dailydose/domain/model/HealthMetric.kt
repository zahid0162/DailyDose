package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.util.Date

@Serializable
data class HealthMetric(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val notes: String? = null,
    @SerialName("recorded_at")
    val recordedAt: Long,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
)

@Serializable
enum class HealthMetricType {
    BLOOD_PRESSURE_SYSTOLIC,
    BLOOD_PRESSURE_DIASTOLIC,

    DIABETES,
    HEART_RATE,
    WEIGHT,
    TEMPERATURE
}

@Serializable
data class BloodPressureReading(
    val systolic: Double,
    val diastolic: Double,
    val unit: String = "mmHg"
)

@Serializable
data class HealthMetricSummary(
    val type: HealthMetricType,
    val latestValue: Double,
    val unit: String,
    val lastRecorded: Long,
    val trend: HealthTrend? = null
)

@Serializable
enum class HealthTrend {
    IMPROVING,
    STABLE,
    DECLINING
}
