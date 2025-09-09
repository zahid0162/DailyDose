package com.zahid.dailydose

import android.app.Application
import com.zahid.dailydose.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DailyDoseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@DailyDoseApplication)
            modules(appModule)
        }
    }
}
