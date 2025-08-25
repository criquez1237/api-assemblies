#!/usr/bin/env bash
# Tests for POST /auth/verify-otp

API_HOST=${API_HOST:-http://localhost:8081/api}
EMAIL=${SAMPLE_EMAIL:-user@example.com}
OTP_CODE=${SAMPLE_OTP:-123456}

echo "\n=== Verify OTP: happy path ==="
curl -s -X POST "$API_HOST/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'","otpCode":"'$OTP_CODE'"}' | jq .

echo "\n=== Verify OTP: invalid code ==="
curl -s -X POST "$API_HOST/auth/verify-otp" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'","otpCode":"000000"}' | jq .
