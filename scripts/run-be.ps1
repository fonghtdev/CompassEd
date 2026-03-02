param(
  [switch]$UseConfigOnly
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "dev-env.ps1")

$beDir = Join-Path $root "BE\compassed-api"
if (!(Test-Path $beDir)) {
  throw "Backend directory not found: $beDir"
}

$params = @{
  MySqlHost = $script:DEV_ENV.MYSQL_HOST
  MySqlPort = [int]$script:DEV_ENV.MYSQL_PORT
  MySqlUser = $script:DEV_ENV.MYSQL_USER
  MySqlPassword = $script:DEV_ENV.MYSQL_PASSWORD
  Database = $script:DEV_ENV.MYSQL_DATABASE
  JwtSecret = $script:DEV_ENV.JWT_SECRET
  AdminEmail = $script:DEV_ENV.ADMIN_EMAIL
  AdminPassword = $script:DEV_ENV.ADMIN_PASSWORD
  MailHost = $script:DEV_ENV.MAIL_HOST
  MailPort = [int]$script:DEV_ENV.MAIL_PORT
  MailUsername = $script:DEV_ENV.MAIL_USERNAME
  MailPassword = $script:DEV_ENV.MAIL_PASSWORD
  MailFrom = $script:DEV_ENV.MAIL_FROM
  ServerPort = [int]$script:DEV_ENV.BE_PORT
}

Push-Location $beDir
try {
  if ($script:DEV_ENV.OPENAI_API_KEY -and $script:DEV_ENV.OPENAI_API_KEY.Trim() -ne "") {
    $env:OPENAI_API_KEY = $script:DEV_ENV.OPENAI_API_KEY
  }
  if ($script:DEV_ENV.GOOGLE_CLIENT_ID -and $script:DEV_ENV.GOOGLE_CLIENT_ID.Trim() -ne "") {
    $env:GOOGLE_CLIENT_ID = $script:DEV_ENV.GOOGLE_CLIENT_ID
  }
  if ($UseConfigOnly) {
    $params.MailUsername = ""
    $params.MailPassword = ""
    $params.MailFrom = ""
  }
  & .\scripts\run-mysql.ps1 @params
} finally {
  Pop-Location
}
