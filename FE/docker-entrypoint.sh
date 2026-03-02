#!/bin/sh
set -eu

API_BASE="${API_BASE:-http://localhost:8080}"
GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-}"

cat > /app/config.js <<EOF
window.APP_CONFIG = {
  API_BASE: "${API_BASE}",
  GOOGLE_CLIENT_ID: "${GOOGLE_CLIENT_ID}"
};
EOF

exec "$@"
