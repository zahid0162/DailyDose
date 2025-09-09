-- Health Metrics Table
CREATE TABLE IF NOT EXISTS health_metrics (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('BLOOD_PRESSURE_SYSTOLIC', 'BLOOD_PRESSURE_DIASTOLIC', 'HEART_RATE', 'WEIGHT', 'TEMPERATURE')),
    value DECIMAL(10,2) NOT NULL,
    unit TEXT NOT NULL,
    notes TEXT,
    recorded_at BIGINT NOT NULL,
    created_at BIGINT DEFAULT extract(epoch from now()) * 1000,
    updated_at BIGINT DEFAULT extract(epoch from now()) * 1000
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_health_metrics_user_id ON health_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_health_metrics_type ON health_metrics(type);
CREATE INDEX IF NOT EXISTS idx_health_metrics_recorded_at ON health_metrics(recorded_at);
CREATE INDEX IF NOT EXISTS idx_health_metrics_user_type ON health_metrics(user_id, type);

-- RLS (Row Level Security) policies
ALTER TABLE health_metrics ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only access their own health metrics
CREATE POLICY "Users can view their own health metrics" ON health_metrics
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own health metrics" ON health_metrics
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own health metrics" ON health_metrics
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own health metrics" ON health_metrics
    FOR DELETE USING (auth.uid() = user_id);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_health_metrics_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = extract(epoch from now()) * 1000;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to automatically update updated_at
CREATE TRIGGER update_health_metrics_updated_at
    BEFORE UPDATE ON health_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_health_metrics_updated_at();
