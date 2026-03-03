#!/bin/bash

# Navigate to frontend directory
cd "$(dirname "$0")/../FE"

# Install dependencies if needed
pip3 install flask requests 2>/dev/null

# Kill any process on port 3000
lsof -ti:3000 | xargs kill -9 2>/dev/null

# Run Flask server on port 3000 (port 5000 is used by macOS AirPlay)
export FE_PORT=3000
python3 Extensions.py
