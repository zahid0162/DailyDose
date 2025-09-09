package com.zahid.dailydose.data.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun isOnboardingCompletedFlow(): Flow<Boolean>
}
