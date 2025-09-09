package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    suspend fun getMedicationsByUserId(userId: String): List<Medication>
    suspend fun getActiveMedicationsByUserId(userId: String): List<Medication>
    suspend fun getMedicationById(medicationId: String): Medication?
    suspend fun addMedication(medication: Medication): Result<Medication>
    suspend fun updateMedication(medication: Medication): Result<Medication>
    suspend fun deleteMedication(medicationId: String): Result<Unit>
    suspend fun getTodaysMedications(userId: String): List<Medication>
    suspend fun getMedicationsForDate(userId: String, date: java.util.Date): List<Medication>
    fun observeMedicationsByUserId(userId: String): Flow<List<Medication>>
}
