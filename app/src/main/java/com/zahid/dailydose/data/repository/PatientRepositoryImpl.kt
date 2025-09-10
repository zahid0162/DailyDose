package com.zahid.dailydose.data.repository

import android.util.Log
import com.zahid.dailydose.data.model.PatientDto
import com.zahid.dailydose.data.supabase.SupabaseConfig
import com.zahid.dailydose.domain.model.Patient
import com.zahid.dailydose.domain.repository.PatientRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PatientRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : PatientRepository {
    
    override suspend fun createPatient(patient: Patient): Result<Boolean> {
        return try {
            val patientDto = PatientDto.fromPatient(patient)
            val result = supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .insert(patientDto)
            Log.d("result json", result.data)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePatient(patient: Patient): Result<Patient> {
        return try {
            val patientDto = PatientDto.fromPatient(patient)
            val result = supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .update(patientDto) {
                    filter {
                        eq("id", patient.id)
                    }
                    select()
                }
                .decodeSingleOrNull<PatientDto>()
            
            result?.let { 
                Result.success(PatientDto.toPatient(it))
            } ?: Result.failure(Exception("Failed to update patient - no data returned"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPatient(userId: String): Result<Patient> {
        return try {
            val result = supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<PatientDto>()
            
            result?.let { 
                Result.success(PatientDto.toPatient(it))
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePatient(patientId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .delete {
                    filter {
                        eq("id", patientId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getPatientFlow(userId: String): Flow<Patient?> = flow {
        try {
            val result = supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<PatientDto>()
            
            emit(result?.let { PatientDto.toPatient(it) })
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    override suspend fun hasPatientProfile(userId: String): Boolean {
        return try {
            val result = supabaseClient.postgrest.from(SupabaseConfig.PATIENTS_TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<PatientDto>()
            
            result != null
        } catch (e: Exception) {
            false
        }
    }
}
