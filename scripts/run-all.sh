#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "🚀 Starting CompassEd..."
echo ""
echo "📦 Backend will start on: http://localhost:8080"
echo "🌐 Frontend will start on: http://127.0.0.1:3000"
echo "⚠️  Note: Using port 3000 because macOS uses port 5000 for AirPlay"
echo ""
echo "Opening 2 terminals..."
echo "⚠️  Keep both terminal windows open!"
echo ""

# Start backend in new terminal
osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR' && ./run-be.sh\""

# Wait a bit before starting frontend
sleep 2

# Start frontend in new terminal
osascript -e "tell application \"Terminal\" to do script \"cd '$SCRIPT_DIR' && ./run-fe.sh\""

echo "✅ Both terminals opened!"
echo ""
echo "🌐 Access the app at: http://127.0.0.1:3000/landing"
echo "⚠️  Important: Use /landing route, NOT /template/landingPage.html"
