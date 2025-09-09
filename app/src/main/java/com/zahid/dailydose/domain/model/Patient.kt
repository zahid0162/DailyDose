package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

@Serializable
data class Patient(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("personal_info")
    val personalInfo: PersonalInfo,
    @SerialName("medical_info")
    val medicalInfo: MedicalInfo,
    @SerialName("emergency_contact_name")
    val emergencyContactName: String,
    @SerialName("emergency_contact_phone")
    val emergencyContactPhone: String,
    @SerialName("blood_group")
    val bloodGroup: BloodGroup,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long
)

@Serializable
data class PersonalInfo(
    @SerialName("full_name")
    val fullName: String,
    @SerialName("date_of_birth")
    @Serializable(with = DateSerializer::class)
    val dateOfBirth: Date,
    val gender: Gender,
    @SerialName("contact_number")
    val contactNumber: String? = null
)

@Serializable
data class MedicalInfo(
    @SerialName("medical_conditions")
    val medicalConditions: List<String>,
    val allergies: List<String>,
    @SerialName("primary_doctor_name")
    val primaryDoctorName: String? = null,
    @SerialName("primary_doctor_contact")
    val primaryDoctorContact: String? = null
)

@Serializable
enum class Gender {
    MALE, FEMALE, OTHER
}

@Serializable
enum class BloodGroup {
    A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE,
    AB_POSITIVE, AB_NEGATIVE, O_POSITIVE, O_NEGATIVE
}

// Custom serializer for Date objects
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)
    
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time)
    }
    
    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeLong())
    }
}
