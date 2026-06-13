$ErrorActionPreference = "Stop"

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:3000/api/v1" }
$MailpitUrl = if ($env:MAILPIT_URL) { $env:MAILPIT_URL } else { "http://localhost:8025/api/v1" }
$AdminEmail = if ($env:ADMIN_EMAIL) { $env:ADMIN_EMAIL } else { "admin@32bit.com.tr" }
$AdminPassword = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "Admin12345!" }
$SamplePdf = Join-Path $PSScriptRoot "sample/sample.pdf"

if (!(Test-Path $SamplePdf)) {
    throw "Sample PDF not found: $SamplePdf"
}
$RunDir = Join-Path ([System.IO.Path]::GetTempPath()) ("ats-task-smoke-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $RunDir -Force | Out-Null

function Write-Step([string]$message) {
    Write-Host "`n=== $message ===" -ForegroundColor Cyan
}

function Assert-Status([string]$name, [int]$actual, [int[]]$expected) {
    if ($expected -notcontains $actual) {
        throw "$name failed. Expected status $($expected -join ',') but got $actual"
    }
}

function Wait-Healthy([string]$serviceName, [int]$maxRetries = 40) {
    for ($i = 1; $i -le $maxRetries; $i++) {
        $status = (docker inspect $serviceName --format "{{.State.Health.Status}}").Trim()
        if ($status -eq "healthy") {
            return
        }
        Start-Sleep -Seconds 2
    }
    throw "$serviceName did not become healthy"
}

Write-Step "Compose up"
docker compose up -d --build | Out-Host
Wait-Healthy "ats-backend"
Wait-Healthy "ats-frontend"

Write-Step "Admin login"
$adminLoginBody = @{ email = $AdminEmail; password = $AdminPassword } | ConvertTo-Json -Compress
$adminLoginResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/auth/login" -ContentType "application/json" -Body $adminLoginBody
$adminToken = $adminLoginResp.accessToken
if ([string]::IsNullOrWhiteSpace($adminToken)) {
    throw "Admin token is empty"
}

Write-Step "Register + verify student"
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$studentEmail = "task_user_$stamp@ogr.sakarya.edu.tr"
$studentPassword = "Task12345!"
$registerJsonPath = Join-Path $RunDir "register-data.$stamp.json"
$registerPayload = @{
    email = $studentEmail
    password = $studentPassword
    firstName = "Task"
    lastName = "Student"
    classYear = 3
    department = "Computer Engineering"
    englishLevel = "B2"
    gpa = 3.4
} | ConvertTo-Json -Compress
Set-Content -Path $registerJsonPath -Value $registerPayload -Encoding UTF8

$registerHeadersPath = Join-Path $RunDir "register-headers.$stamp.txt"
$registerBodyPath = Join-Path $RunDir "register-body.$stamp.json"
curl.exe -sS -D $registerHeadersPath -o $registerBodyPath -X POST "$BaseUrl/auth/register" `
  -F "data=@$registerJsonPath;type=application/json" `
  -F "cv=@$SamplePdf;type=application/pdf" | Out-Null

$registerStatus = [int]((Get-Content $registerHeadersPath | Select-String "HTTP/" | Select-Object -Last 1).ToString().Split(" ")[1])
Assert-Status "Register" $registerStatus @(201)

Start-Sleep -Seconds 2
$messages = Invoke-RestMethod -Method Get -Uri "$MailpitUrl/messages"
$message = $messages.messages | Where-Object { $_.To[0].Address -eq $studentEmail } | Select-Object -First 1
if (-not $message) {
    throw "Verification mail not found for $studentEmail"
}
$messageDetail = Invoke-RestMethod -Method Get -Uri "$MailpitUrl/message/$($message.ID)"
$messageText = "$($messageDetail.Text) $($messageDetail.HTML)"
$tokenMatch = [regex]::Match($messageText, "token=([0-9a-fA-F-]{36})")
if (-not $tokenMatch.Success) {
    throw "Verification token not found in Mailpit message"
}
$verifyToken = $tokenMatch.Groups[1].Value
Invoke-RestMethod -Method Get -Uri "$BaseUrl/auth/verify?token=$verifyToken" | Out-Null

Write-Step "Student login"
$studentLoginBody = @{ email = $studentEmail; password = $studentPassword } | ConvertTo-Json -Compress
$studentLoginResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/auth/login" -ContentType "application/json" -Body $studentLoginBody
$studentToken = $studentLoginResp.accessToken
if ([string]::IsNullOrWhiteSpace($studentToken)) {
    throw "Student token is empty"
}

Write-Step "Create + publish posting"
$postingBody = @{
    category = "BACKEND"
    title = "Task Smoke Posting $stamp"
    description = "Task workflow smoke"
    projectName = "Task Smoke"
    projectDetails = "Task smoke details"
} | ConvertTo-Json -Compress
$postingResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/postings" -Headers @{ Authorization = "Bearer $adminToken" } -ContentType "application/json" -Body $postingBody
$postingId = $postingResp.id
Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/postings/$postingId/publish" -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null

Write-Step "User submission + admin approve"
$submissionBody = @{ postingId = $postingId } | ConvertTo-Json -Compress
$submissionResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/me/submissions" -Headers @{ Authorization = "Bearer $studentToken" } -ContentType "application/json" -Body $submissionBody
$submissionId = $submissionResp.id
Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/submissions/$submissionId/approve" -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null

Write-Step "Create task assignMode=ALL"
$taskBody = @{
    title = "Task Smoke Assignment $stamp"
    description = "Complete smoke assignment"
    assignMode = "ALL"
} | ConvertTo-Json -Compress
$taskPayload = Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/projects/$postingId/tasks" -Headers @{ Authorization = "Bearer $adminToken" } -ContentType "application/json" -Body $taskBody
$taskId = $taskPayload.id
$assignment = $taskPayload.assignments | Where-Object { $_.assignee.userId -ne $null } | Select-Object -First 1
if (-not $assignment) {
    throw "No assignment created"
}
$assignmentId = $assignment.assignmentId

Write-Step "Upload task attachment (admin)"
$taskAttachmentHeaders = Join-Path $RunDir "task-attachment-headers.$stamp.txt"
$taskAttachmentBody = Join-Path $RunDir "task-attachment-body.$stamp.json"
curl.exe -sS -D $taskAttachmentHeaders -o $taskAttachmentBody -X POST "$BaseUrl/admin/tasks/$taskId/attachments" `
  -H "Authorization: Bearer $adminToken" `
  -F "files=@$SamplePdf;type=application/pdf" | Out-Null
$taskAttachmentStatus = [int]((Get-Content $taskAttachmentHeaders | Select-String "HTTP/" | Select-Object -Last 1).ToString().Split(" ")[1])
Assert-Status "Upload task attachment" $taskAttachmentStatus @(201)
$taskAttachment = (Get-Content $taskAttachmentBody -Raw | ConvertFrom-Json)[0]
$taskAttachmentId = $taskAttachment.id

Write-Step "User submit assignment with file"
$submissionJsonPath = Join-Path $RunDir "task-submit-data.$stamp.json"
Set-Content -Path $submissionJsonPath -Value (@{ textAnswer = "Task smoke submission" } | ConvertTo-Json -Compress) -Encoding UTF8
$submitHeadersPath = Join-Path $RunDir "task-submit-headers.$stamp.txt"
$submitBodyPath = Join-Path $RunDir "task-submit-body.$stamp.json"
curl.exe -sS -D $submitHeadersPath -o $submitBodyPath -X POST "$BaseUrl/me/task-assignments/$assignmentId/submit" `
  -H "Authorization: Bearer $studentToken" `
  -F "data=@$submissionJsonPath;type=application/json" `
  -F "files=@$SamplePdf;type=application/pdf" | Out-Null
$submitStatus = [int]((Get-Content $submitHeadersPath | Select-String "HTTP/" | Select-Object -Last 1).ToString().Split(" ")[1])
Assert-Status "Submit assignment" $submitStatus @(200)
$submitPayload = Get-Content $submitBodyPath -Raw | ConvertFrom-Json
$submissionFileId = $submitPayload.submissionFiles[0].id
if (-not $submissionFileId) {
    throw "Submission file id missing"
}

Write-Step "Admin review assignment"
$reviewBody = @{ decision = "APPROVED"; note = "Smoke review OK" } | ConvertTo-Json -Compress
Invoke-RestMethod -Method Post -Uri "$BaseUrl/admin/task-assignments/$assignmentId/review" -Headers @{ Authorization = "Bearer $adminToken" } -ContentType "application/json" -Body $reviewBody | Out-Null

Write-Step "Download task + submission files and assert non-empty"
$dlTaskPath = Join-Path $RunDir "task-attachment.$stamp.pdf"
$dlSubmissionPath = Join-Path $RunDir "task-submission.$stamp.pdf"
curl.exe -sS -o $dlTaskPath -X GET "$BaseUrl/admin/tasks/$taskId/attachments/$taskAttachmentId/download" -H "Authorization: Bearer $adminToken" | Out-Null
curl.exe -sS -o $dlSubmissionPath -X GET "$BaseUrl/admin/task-assignments/$assignmentId/submission-files/$submissionFileId/download" -H "Authorization: Bearer $adminToken" | Out-Null
if ((Get-Item $dlTaskPath).Length -le 0) { throw "Task attachment download is empty" }
if ((Get-Item $dlSubmissionPath).Length -le 0) { throw "Submission file download is empty" }

Write-Step "Recreate backend container and re-check persistence"
docker compose up -d --force-recreate backend | Out-Host
Wait-Healthy "ats-backend"
curl.exe -sS -o $dlTaskPath -X GET "$BaseUrl/admin/tasks/$taskId/attachments/$taskAttachmentId/download" -H "Authorization: Bearer $adminToken" | Out-Null
curl.exe -sS -o $dlSubmissionPath -X GET "$BaseUrl/admin/task-assignments/$assignmentId/submission-files/$submissionFileId/download" -H "Authorization: Bearer $adminToken" | Out-Null
if ((Get-Item $dlTaskPath).Length -le 0) { throw "Task attachment lost after backend recreate" }
if ((Get-Item $dlSubmissionPath).Length -le 0) { throw "Submission file lost after backend recreate" }

Write-Step "Smoke complete"
Write-Host "studentEmail=$studentEmail"
Write-Host "postingId=$postingId taskId=$taskId assignmentId=$assignmentId"
Write-Host "taskAttachmentId=$taskAttachmentId submissionFileId=$submissionFileId"
Remove-Item -Path $RunDir -Recurse -Force -ErrorAction SilentlyContinue
