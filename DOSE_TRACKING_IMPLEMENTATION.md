# Dose Tracking Feature Implementation

## Overview
This document describes the implementation of a complex dose tracking feature for the DailyDose Android application. The feature calculates medication doses in real-time based on medication schedules and allows users to mark doses as taken.

## Key Features Implemented

### 1. Real-time Dose Calculation
- **DoseCalculationService**: Calculates today's doses based on medication schedules
- Considers medication start/end dates and active status
- Determines dose status (UPCOMING, DUE, TAKEN, MISSED, SKIPPED) based on current time
- Handles multiple doses per day for each medication

### 2. Database Schema
- **dose_logs table**: ONLY contains doses that have been taken by the user
- Includes medication_id, user_id, dose_time, scheduled_time, status (always 'TAKEN'), taken_at, notes
- Proper indexing and Row Level Security (RLS) policies
- Automatic timestamp updates
- Does NOT store all scheduled doses - only the ones actually taken

### 3. Data Models
- **Dose**: Core domain model for dose tracking
- **DoseDto**: Data transfer object for Supabase communication
- **EnrichedDose**: Enhanced dose model with medication information
- **DoseStatus**: Enum for dose states (UPCOMING, DUE, TAKEN, MISSED, SKIPPED)

### 4. Repository Layer
- **DoseRepository**: Interface for dose operations
- **DoseRepositoryImpl**: Implementation with Supabase integration
- Methods for CRUD operations, status updates, and dose logging

### 5. Home Screen Updates
- **Real-time Statistics**: Shows total, taken, and pending dose counts
- **Dose Cards**: Display individual doses with:
  - Time and AM/PM indicator
  - Medication name, strength, form, and dosage
  - Meal timing instructions
  - Status with color coding
  - Checkbox for marking as taken (or checkmark if already taken)

### 6. Status Management
- **UPCOMING**: More than 30 minutes in the future (can be marked as taken)
- **DUE**: Within 30 minutes (past or future) (can be marked as taken)
- **TAKEN**: Found in dose_logs table (successfully taken)
- **MISSED**: More than 30 minutes past scheduled time (cannot be marked as taken)
- **SKIPPED**: Intentionally skipped by user (cannot be marked as taken)

## Technical Implementation Details

### Dose Calculation Logic
```kotlin
// Status calculation based on time difference
when {
    timeDifference > 30 * 60 * 1000 -> DoseStatus.UPCOMING
    timeDifference > -30 * 60 * 1000 -> DoseStatus.DUE
    else -> DoseStatus.MISSED
}
```

### Data Flow
1. Load user's active medications for today
2. Calculate doses based on medication schedules
3. Get taken doses from database (only doses that were actually taken)
4. Determine status for each calculated dose:
   - If found in logs → TAKEN
   - If not in logs + time passed → MISSED
   - If not in logs + time not passed → UPCOMING/DUE
5. Enrich with medication information
6. Display in UI with real-time statistics

### Database Integration
- Automatic dose log creation when marking as taken
- Proper error handling and user feedback
- Efficient queries with proper indexing
- Row Level Security for data protection

## Files Created/Modified

### New Files
- `domain/model/Dose.kt` - Core dose model
- `domain/model/EnrichedDose.kt` - Enhanced dose with medication info
- `data/model/DoseDto.kt` - Data transfer object
- `domain/repository/DoseRepository.kt` - Repository interface
- `data/repository/DoseRepositoryImpl.kt` - Repository implementation
- `data/service/DoseCalculationService.kt` - Dose calculation logic
- `SUPABASE_DOSE_LOGS_TABLE.sql` - Database schema

### Modified Files
- `presentation/home/HomeScreen.kt` - Updated UI for dose display
- `presentation/home/HomeViewModel.kt` - Added dose management logic
- `di/AppModule.kt` - Added dependency injection for new components

## Usage Instructions

### For Users
1. View today's medication doses on the home screen
2. See real-time status updates (upcoming, due, missed)
3. Mark doses as taken using the checkbox
4. View comprehensive statistics (total, taken, pending)

### For Developers
1. Run the SQL script in Supabase to create the dose_logs table
2. The feature automatically calculates doses based on existing medications
3. Doses are created on-demand when first viewed
4. Status updates happen in real-time based on current time

## Future Enhancements
- Push notifications for due doses
- Dose history and analytics
- Medication adherence tracking
- Customizable reminder windows
- Batch dose operations
- Offline support with sync

## Testing Considerations
- Test with different time zones
- Verify status transitions (upcoming → due → missed)
- Test dose marking functionality
- Validate database operations
- Check error handling scenarios
