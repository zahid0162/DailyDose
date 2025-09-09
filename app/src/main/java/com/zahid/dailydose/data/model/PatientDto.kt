package com.zahid.dailydose.data.model

import com.zahid.dailydose.domain.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.util.Date

@Serializable
data class PatientDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("personal_info")
    val personalInfo: String,
    @SerialName("medical_info")
    val medicalInfo: String,
    @SerialName("emergency_contact_name")
    val emergencyContactName: String,
    @SerialName("emergency_contact_phone")
    val emergencyContactPhone: String,
    @SerialName("blood_group")
    val bloodGroup: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        
        fun fromPatient(patient: Patient): PatientDto {
            val personalInfoJson = buildJsonObject {
                put("full_name", patient.personalInfo.fullName)
                put("date_of_birth", patient.personalInfo.dateOfBirth.time)
                put("gender", patient.personalInfo.gender.name)
                patient.personalInfo.contactNumber?.let { put("contact_number", it) }
            }
            
            val medicalInfoJson = buildJsonObject {
                putJsonArray("medical_conditions") {
                    patient.medicalInfo.medicalConditions.forEach { condition ->
                        add(condition)
                    }
                }
                putJsonArray("allergies") {
                    patient.medicalInfo.allergies.forEach { allergy ->
                        add(allergy)
                    }
                }
                patient.medicalInfo.primaryDoctorName?.let { put("primary_doctor_name", it) }
                patient.medicalInfo.primaryDoctorContact?.let { put("primary_doctor_contact", it) }
            }
            
            return PatientDto(
                id = patient.id,
                userId = patient.userId,
                personalInfo = json.encodeToString(personalInfoJson),
                medicalInfo = json.encodeToString(medicalInfoJson),
                emergencyContactName = patient.emergencyContactName,
                emergencyContactPhone = patient.emergencyContactPhone,
                bloodGroup = patient.bloodGroup.name,
                createdAt = patient.createdAt,
                updatedAt = patient.updatedAt
            )
        }
        
        fun toPatient(dto: PatientDto): Patient {
            val personalInfoJson = json.parseToJsonElement(dto.personalInfo) as JsonObject
            val medicalInfoJson = json.parseToJsonElement(dto.medicalInfo) as JsonObject
            
            return Patient(
                id = dto.id,
                userId = dto.userId,
                personalInfo = PersonalInfo(
                    fullName = personalInfoJson["full_name"]?.toString()?.trim('"') ?: "",
                    dateOfBirth = Date(
                        personalInfoJson["date_of_birth"]?.toString()?.toLongOrNull() ?: 0
                    ),
                    gender = Gender.valueOf(personalInfoJson["gender"]?.toString()?.trim('"') ?: "OTHER"),
                    contactNumber = personalInfoJson["contact_number"]?.toString()?.trim('"')
                ),
                medicalInfo = MedicalInfo(
                    medicalConditions = parseStringList(medicalInfoJson["medical_conditions"]),
                    allergies = parseStringList(medicalInfoJson["allergies"]),
                    primaryDoctorName = medicalInfoJson["primary_doctor_name"]?.toString()?.trim('"'),
                    primaryDoctorContact = medicalInfoJson["primary_doctor_contact"]?.toString()?.trim('"')
                ),
                emergencyContactName = dto.emergencyContactName,
                emergencyContactPhone = dto.emergencyContactPhone,
                bloodGroup = BloodGroup.valueOf(dto.bloodGroup),
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
        
        private fun parseStringList(value: kotlinx.serialization.json.JsonElement?): List<String> {
            return try {
                when (value) {
                    is JsonArray -> {
                        value.map { it.toString().trim('"') }
                    }
                    is kotlinx.serialization.json.JsonPrimitive -> {
                        val stringValue = value.toString().trim('"')
                        if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
                            json.decodeFromString<List<String>>(stringValue)
                        } else {
                            emptyList()
                        }
                    }
                    else -> {
                        val stringValue = value?.toString()?.trim('"') ?: "[]"
                        if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
                            json.decodeFromString<List<String>>(stringValue)
                        } else {
                            emptyList()
                        }
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
    }
}
