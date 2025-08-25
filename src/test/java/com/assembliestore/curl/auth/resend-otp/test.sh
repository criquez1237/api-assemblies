#!/usr/bin/env bash
# Tests for POST /auth/resend-otp

API_HOST=${API_HOST:-http://localhost:8081/api}
EMAIL=${SAMPLE_EMAIL:-user@example.com}

echo "\n=== Resend OTP: happy path (registered & not verified) ==="
curl -s -X POST "$API_HOST/auth/resend-otp" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'"}' | jq .

echo "\n=== Resend OTP: not registered (should error) ==="
curl -s -X POST "$API_HOST/auth/resend-otp" \
  -H "Content-Type: application/json" \
  -d '{"email":"notfound@example.com"}' | jq .

echo "\n=== Resend OTP: already verified (should error) ==="
# Replace with an email that is already verified in your DB
curl -s -X POST "$API_HOST/auth/resend-otp" \
  -H "Content-Type: application/json" \
  -d '{"email":"verified@example.com"}' | jq .
