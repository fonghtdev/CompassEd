#!/bin/bash

# Script dừng Backend + Frontend cho CompassED
# Sử dụng: bash stop-all.sh

echo "🛑 Stopping CompassED Application..."
echo ""

# Kill all backend processes
echo "Stopping Backend (Spring Boot)..."
ps aux | grep -E 'mvnw|spring-boot|compassed-api' | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null
lsof -ti:8080 | xargs kill -9 2>/dev/null

# Kill all frontend processes
echo "Stopping Frontend (Flask)..."
ps aux | grep -E 'Extensions.py|Flask' | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null
lsof -ti:3000 | xargs kill -9 2>/dev/null

echo ""
echo "✅ All processes stopped!"
echo "Ports 8080 and 3000 are now free."
