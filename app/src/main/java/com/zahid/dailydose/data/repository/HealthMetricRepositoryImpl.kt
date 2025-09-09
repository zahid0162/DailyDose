package com.zahid.dailydose.data.repository

import com.zahid.dailydose.data.model.HealthMetricDto
import com.zahid.dailydose.data.supabase.SupabaseClient
import com.zahid.dailydose.domain.model.HealthMetric
import com.zahid.dailydose.domain.model.HealthMetricType
import com.zahid.dailydose.domain.model.HealthMetricSummary
import com.zahid.dailydose.domain.model.HealthTrend
import com.zahid.dailydose.domain.repository.HealthMetricRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class HealthMetricRepositoryImpl(

) : HealthMetricRepository {

    private val supabaseClient = SupabaseClient.client

    override suspend fun addHealthMetric(metric: HealthMetric): Result<HealthMetric> {
        return try {
            val dto = HealthMetricDto.fromHealthMetric(metric)
            val result = supabaseClient
                .from("health_metrics")
                .insert(dto){
                    select()
                }.decodeSingle<HealthMetricDto>()
            
            Result.success(HealthMetricDto.toHealthMetric(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHealthMetricsByType(userId: String, type: HealthMetricType): Result<List<HealthMetric>> {
        return try {
            val result = supabaseClient
                .from("health_metrics")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("type", type.name)
                    }
                    order("recorded_at", Order.DESCENDING)
                }
                .decodeList<HealthMetricDto>()
            
            val metrics = result.map { HealthMetricDto.toHealthMetric(it) }
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestHealthMetrics(userId: String): Result<List<HealthMetricSummary>> {
        return try {
            val allMetrics = supabaseClient
                .from("health_metrics")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("recorded_at", Order.DESCENDING)
                }
                .decodeList<HealthMetricDto>()

            val metricsByType = allMetrics.groupBy { it.type }
            val summaries = mutableListOf<HealthMetricSummary>()

            // Get latest for each type
            HealthMetricType.entries.forEach { type ->
                val typeMetrics = metricsByType[type.name] ?: return@forEach
                if (typeMetrics.isNotEmpty()) {
                    val latest = typeMetrics.first()
                    val trend = calculateTrend(typeMetrics.take(3))
                    
                    summaries.add(
                        HealthMetricSummary(
                            type = type,
                            latestValue = latest.value,
                            unit = latest.unit,
                            lastRecorded = latest.recordedAt,
                            trend = trend
                        )
                    )
                }
            }

            Result.success(summaries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHealthMetric(metricId: String): Result<Unit> {
        return try {
            supabaseClient
                .from("health_metrics")
                .delete {
                    filter {
                        eq("id", metricId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHealthMetric(metric: HealthMetric): Result<HealthMetric> {
        return try {
            val dto = HealthMetricDto.fromHealthMetric(metric)
            val result = supabaseClient
                .from("health_metrics")
                .update(dto) {
                    filter {
                        eq("id", metric.id)
                    }
                }
                .decodeSingle<HealthMetricDto>()
            
            Result.success(HealthMetricDto.toHealthMetric(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateTrend(metrics: List<HealthMetricDto>): HealthTrend? {
        if (metrics.size < 2) return null
        
        val values = metrics.map { it.value }
        val first = values.first()
        val last = values.last()
        
        val change = (last - first) / first * 100
        
        return when {
            change > 5 -> HealthTrend.IMPROVING
            change < -5 -> HealthTrend.DECLINING
            else -> HealthTrend.STABLE
        }
    }
}
