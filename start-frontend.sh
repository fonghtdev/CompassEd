#!/bin/bash

echo "🟢 Starting Frontend Server..."
echo "================================"

cd /Users/hoangngoctinh/compassED/ED/FE

# Kill any existing frontend process
lsof -ti:3000 | xargs kill -9 2>/dev/null

# Install dependencies if needed
python3 -m pip install flask requests --quiet

# Start frontend
python3 Extensions.py
