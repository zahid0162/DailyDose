package com.zahid.dailydose.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.core.content.edit

class OnboardingRepositoryImpl(
    private val context: Context
) : OnboardingRepository {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "onboarding_prefs", 
        Context.MODE_PRIVATE
    )
    
    private val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
    
    override suspend fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(ONBOARDING_COMPLETED_KEY, false)
    }
    
    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit { putBoolean(ONBOARDING_COMPLETED_KEY, completed) }
    }
    
    override fun isOnboardingCompletedFlow(): Flow<Boolean> = flow {
        emit(isOnboardingCompleted())
    }
}
