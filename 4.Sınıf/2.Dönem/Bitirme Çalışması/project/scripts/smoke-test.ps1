# Smoke Test for ATS Project (Windows PowerShell)

$ErrorActionPreference = "Stop"
$BaseUrl = "http://localhost:8080/api/v1"
$MailpitUrl = "http://localhost:8025/api/v1"
$SamplePdf = Join-Path $PSScriptRoot "sample/sample.pdf"

# 1. Start System
Write-Host "--- Starting Services ---" -ForegroundColor Cyan
docker compose up -d --build

# 2. Wait for Backend
Write-Host "--- Waiting for Backend Health Check ---" -ForegroundColor Cyan
$maxRetries = 30
$retryCount = 0
$healthy = $false

while (-not $healthy -and $retryCount -lt $maxRetries) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
        if ($response.status -eq "UP") {
            $healthy = $true
        }
    } catch {
        # Wait and retry
    }
    if (-not $healthy) {
        $retryCount++
        Write-Host "Waiting... ($retryCount/$maxRetries)"
        Start-Sleep -Seconds 2
    }
}

if (-not $healthy) {
    Write-Error "Backend failed to become healthy in time."
    exit 1
}
Write-Host "Backend is UP!" -ForegroundColor Green

# Define results summary
$results = @()

function Add-Result($task, $status) {
    $global:results += [PSCustomObject]@{ Task = $task; Status = $status }
}

# 3. Public Postings
Write-Host "`n--- Testing Public Postings ---" -ForegroundColor Cyan
try {
    $postings = Invoke-RestMethod -Uri "$BaseUrl/public/postings" -Method Get
    Write-Host "Public postings found: $($postings.postings.Count)"
    Add-Result "Public Postings" "PASS"
} catch {
    Write-Host "Failed: $_" -ForegroundColor Red
    Add-Result "Public Postings" "FAIL"
}

# 4. Register
Write-Host "`n--- Testing Registration (Multipart) ---" -ForegroundColor Cyan
$boundary = [System.Guid]::NewGuid().ToString()
$LF = "`r`n"
$registerEmail = "testuser_$([guid]::NewGuid().ToString().Substring(0,8))@ogr.sakarya.edu.tr"

$body = (
    "--$boundary$LF" +
    "Content-Disposition: form-data; name=`"data`"$LF" +
    "Content-Type: application/json$LF$LF" +
    "{`"email`":`"$registerEmail`", `"password`":`"Test12345!`", `"firstName`":`"Test`", `"lastName`":`"User`", `"classYear`":3, `"department`":`"Computer Engineering`", `"englishLevel`":`"B2`", `"gpa`":3.5}$LF" +
    "--$boundary$LF" +
    "Content-Disposition: form-data; name=`"cv`"; filename=`"test.pdf`"$LF" +
    "Content-Type: application/pdf$LF$LF" +
    "Dummy PDF content$LF" +
    "--$boundary--$LF"
)

try {
    $regResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/register" -Method Post -ContentType "multipart/form-data; boundary=$boundary" -Body $body
    Write-Host "Registration successful for $registerEmail"
    Add-Result "Registration" "PASS"
} catch {
    Write-Host "Failed: $_" -ForegroundColor Red
    Add-Result "Registration" "FAIL"
}

# 4b. Verify Email via Mailpit API
Write-Host "`n--- Verifying Email via Mailpit ---" -ForegroundColor Cyan
$verificationToken = ""
try {
    Start-Sleep -Seconds 2  # Give mailpit time to receive the email
    $messages = Invoke-RestMethod -Uri "$MailpitUrl/messages" -Method Get
    foreach ($msg in $messages.messages) {
        if ($msg.To -and $msg.To[0].Address -eq $registerEmail) {
            # Fetch the message body to extract verification token
            $msgDetail = Invoke-RestMethod -Uri "$MailpitUrl/message/$($msg.ID)" -Method Get
            $msgText = $msgDetail.Text
            # Extract token from URL pattern /verify?token=UUID
            if ($msgText -match 'token=([0-9a-f\-]{36})') {
                $verificationToken = $Matches[1]
                Write-Host "Found verification token: $verificationToken"
            }
            break
        }
    }
    if ($verificationToken) {
        $verifyResp = Invoke-RestMethod -Uri "$BaseUrl/auth/verify?token=$verificationToken" -Method Get
        Write-Host "Email verified: $($verifyResp.message)"
        Add-Result "Email Verify" "PASS"
    } else {
        Write-Host "Could not find verification token in Mailpit" -ForegroundColor Yellow
        Add-Result "Email Verify" "SKIPPED"
    }
} catch {
    Write-Host "Failed: $_" -ForegroundColor Red
    Add-Result "Email Verify" "FAIL"
}

# 5. Admin Login
Write-Host "`n--- Testing Admin Login ---" -ForegroundColor Cyan
$adminToken = ""
try {
    $loginBody = @{
        email = "admin@32bit.com.tr"
        password = "Admin12345!"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
    $adminToken = $loginResponse.accessToken
    Write-Host "Admin login successful"
    Add-Result "Admin Login" "PASS"
} catch {
    Write-Host "Failed: $_" -ForegroundColor Red
    Add-Result "Admin Login" "FAIL"
}

# 6. Admin Posting Create & Publish
$postingId = ""
if ($adminToken) {
    Write-Host "`n--- Testing Admin Posting Creation & Publish ---" -ForegroundColor Cyan
    try {
        $headers = @{ "Authorization" = "Bearer $adminToken" }
        $postData = @{
            title = "QA Automation Engineer"
            description = "Test description"
            category = "BACKEND"
            projectName = "Project X"
            projectDetails = "Details of Project X"
        } | ConvertTo-Json

        $newPosting = Invoke-RestMethod -Uri "$BaseUrl/admin/postings" -Method Post -Headers $headers -ContentType "application/json" -Body $postData
        $postingId = $newPosting.id
        Write-Host "Posting created: ID $postingId"

        # Publish
        Invoke-RestMethod -Uri "$BaseUrl/admin/postings/$postingId/publish" -Method Post -Headers $headers
        Write-Host "Posting published"
        Add-Result "Admin Create/Publish" "PASS"
    } catch {
        Write-Host "Failed: $_" -ForegroundColor Red
        Add-Result "Admin Create/Publish" "FAIL"
    }
}

# 7. User Login & Submission
Write-Host "`n--- Testing User Login & Submission ---" -ForegroundColor Cyan
try {
    $userLoginBody = @{
        email = $registerEmail
        password = "Test12345!"
    } | ConvertTo-Json
    $userLoginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType "application/json" -Body $userLoginBody
    $userToken = $userLoginResponse.accessToken
    $userHeaders = @{ "Authorization" = "Bearer $userToken" }
    Write-Host "User login successful"

    # Create submission (requires a published posting)
    if ($postingId) {
        $subBody = @{
            postingId = $postingId
        } | ConvertTo-Json
        $newSub = Invoke-RestMethod -Uri "$BaseUrl/me/submissions" -Method Post -Headers $userHeaders -ContentType "application/json" -Body $subBody
        $subId = $newSub.id
        Write-Host "Submission created: ID $subId"
        Add-Result "User Submission" "PASS"
    } else {
        Add-Result "User Submission" "SKIPPED (No Posting)"
    }
} catch {
    Write-Host "Failed: $_" -ForegroundColor Red
    Add-Result "User Submission" "FAIL"
}

# 8. Mail Job
if ($adminToken) {
    Write-Host "`n--- Testing Mail Job Creation ---" -ForegroundColor Cyan
    try {
        $boundary2 = [System.Guid]::NewGuid().ToString()
        $mailJobBody = (
            "--$boundary2$LF" +
            "Content-Disposition: form-data; name=`"data`"$LF" +
            "Content-Type: application/json$LF$LF" +
            "{`"subject`":`"Test Subject`", `"body`":`"Test Body`"}$LF" +
            "--$boundary2--$LF"
        )
        $mailHeaders = @{ "Authorization" = "Bearer $adminToken" }
        Invoke-RestMethod -Uri "$BaseUrl/admin/mail/jobs/all-students" -Method Post -Headers $mailHeaders -ContentType "multipart/form-data; boundary=$boundary2" -Body $mailJobBody
        Write-Host "Mail job created"
        Add-Result "Mail Job" "PASS"
    } catch {
        Write-Host "Failed: $_" -ForegroundColor Red
        Add-Result "Mail Job" "FAIL"
    }
}

# Final Summary
Write-Host "`n================ SMOKE TEST SUMMARY ================" -ForegroundColor Cyan
$results | Format-Table -AutoSize
Write-Host "====================================================" -ForegroundColor Cyan
