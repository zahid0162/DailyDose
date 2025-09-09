-- Create medications table in Supabase
-- This is a SEPARATE table from patient profiles
-- Medications are managed independently from general patient information
-- Run this SQL in your Supabase SQL editor

CREATE TABLE IF NOT EXISTS medications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    form TEXT NOT NULL CHECK (form IN ('TABLET', 'CAPSULE', 'SYRUP', 'INJECTION', 'CREAM', 'DROPS', 'PATCH', 'OTHER')),
    strength TEXT NOT NULL,
    dosage TEXT NOT NULL,
    start_date BIGINT NOT NULL,
    end_date BIGINT,
    is_ongoing BOOLEAN DEFAULT false,
    times_per_day INTEGER NOT NULL CHECK (times_per_day > 0 AND times_per_day <= 10),
    specific_times TEXT NOT NULL, -- JSON array as string
    meal_timing TEXT CHECK (meal_timing IN ('BEFORE_MEAL', 'AFTER_MEAL', 'WITH_MEAL', 'ON_EMPTY_STOMACH', 'ANYTIME')),
    reminders_enabled BOOLEAN DEFAULT true,
    reminder_type TEXT DEFAULT 'DEFAULT' CHECK (reminder_type IN ('DEFAULT', 'SILENT', 'LOUD')),
    prescribed_by TEXT,
    prescription_file_url TEXT,
    notes TEXT,
    refill_count INTEGER CHECK (refill_count > 0),
    category TEXT CHECK (category IN ('DIABETES', 'HEART', 'BLOOD_PRESSURE', 'PAIN_RELIEF', 'VITAMINS', 'ANTIBIOTICS', 'MENTAL_HEALTH', 'RESPIRATORY', 'DIGESTIVE', 'GENERAL')),
    is_active BOOLEAN DEFAULT true,
    created_at BIGINT DEFAULT extract(epoch from now()) * 1000,
    updated_at BIGINT DEFAULT extract(epoch from now()) * 1000
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_medications_user_id ON medications(user_id);
CREATE INDEX IF NOT EXISTS idx_medications_user_id_active ON medications(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_medications_start_date ON medications(start_date);
CREATE INDEX IF NOT EXISTS idx_medications_end_date ON medications(end_date);

-- Enable Row Level Security (RLS)
ALTER TABLE medications ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
-- Users can only see their own medications
CREATE POLICY "Users can view their own medications" ON medications
    FOR SELECT USING (auth.uid() = user_id);

-- Users can insert their own medications
CREATE POLICY "Users can insert their own medications" ON medications
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Users can update their own medications
CREATE POLICY "Users can update their own medications" ON medications
    FOR UPDATE USING (auth.uid() = user_id);

-- Users can delete their own medications
CREATE POLICY "Users can delete their own medications" ON medications
    FOR DELETE USING (auth.uid() = user_id);

-- Create a function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = extract(epoch from now()) * 1000;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_medications_updated_at 
    BEFORE UPDATE ON medications 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
