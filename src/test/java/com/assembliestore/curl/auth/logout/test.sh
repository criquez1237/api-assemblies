#!/usr/bin/env bash
# Tests for POST /auth/logout

API_HOST=${API_HOST:-http://localhost:8081/api}
AUTH_TOKEN=${SAMPLE_AUTH_TOKEN:-"Bearer <access-token>"}

echo "\n=== Logout (happy path) ==="
curl -s -X POST "$API_HOST/auth/logout" \
  -H "Authorization: $AUTH_TOKEN" | jq .

echo "\n=== Logout: missing header ==="
curl -s -X POST "$API_HOST/auth/logout" | jq .
