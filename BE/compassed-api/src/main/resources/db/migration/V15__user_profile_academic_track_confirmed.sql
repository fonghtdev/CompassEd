ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS academic_track_confirmed BIT(1) NOT NULL DEFAULT b'0';

UPDATE user_profiles
SET academic_track_confirmed = b'1'
WHERE academic_track IS NOT NULL
  AND TRIM(academic_track) <> '';
