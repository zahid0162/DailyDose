package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.AuthResponse
import com.zahid.dailydose.domain.model.LoginRequest
import com.zahid.dailydose.domain.model.RegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getCurrentUser(): Result<com.zahid.dailydose.domain.model.User>
}
