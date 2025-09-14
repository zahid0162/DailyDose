package com.zahid.dailydose.domain.repository

import com.zahid.dailydose.domain.model.AuthResponse
import com.zahid.dailydose.domain.model.LoginRequest
import com.zahid.dailydose.domain.model.RegisterRequest
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout()

    suspend fun updateEmail(newEmail: String): Result<UserInfo>
    suspend fun updatePassword(newPass: String): Result<UserInfo>

    suspend fun resetPassword(email:String):Result<Unit>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getCurrentUser(): Result<com.zahid.dailydose.domain.model.User>
}
