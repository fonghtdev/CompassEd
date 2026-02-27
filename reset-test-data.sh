#!/bin/bash

# Reset Test Data Script
# This script clears all test data to start fresh testing

echo "🧹 Clearing test data..."

mysql -u root -proot compassed_db << 'EOF'
SET FOREIGN_KEY_CHECKS=0;

-- Clear test user data
DELETE FROM final_test_attempts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM mini_test_attempts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM user_module_progress WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM payments WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM subscriptions WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM placement_attempts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM user_subject_free_attempts WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM users WHERE email LIKE '%test%';

-- Reset auto increment
ALTER TABLE users AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS=1;

-- Show remaining data count
SELECT 
    (SELECT COUNT(*) FROM users) as users_count,
    (SELECT COUNT(*) FROM payments) as payments_count,
    (SELECT COUNT(*) FROM subscriptions) as subscriptions_count,
    (SELECT COUNT(*) FROM user_module_progress) as progress_count;

EOF

echo "✅ Test data cleared!"
echo ""
echo "📊 Database status:"
mysql -u root -proot compassed_db -e "SELECT COUNT(*) as total_users FROM users;"
echo ""
echo "🎯 Ready for fresh testing!"
echo "   1. Go to http://localhost:3000"
echo "   2. Register new user"
echo "   3. Follow TEST_FLOW_COMPLETE.md"
