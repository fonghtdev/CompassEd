#!/bin/bash

echo "🟡 Setting up MySQL Database..."
echo "================================"

# Check MySQL connection
if ! mysql -u root -proot -e "SELECT 1" &>/dev/null; then
    echo "❌ Cannot connect to MySQL. Please check:"
    echo "   1. MySQL is running"
    echo "   2. Password is 'root'"
    exit 1
fi

echo "✅ MySQL connection OK"

# Create database if not exists
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS compassed_db;"
echo "✅ Database 'compassed_db' ready"

# Show tables
echo ""
echo "📊 Current tables:"
mysql -u root -proot compassed_db -e "SHOW TABLES;"

echo ""
echo "🎉 MySQL setup complete!"
echo ""
echo "Next steps:"
echo "  1. Terminal 1: bash start-backend.sh"
echo "  2. Terminal 2 (wait for backend): bash start-frontend.sh"
