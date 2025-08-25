#!/usr/bin/env bash
# Tests for POST /auth/refresh

API_HOST=${API_HOST:-http://localhost:8081/api}
REFRESH_TOKEN=${SAMPLE_REFRESH_TOKEN:-"Bearer <refresh-token>"}

echo "\n=== Refresh token (happy path) ==="
curl -s -X POST "$API_HOST/auth/refresh" \
  -H "Authorization: $REFRESH_TOKEN" | jq .

echo "\n=== Refresh token: missing header ==="
curl -s -X POST "$API_HOST/auth/refresh" | jq .
