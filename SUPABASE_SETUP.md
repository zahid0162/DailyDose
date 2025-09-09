# Supabase Setup for DailyDose App

## Prerequisites
1. Create a Supabase account at [supabase.com](https://supabase.com)
2. Create a new project in your Supabase dashboard

## Configuration

### 1. Update Supabase Configuration
Edit `app/src/main/java/com/zahid/dailydose/data/supabase/SupabaseConfig.kt`:

```kotlin
object SupabaseConfig {
    // Replace with your actual Supabase project details
    const val SUPABASE_URL = "https://your-project-id.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key"
    
    // Database table names
    const val PATIENTS_TABLE = "patients"
    const val MEDICATIONS_TABLE = "medications"
    const val DOSES_TABLE = "doses"
    const val REMINDERS_TABLE = "reminders"
}
```

### 2. Get Your Supabase Credentials
1. Go to your Supabase project dashboard
2. Navigate to **Settings** → **API**
3. Copy your **Project URL** and **anon public** key
4. Update the `SUPABASE_URL` and `SUPABASE_ANON_KEY` in `SupabaseConfig.kt`

## Database Schema

### Create the following tables in your Supabase database:

#### 1. Patients Table
```sql
CREATE TABLE patients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    personal_info JSONB NOT NULL,
    medical_info JSONB NOT NULL,
    emergency_info JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE patients ENABLE ROW LEVEL SECURITY;

-- Create policy for users to access their own data
CREATE POLICY "Users can view own patient data" ON patients
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own patient data" ON patients
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own patient data" ON patients
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own patient data" ON patients
    FOR DELETE USING (auth.uid() = user_id);
```

#### 2. Medications Table (for future use)
```sql
CREATE TABLE medications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id UUID REFERENCES patients(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    dosage TEXT NOT NULL,
    frequency TEXT NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE medications ENABLE ROW LEVEL SECURITY;

-- Create policy for users to access their own medications
CREATE POLICY "Users can view own medications" ON medications
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM patients 
            WHERE patients.id = medications.patient_id 
            AND patients.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert own medications" ON medications
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM patients 
            WHERE patients.id = medications.patient_id 
            AND patients.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can update own medications" ON medications
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM patients 
            WHERE patients.id = medications.patient_id 
            AND patients.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete own medications" ON medications
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM patients 
            WHERE patients.id = medications.patient_id 
            AND patients.user_id = auth.uid()
        )
    );
```

## Authentication Setup

### Enable Email Authentication
1. Go to **Authentication** → **Settings** in your Supabase dashboard
2. Make sure **Enable email confirmations** is configured as needed
3. Configure **Site URL** for your app (for development: `http://localhost:3000`)

### Optional: Configure Email Templates
1. Go to **Authentication** → **Email Templates**
2. Customize the email templates for signup, login, etc.

## Features Included

✅ **Authentication**: Email/password signup and login  
✅ **Database**: PostgreSQL with Row Level Security  
✅ **Real-time**: Real-time subscriptions (ready for future features)  
✅ **Storage**: File storage (ready for future features like profile pictures)  
✅ **Functions**: Edge functions (ready for future features)  

## Next Steps

1. Update the Supabase configuration with your project details
2. Run the SQL scripts to create the database tables
3. Test the authentication flow in your app
4. The app is now ready to use Supabase as the backend!

## Troubleshooting

- Make sure your Supabase URL and API key are correct
- Check that the database tables are created with the correct schema
- Verify that Row Level Security policies are set up correctly
- Check the Supabase logs in your dashboard for any errors
