#!/usr/bin/env bash
# Tests for POST /auth/signup (multipart/form-data)

API_HOST=${API_HOST:-http://localhost:8081/api}
EMAIL=${SAMPLE_EMAIL:-newuser@example.com}
PASSWORD=${SAMPLE_PASSWORD:-Password123}
NAMES="Juan"
SURNAMES="Perez"
IMAGE_PATH=${IMAGE_PATH:-./fixtures/avatar.png}

echo "\n=== Signup multipart: happy path (with image) ==="
# Ensure IMAGE_PATH exists or adjust path
curl -s -X POST "$API_HOST/auth/signup" \
  -F "email=$EMAIL" \
  -F "password=$PASSWORD" \
  -F "names=$NAMES" \
  -F "surnames=$SURNAMES" \
  -F "imagePerfil=@$IMAGE_PATH" | jq .

echo "\n=== Signup multipart: without image ==="
curl -s -X POST "$API_HOST/auth/signup" \
  -F "email=$EMAIL" \
  -F "password=$PASSWORD" \
  -F "names=$NAMES" \
  -F "surnames=$SURNAMES" | jq .
