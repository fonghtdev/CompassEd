#!/bin/bash

echo "🔵 Starting Backend Server..."
echo "================================"

cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api

# Kill any existing backend process
lsof -ti:8080 | xargs kill -9 2>/dev/null

# Start backend
./mvnw spring-boot:run
