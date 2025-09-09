package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.Dose
import kotlinx.coroutines.flow.Flow

interface DoseRepository {
    suspend fun getTodaysDoses(userId: String): List<Dose>
    suspend fun getDosesByMedicationId(medicationId: String): List<Dose>
    suspend fun markDoseAsTaken(doseId: String): Result<Boolean>
    suspend fun createDoseLog(dose: Dose): Result<Boolean>
    suspend fun updateDoseStatus(doseId: String, status: com.zahid.dailydose.domain.model.DoseStatus): Result<Boolean>
    suspend fun getDoseById(doseId: String): Dose?
    fun observeTodaysDoses(userId: String): Flow<List<Dose>>
}
