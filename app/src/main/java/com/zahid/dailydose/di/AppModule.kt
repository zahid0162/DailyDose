package com.zahid.dailydose.di

import com.zahid.dailydose.data.repository.AuthRepositoryImpl
import com.zahid.dailydose.data.repository.AuthStateManager
import com.zahid.dailydose.data.repository.DoseRepositoryImpl
import com.zahid.dailydose.data.repository.HealthMetricRepositoryImpl
import com.zahid.dailydose.data.repository.MedicationRepositoryImpl
import com.zahid.dailydose.data.repository.OnboardingRepositoryImpl
import com.zahid.dailydose.data.repository.PatientRepositoryImpl
import com.zahid.dailydose.data.repository.OnboardingRepository
import com.zahid.dailydose.data.service.NotificationService
import com.zahid.dailydose.domain.repository.AuthRepository
import com.zahid.dailydose.domain.repository.DoseRepository
import com.zahid.dailydose.domain.repository.HealthMetricRepository
import com.zahid.dailydose.domain.repository.MedicationRepository
import com.zahid.dailydose.domain.repository.PatientRepository
import com.zahid.dailydose.presentation.SimpleAuthManager
import com.zahid.dailydose.presentation.SimpleMainViewModel
import com.zahid.dailydose.presentation.auth.LoginViewModel
import com.zahid.dailydose.presentation.auth.RegisterViewModel
import com.zahid.dailydose.presentation.care.CareViewModel
import com.zahid.dailydose.presentation.home.HomeViewModel
import com.zahid.dailydose.presentation.medication.AddMedicationViewModel
import com.zahid.dailydose.presentation.medication.MedicationViewModel
import com.zahid.dailydose.presentation.medication.ViewMedicationViewModel
import com.zahid.dailydose.presentation.onboarding.OnboardingViewModel
import com.zahid.dailydose.presentation.patient.PatientOnboardingViewModel
import com.zahid.dailydose.presentation.splash.SplashViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    
    // Supabase Client
    single<SupabaseClient> { com.zahid.dailydose.data.supabase.SupabaseClient.client }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<PatientRepository> { PatientRepositoryImpl(get()) }
    single<HealthMetricRepository> { HealthMetricRepositoryImpl() }
    single<MedicationRepository> { MedicationRepositoryImpl() }
    single<DoseRepository> { DoseRepositoryImpl() }
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    single<SimpleAuthManager> { SimpleAuthManager(get(), get(), get(), get()) }
    
    // Services
    single<NotificationService> { NotificationService(get()) }
    
    // ViewModels
    viewModel { SimpleMainViewModel() }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { CareViewModel(get()) }
    viewModel { AddMedicationViewModel(get()) }
    viewModel { MedicationViewModel(get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { PatientOnboardingViewModel(get(), get()) }
    viewModel { SplashViewModel(get(), get(),get()) }
    viewModel { ViewMedicationViewModel() }
}
