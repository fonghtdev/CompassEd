#!/bin/bash

# Script tự động start Backend + Frontend cho CompassED
# Sử dụng: bash start-all.sh

echo "🚀 Starting CompassED Application..."
echo ""

# 1. Kill old processes
echo "🧹 Cleaning up old processes..."
ps aux | grep -E 'mvnw|spring-boot|compassed-api' | grep -v grep | awk '{print $2}' | xargs kill -9 2>/dev/null
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:3000 | xargs kill -9 2>/dev/null
sleep 2
echo "✅ Old processes cleaned"
echo ""

# 2. Start Backend
echo "🔵 Starting Backend (Spring Boot) on port 8080..."
cd /Users/hoangngoctinh/compassED/ED/BE/compassed-api
./mvnw spring-boot:run > /tmp/compassed-backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"
echo "Backend log: /tmp/compassed-backend.log"
echo ""

# 3. Wait for backend to start
echo "⏳ Waiting for backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "✅ Backend is ready!"
        break
    fi
    sleep 2
    echo -n "."
done
echo ""

# 4. Start Frontend
echo "🔵 Starting Frontend (Flask) on port 3000..."
cd /Users/hoangngoctinh/compassED/ED/FE
python3 Extensions.py > /tmp/compassed-frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"
echo "Frontend log: /tmp/compassed-frontend.log"
echo ""

# 5. Wait for frontend to start
echo "⏳ Waiting for frontend to start..."
sleep 3
if lsof -ti:3000 > /dev/null 2>&1; then
    echo "✅ Frontend is ready!"
else
    echo "❌ Frontend failed to start. Check /tmp/compassed-frontend.log"
fi
echo ""

# 6. Summary
echo "=========================================="
echo "🎉 CompassED Application Started!"
echo "=========================================="
echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo ""
echo "📝 Logs:"
echo "  Backend:  tail -f /tmp/compassed-backend.log"
echo "  Frontend: tail -f /tmp/compassed-frontend.log"
echo ""
echo "🛑 To stop:"
echo "  kill $BACKEND_PID $FRONTEND_PID"
echo "  or run: bash stop-all.sh"
echo "=========================================="
echo ""
echo "✨ Open browser: http://localhost:3000/auth"
