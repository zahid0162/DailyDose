package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.domain.model.HealthMetricSummary

interface HealthMetricRepository {
    suspend fun addHealthMetric(metric: HealthMetric): Result<HealthMetric>
    suspend fun getHealthMetricsByType(userId: String, type: HealthMetricType): Result<List<HealthMetric>>
    suspend fun getLatestHealthMetrics(userId: String): Result<List<HealthMetricSummary>>
    suspend fun deleteHealthMetric(metricId: String): Result<Unit>
    suspend fun updateHealthMetric(metric: HealthMetric): Result<HealthMetric>
}
