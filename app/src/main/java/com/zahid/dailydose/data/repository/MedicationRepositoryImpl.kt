package com.zahid.dailydose.data.repository

import com.zahid.dailydose.data.model.MedicationDto
import com.zahid.dailydose.data.supabase.SupabaseClient
import com.zahid.dailydose.domain.model.Medication
import com.zahid.dailydose.domain.repository.MedicationRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class MedicationRepositoryImpl : MedicationRepository {
    
    private val supabase = SupabaseClient.client
    
    override suspend fun getMedicationsByUserId(userId: String): List<Medication> {
        return try {
            val response = supabase.postgrest.from("medications")
                .select{
                    filter {
                        eq("user_id", userId)

                    }
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<MedicationDto>()

            
            response.map { MedicationDto.toMedication(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getActiveMedicationsByUserId(userId: String): List<Medication> {
        return try {
            val response = supabase.postgrest.from("medications")
                .select{
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<MedicationDto>()
            
            response.map { MedicationDto.toMedication(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getMedicationById(medicationId: String): Medication? {
        return try {
            val response = supabase.postgrest.from("medications")
                .select{
                    filter {
                        eq("id", medicationId)
                    }
                }
                .decodeSingle<MedicationDto>()
            
            MedicationDto.toMedication(response)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addMedication(medication: Medication): Result<Medication> {
        return try {
            val medicationDto = MedicationDto.fromMedication(medication)
            val response = supabase
                .from("medications")
                .insert(medicationDto){
                    select()
                }.decodeSingle<MedicationDto>()

            // Convert back to your Medication model
            val addedMedication = MedicationDto.toMedication(response)
            Result.success(addedMedication)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMedication(medication: Medication): Result<Medication> {
        return try {
            val medicationDto = MedicationDto.fromMedication(medication.copy(updatedAt = System.currentTimeMillis()))
            val response = supabase.postgrest.from("medications")
                .update(medicationDto){
                    filter {
                        eq("id", medication.id!!)
                    }
                }.decodeSingle<MedicationDto>()
            
            Result.success(MedicationDto.toMedication(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMedication(medicationId: String): Result<Unit> {
        return try {
            supabase.from("medications")
                .delete{
                    filter {
                        eq("id", medicationId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTodaysMedications(userId: String): List<Medication> {
        return try {
            val today = Date()
            val response = supabase.postgrest.from("medications")
                .select{
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                        lte("start_date", today.time)
                        or {
                            gte("end_date", today.time)
                            or {
                                MedicationDto::endDate isExact null
                            }
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<MedicationDto>()
            
            response.map { MedicationDto.toMedication(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getMedicationsForDate(userId: String, date: Date): List<Medication> {
        return try {
            val response = supabase.postgrest.from("medications")
                .select{
                    filter {
                        eq("user_id", userId)
                        eq("is_active", true)
                        lte("start_date", date.time)
                        or {
                            gte("end_date", date.time)
                            or {
                                MedicationDto::endDate isExact null
                            }
                        }
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<MedicationDto>()
            
            response.map { MedicationDto.toMedication(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun observeMedicationsByUserId(userId: String): Flow<List<Medication>> = flow {
        try {
            val medications = getMedicationsByUserId(userId)
            emit(medications)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
