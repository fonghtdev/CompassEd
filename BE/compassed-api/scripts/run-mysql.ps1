param(
  [string]$MySqlHost = "localhost",
  [int]$MySqlPort = 3306,
  [string]$MySqlUser = "root",
  [string]$MySqlPassword = "1234",
  [string]$Database = "compassed",
  [string]$JwtSecret = "this-is-a-very-strong-jwt-secret-min-32",
  [string]$AdminEmail = "",
  [string]$AdminPassword = "",
  [string]$MailHost = "smtp.gmail.com",
  [int]$MailPort = 587,
  [string]$MailUsername = "",
  [string]$MailPassword = "",
  [string]$MailFrom = "",
  [bool]$PayOsEnabled = $false,
  [string]$PayOsClientId = "",
  [string]$PayOsApiKey = "",
  [string]$PayOsChecksumKey = "",
  [string]$PayOsBaseUrl = "https://api-merchant.payos.vn",
  [string]$PayOsReturnUrl = "https://compassed.io.vn/checkout",
  [string]$PayOsCancelUrl = "https://compassed.io.vn/checkout",
  [int]$PayOsCheckCooldownSeconds = 15,
  [int]$ServerPort = 8080
)

$ErrorActionPreference = "Stop"

Write-Host "Ensuring database '$Database' exists on $MySqlHost`:$MySqlPort..."
mysql -h $MySqlHost -P $MySqlPort -u $MySqlUser -p$MySqlPassword -e "CREATE DATABASE IF NOT EXISTS $Database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" | Out-Null

$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:MYSQL_URL = "jdbc:mysql://${MySqlHost}:${MySqlPort}/${Database}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:MYSQL_USER = $MySqlUser
$env:MYSQL_PASSWORD = $MySqlPassword
$env:JWT_SECRET = $JwtSecret
$env:ADMIN_EMAIL = $AdminEmail
$env:ADMIN_PASSWORD = $AdminPassword
if ($MailHost -and $MailHost.Trim() -ne "") { $env:MAIL_HOST = $MailHost }
if ($MailPort -gt 0) { $env:MAIL_PORT = "$MailPort" }
if ($MailUsername -and $MailUsername.Trim() -ne "") { $env:MAIL_USERNAME = $MailUsername }
if ($MailPassword -and $MailPassword.Trim() -ne "") { $env:MAIL_PASSWORD = $MailPassword }
if ($MailFrom -and $MailFrom.Trim() -ne "") { $env:MAIL_FROM = $MailFrom }
$env:PAYOS_ENABLED = ($(if ($PayOsEnabled) { "true" } else { "false" }))
if ($PayOsClientId -and $PayOsClientId.Trim() -ne "") { $env:PAYOS_CLIENT_ID = $PayOsClientId }
if ($PayOsApiKey -and $PayOsApiKey.Trim() -ne "") { $env:PAYOS_API_KEY = $PayOsApiKey }
if ($PayOsChecksumKey -and $PayOsChecksumKey.Trim() -ne "") { $env:PAYOS_CHECKSUM_KEY = $PayOsChecksumKey }
if ($PayOsBaseUrl -and $PayOsBaseUrl.Trim() -ne "") { $env:PAYOS_BASE_URL = $PayOsBaseUrl }
if ($PayOsReturnUrl -and $PayOsReturnUrl.Trim() -ne "") { $env:PAYOS_RETURN_URL = $PayOsReturnUrl }
if ($PayOsCancelUrl -and $PayOsCancelUrl.Trim() -ne "") { $env:PAYOS_CANCEL_URL = $PayOsCancelUrl }
if ($PayOsCheckCooldownSeconds -gt 0) { $env:PAYOS_CHECK_COOLDOWN_SECONDS = "$PayOsCheckCooldownSeconds" }
$env:SERVER_PORT = "$ServerPort"

Write-Host "Starting backend on http://localhost:$ServerPort ..."
.\mvnw.cmd spring-boot:run
