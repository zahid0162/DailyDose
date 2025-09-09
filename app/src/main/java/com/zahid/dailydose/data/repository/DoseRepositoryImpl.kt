package com.zahid.dailydose.data.repository

import com.zahid.dailydose.data.model.DoseDto
import com.zahid.dailydose.data.supabase.SupabaseClient
import com.zahid.dailydose.domain.model.Dose
import com.zahid.dailydose.domain.model.DoseStatus
import com.zahid.dailydose.domain.repository.DoseRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import java.util.Date

class DoseRepositoryImpl : DoseRepository {
    
    private val supabase = SupabaseClient.client
    
    override suspend fun getTodaysDoses(userId: String): List<Dose> {
        return try {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val tomorrow = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val response = supabase.postgrest.from("dose_logs")
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("dose_time", today.timeInMillis)
                        lt("dose_time", tomorrow.timeInMillis)
                    }
                    order("dose_time", Order.ASCENDING)
                }
                .decodeList<DoseDto>()
            
            response.map { DoseDto.toDose(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getDosesByMedicationId(medicationId: String): List<Dose> {
        return try {
            val response = supabase.postgrest.from("dose_logs")
                .select {
                    filter {
                        eq("medication_id", medicationId)
                    }
                    order("dose_time", Order.DESCENDING)
                }
                .decodeList<DoseDto>()
            
            response.map { DoseDto.toDose(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun markDoseAsTaken(doseId: String): Result<Boolean> {
        return try {
            val currentTime = System.currentTimeMillis()
            supabase.postgrest.from("dose_logs")
                .update(mapOf(
                    "taken_at" to currentTime,
                    "updated_at" to currentTime
                )) {
                    filter {
                        eq("id", doseId)
                    }
                }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createDoseLog(dose: Dose): Result<Boolean> {
        return try {
            // Ensure the dose status is TAKEN when creating a log entry
            val takenDose = dose.copy(status = DoseStatus.TAKEN, takenAt = Date())
            val doseDto = DoseDto.fromDose(takenDose)
            supabase.postgrest.from("dose_logs")
                .insert(doseDto)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateDoseStatus(doseId: String, status: DoseStatus): Result<Boolean> {
        return try {
            val currentTime = System.currentTimeMillis()
            supabase.postgrest.from("dose_logs")
                .update(mapOf(
                    "status" to status.name,
                    "updated_at" to currentTime
                )) {
                    filter {
                        eq("id", doseId)
                    }
                }
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDoseById(doseId: String): Dose? {
        return try {
            val response = supabase.postgrest.from("dose_logs")
                .select {
                    filter {
                        eq("id", doseId)
                    }
                    single()
                }
                .decodeSingle<DoseDto>()
            
            DoseDto.toDose(response)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observeTodaysDoses(userId: String): Flow<List<Dose>> = flow {
        try {
            val doses = getTodaysDoses(userId)
            emit(doses)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
