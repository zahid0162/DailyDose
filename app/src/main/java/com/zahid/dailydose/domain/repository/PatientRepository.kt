package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.Patient
import kotlinx.coroutines.flow.Flow

interface PatientRepository {
    suspend fun createPatient(patient: Patient): Result<Boolean>
    suspend fun updatePatient(patient: Patient): Result<Patient>
    suspend fun getPatient(userId: String): Result<Patient>
    suspend fun deletePatient(patientId: String): Result<Unit>
    fun getPatientFlow(userId: String): Flow<Patient?>
    suspend fun hasPatientProfile(userId: String): Boolean
}
