package com.zahid.dailydose.data.repository

import com.zahid.dailydose.domain.model.AuthResponse
import com.zahid.dailydose.domain.model.LoginRequest
import com.zahid.dailydose.domain.model.RegisterRequest
import com.zahid.dailydose.domain.model.User
import com.zahid.dailydose.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : AuthRepository {
    
    private var currentUser: User? = null
    private var isUserLoggedIn = false
    
    init {
        // Check if there's an existing session on initialization
        initializeSession()
    }
    
    private fun initializeSession() {
        val supabaseUser = supabaseClient.auth.currentUserOrNull()
        if (supabaseUser != null) {
            currentUser = convertSupabaseUserToUser(supabaseUser)
            isUserLoggedIn = true
        }
    }
    
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val authResponse = supabaseClient.auth.signInWith(Email) {
                email = request.email
                password = request.password
            }
            
            val user = convertSupabaseUserToUser(supabaseClient.auth.currentUserOrNull())
            currentUser = user
            isUserLoggedIn = true
            
            Result.success(AuthResponse(user = user, token = supabaseClient.auth.currentUserOrNull()?.id ?: ""))
        } catch (e: AuthRestException) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val authResponse = supabaseClient.auth.signUpWith(Email) {
                email = request.email
                password = request.password
            }
            
            val user = convertSupabaseUserToUser(supabaseClient.auth.currentUserOrNull())
            currentUser = user
            isUserLoggedIn = true
            
            Result.success(AuthResponse(user = user, token = supabaseClient.auth.currentUserOrNull()?.id ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        try {
            supabaseClient.auth.signOut()
        } catch (e: Exception) {
            // Log error but don't fail logout
        } finally {
            currentUser = null
            isUserLoggedIn = false
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<UserInfo> {
        return try{
            val result = supabaseClient.auth.updateUser {
                email = newEmail
            }
            Result.success(result)
        }catch (e: Exception){
            Result.failure(e)
        }


    }

    override suspend fun updatePassword(newPass: String): Result<UserInfo> {
        return try{
            val result = supabaseClient.auth.updateUser {
                password = newPass
            }
            Result.success(result)
        }catch (e: Exception){
            Result.failure(e)
        }


    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try{
            val result = supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(result)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        // Check both in-memory state and Supabase session
        val supabaseUser = supabaseClient.auth.currentUserOrNull()
        val isLoggedIn = isUserLoggedIn || supabaseUser != null
        
        // Update in-memory state if Supabase has a session but we don't
        if (supabaseUser != null && !isUserLoggedIn) {
            currentUser = convertSupabaseUserToUser(supabaseUser)
            isUserLoggedIn = true
        }
        
        emit(isLoggedIn)
    }
    
    override suspend fun getCurrentUser(): Result<User> {
        // First check in-memory user
        currentUser?.let { user ->
            return Result.success(user)
        }
        
        // If no in-memory user, check Supabase session
        val supabaseUser = supabaseClient.auth.currentUserOrNull()
        return if (supabaseUser != null) {
            val user = convertSupabaseUserToUser(supabaseUser)
            currentUser = user
            isUserLoggedIn = true
            Result.success(user)
        } else {
            // Try to refresh the session once more
            try {
                supabaseClient.auth.refreshCurrentSession()
                val refreshedUser = supabaseClient.auth.currentUserOrNull()
                if (refreshedUser != null) {
                    val user = convertSupabaseUserToUser(refreshedUser)
                    currentUser = user
                    isUserLoggedIn = true
                    Result.success(user)
                } else {
                    Result.failure(Exception("No user logged in"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("No user logged in"))
            }
        }
    }
    
    private fun convertSupabaseUserToUser(supabaseUser: UserInfo?): User {
        return User(
            id = supabaseUser?.id ?: "",
            email = supabaseUser?.email ?: "",
            createdAt =  System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
