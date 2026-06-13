#!/usr/bin/env bash
set -euo pipefail

BASE_FRONTEND="${BASE_FRONTEND:-http://localhost:3000}"
BASE_BACKEND="${BASE_BACKEND:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@32bit.com.tr}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin12345!}"
USER_EMAIL="${USER_EMAIL:-user1@ogr.sakarya.edu.tr}"
USER_PASSWORD="${USER_PASSWORD:-User12345!}"

WORKDIR="${WORKDIR:-/tmp/ats-smoke}"
mkdir -p "${WORKDIR}"
PDF_SMALL="${WORKDIR}/small.pdf"
PDF_20MB="${WORKDIR}/20mb.pdf"
PDF_60MB="${WORKDIR}/60mb.pdf"

create_dummy_pdf() {
  local path="$1"
  local size_bytes="$2"
  {
    printf '%%PDF-1.4\n'
    head -c "$size_bytes" /dev/zero
    printf '\n%%EOF\n'
  } > "$path"
}

json_field() {
  local key="$1"
  if command -v jq >/dev/null 2>&1; then
    jq -r ".${key} // empty"
  else
    sed -n "s/.*\"${key}\":\"\\([^\"]*\\)\".*/\\1/p"
  fi
}

json_first_id() {
  if command -v jq >/dev/null 2>&1; then
    jq -r 'if type=="array" then .[0].id else .id end // empty'
  else
    sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p' | head -n1
  fi
}

echo "[1/12] Create payload files"
create_dummy_pdf "$PDF_SMALL" 2048
create_dummy_pdf "$PDF_20MB" $((20 * 1024 * 1024))
create_dummy_pdf "$PDF_60MB" $((60 * 1024 * 1024))

echo "[2/12] Register user with profile+cv (multipart)"
REGISTER_DATA="$(cat <<JSON
{"email":"${USER_EMAIL}","password":"${USER_PASSWORD}","firstName":"Ali","lastName":"Kaya","classYear":3,"department":"Computer Engineering","englishLevel":"B2","gpa":3.25}
JSON
)"
curl -sS -X POST "${BASE_BACKEND}/api/v1/auth/register" \
  -F "data=${REGISTER_DATA};type=application/json" \
  -F "cv=@${PDF_SMALL};type=application/pdf" || true

echo "[3/12] Read verify token from backend logs (docker compose required)"
VERIFY_TOKEN="$(docker compose logs backend --no-color 2>/dev/null | grep 'Email verification sent' | tail -n1 | sed -E 's/.*token=([a-f0-9-]+).*/\1/' || true)"
if [[ -z "${VERIFY_TOKEN}" ]]; then
  echo "WARN: verify token not found in logs. Skip verify."
else
  curl -sS "${BASE_BACKEND}/api/v1/auth/verify?token=${VERIFY_TOKEN}" >/dev/null
fi

echo "[4/12] Login user/admin"
USER_LOGIN_JSON="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\"}")"
USER_TOKEN="$(printf '%s' "${USER_LOGIN_JSON}" | json_field accessToken)"

ADMIN_LOGIN_JSON="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}")"
ADMIN_TOKEN="$(printf '%s' "${ADMIN_LOGIN_JSON}" | json_field accessToken)"

echo "[5/12] Update profile + upload CV (pdf + octet-stream)"
curl -sS -X PUT "${BASE_BACKEND}/api/v1/me/profile" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ali","lastName":"Kaya","phoneNumber":"5551112233"}' >/dev/null

curl -sS -X POST "${BASE_BACKEND}/api/v1/users/me/documents/cv" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -F "file=@${PDF_SMALL};type=application/pdf" >/dev/null

curl -sS -X POST "${BASE_BACKEND}/api/v1/users/me/documents/cv" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -F "file=@${PDF_SMALL};type=application/octet-stream" >/dev/null

echo "[5.1/12] USER email update forbidden + self password update"
USER_EMAIL_STATUS="$(curl -sS -o /dev/null -w "%{http_code}" -X PUT "${BASE_BACKEND}/api/v1/me/email" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${USER_EMAIL}\"}")"
echo "user_email_update_status=${USER_EMAIL_STATUS}"
curl -sS -X PUT "${BASE_BACKEND}/api/v1/me/password" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"currentPassword\":\"${USER_PASSWORD}\",\"newPassword\":\"${USER_PASSWORD}\"}" >/dev/null

echo "[6/12] Create + publish posting"
POSTING_JSON="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/postings" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"category":"BACKEND","title":"Backend Intern","description":"desc","projectName":"ATS","projectDetails":"details"}')"
POSTING_ID="$(printf '%s' "${POSTING_JSON}" | json_field id)"
curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/postings/${POSTING_ID}/publish" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" >/dev/null

echo "[7/12] Public list/detail + user submit"
curl -sS "${BASE_BACKEND}/api/v1/public/postings" >/dev/null
curl -sS "${BASE_BACKEND}/api/v1/public/postings/${POSTING_ID}" >/dev/null
curl -sS -X POST "${BASE_BACKEND}/api/v1/me/submissions" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"postingId\":${POSTING_ID}}" >/dev/null

echo "[8/12] Duplicate submit should be 409"
DUP_BODY="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/me/submissions" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"postingId\":${POSTING_ID}}")"
DUP_STATUS="$(printf '%s' "${DUP_BODY}" | (jq -r '.errorCode // empty' 2>/dev/null || sed -n 's/.*"errorCode":"\([^"]*\)".*/\1/p'))"
echo "duplicate_submit_error_code=${DUP_STATUS}"

echo "[9/12] Admin attachment upload + public download"
ATT_JSON="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/postings/${POSTING_ID}/attachments" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F "files=@${PDF_SMALL};type=application/pdf")"
ATT_ID="$(printf '%s' "${ATT_JSON}" | json_first_id)"
curl -sS -o /dev/null "${BASE_BACKEND}/api/v1/postings/${POSTING_ID}/attachments/${ATT_ID}/download"

echo "[10/12] Close posting and verify closed download policy"
curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/postings/${POSTING_ID}/close" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" >/dev/null
ANON_STATUS="$(curl -sS -o /dev/null -w "%{http_code}" \
  "${BASE_BACKEND}/api/v1/postings/${POSTING_ID}/attachments/${ATT_ID}/download")"
USER_STATUS="$(curl -sS -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  "${BASE_BACKEND}/api/v1/postings/${POSTING_ID}/attachments/${ATT_ID}/download")"
ADMIN_STATUS="$(curl -sS -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${BASE_BACKEND}/api/v1/postings/${POSTING_ID}/attachments/${ATT_ID}/download")"
echo "closed_download anon=${ANON_STATUS} submitter=${USER_STATUS} admin=${ADMIN_STATUS}"

echo "[11/12] Admin submissions detail + mail job"
SUB_LIST="$(curl -sS -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${BASE_BACKEND}/api/v1/admin/postings/${POSTING_ID}/submissions")"
SUB_ID="$(printf '%s' "${SUB_LIST}" | json_first_id)"
curl -sS -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${BASE_BACKEND}/api/v1/admin/submissions?category=BACKEND&postingStatus=CLOSED&page=0&size=10&sort=submittedAt,desc" >/dev/null
curl -sS -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${BASE_BACKEND}/api/v1/admin/submissions/${SUB_ID}" >/dev/null
MAIL_JOB="$(curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/mail/jobs/posting/${POSTING_ID}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F "data={\"subject\":\"Test\",\"body\":\"Stub\"};type=application/json")"
MAIL_JOB_ID="$(printf '%s' "${MAIL_JOB}" | json_field id)"
curl -sS -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  "${BASE_BACKEND}/api/v1/admin/mail/jobs/${MAIL_JOB_ID}" >/dev/null
curl -sS -X POST "${BASE_BACKEND}/api/v1/admin/mail/jobs/all-students" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F "data={\"subject\":\"All Students\",\"body\":\"Hello\"};type=application/json" >/dev/null

echo "[12/12] Upload limits through frontend proxy"
OK20="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "${BASE_FRONTEND}/api/v1/users/me/documents/cv" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -F "file=@${PDF_20MB};type=application/pdf")"
FAIL60="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "${BASE_FRONTEND}/api/v1/users/me/documents/cv" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -F "file=@${PDF_60MB};type=application/pdf")"
echo "upload_status_via_frontend 20MB=${OK20} 60MB=${FAIL60}"

echo "Smoke test completed."
