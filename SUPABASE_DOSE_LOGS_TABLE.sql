-- Create dose_logs table in Supabase
-- This table ONLY contains doses that have been taken by the user
-- It does NOT contain all scheduled doses - only the ones actually taken
-- Run this SQL in your Supabase SQL editor

CREATE TABLE IF NOT EXISTS dose_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    dose_time BIGINT NOT NULL, -- Timestamp when the dose was scheduled
    scheduled_time TEXT NOT NULL, -- The scheduled time string (e.g., "08:00")
    status TEXT NOT NULL DEFAULT 'TAKEN' CHECK (status = 'TAKEN'), -- Only taken doses are logged
    taken_at BIGINT, -- Timestamp when the dose was actually taken
    notes TEXT,
    created_at BIGINT DEFAULT extract(epoch from now()) * 1000,
    updated_at BIGINT DEFAULT extract(epoch from now()) * 1000
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_dose_logs_user_id ON dose_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_dose_logs_medication_id ON dose_logs(medication_id);
CREATE INDEX IF NOT EXISTS idx_dose_logs_dose_time ON dose_logs(dose_time);
CREATE INDEX IF NOT EXISTS idx_dose_logs_status ON dose_logs(status);
CREATE INDEX IF NOT EXISTS idx_dose_logs_user_date ON dose_logs(user_id, dose_time);

-- Enable Row Level Security (RLS)
ALTER TABLE dose_logs ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
-- Users can only see their own dose logs
CREATE POLICY "Users can view their own dose logs" ON dose_logs
    FOR SELECT USING (auth.uid() = user_id);

-- Users can insert their own dose logs
CREATE POLICY "Users can insert their own dose logs" ON dose_logs
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Users can update their own dose logs
CREATE POLICY "Users can update their own dose logs" ON dose_logs
    FOR UPDATE USING (auth.uid() = user_id);

-- Users can delete their own dose logs
CREATE POLICY "Users can delete their own dose logs" ON dose_logs
    FOR DELETE USING (auth.uid() = user_id);

-- Create a function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_dose_logs_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = extract(epoch from now()) * 1000;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_dose_logs_updated_at 
    BEFORE UPDATE ON dose_logs 
    FOR EACH ROW 
    EXECUTE FUNCTION update_dose_logs_updated_at_column();
