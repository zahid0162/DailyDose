package com.zahid.dailydose.data.supabase

object SupabaseConfig {
    // TODO: Replace these with your actual Supabase project details
    // You can find these in your Supabase project settings
    const val SUPABASE_URL = "https://ycaavcziewrwgegdzixc.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InljYWF2Y3ppZXdyd2dlZ2R6aXhjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTcyNDU1NjQsImV4cCI6MjA3MjgyMTU2NH0.oSe84piGGjHaXWA5wMYx9oDpIsDfVR1JJhoVoY_HGkw"
    
    // Database table names
    const val PATIENTS_TABLE = "patients"
    const val MEDICATIONS_TABLE = "medications"
    const val DOSES_TABLE = "doses"
    const val REMINDERS_TABLE = "reminders"
}
