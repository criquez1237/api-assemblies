#!/usr/bin/env bash
# Tests for POST /auth/signin

API_HOST=${API_HOST:-http://localhost:8081/api}
EMAIL=${SAMPLE_EMAIL:-user@example.com}
PASSWORD=${SAMPLE_PASSWORD:-Password123}

echo "\n=== Signin: happy path ==="
curl -s -X POST "$API_HOST/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'","password":"'$PASSWORD'"}' | jq .

echo "\n=== Signin: missing password (should 400 or error) ==="
curl -s -X POST "$API_HOST/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"email":"'$EMAIL'"}' | jq .

echo "\n=== Signin: invalid credentials ==="
curl -s -X POST "$API_HOST/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"email":"notfound@example.com","password":"badpass"}' | jq .
