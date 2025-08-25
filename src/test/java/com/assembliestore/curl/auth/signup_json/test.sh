#!/usr/bin/env bash
# Tests for POST /auth/signup (JSON)

API_HOST=${API_HOST:-http://localhost:8081/api}
EMAIL=${SAMPLE_EMAIL:-newuser@example.com}
PASSWORD=${SAMPLE_PASSWORD:-Password123}
NAMES="Juan"
SURNAMES="Perez"

echo "\n=== Signup JSON: happy path ==="
curl -s -X POST "$API_HOST/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'","password":"'$PASSWORD'","names":"'$NAMES'","surnames":"'$SURNAMES'"}' | jq .

echo "\n=== Signup JSON: missing email ==="
curl -s -X POST "$API_HOST/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"password":"'$PASSWORD'","names":"'$NAMES'","surnames":"'$SURNAMES'"}' | jq .

echo "\n=== Signup JSON: duplicate email (if exists) ==="
curl -s -X POST "$API_HOST/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'","password":"'$PASSWORD'","names":"'$NAMES'","surnames":"'$SURNAMES'"}' | jq .
