package com.zahid.dailydose.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val password: String? = null, // Don't store password in production
    val createdAt: Long,
    val updatedAt: Long
)
