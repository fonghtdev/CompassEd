$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
. (Join-Path $PSScriptRoot "dev-env.ps1")

$feDir = Join-Path $root "FE"
if (!(Test-Path $feDir)) {
  throw "Frontend directory not found: $feDir"
}

$env:FE_PORT = $script:DEV_ENV.FE_PORT

Push-Location $feDir
try {
  $apiBase = $script:DEV_ENV.FE_API_BASE
  if (-not $apiBase -or $apiBase.Trim() -eq "") {
    $apiBase = "http://localhost:$($script:DEV_ENV.BE_PORT)"
  }
  $googleClientId = $script:DEV_ENV.GOOGLE_CLIENT_ID
  $configPath = Join-Path $feDir "config.js"
  @"
window.APP_CONFIG = {
  API_BASE: "$apiBase",
  GOOGLE_CLIENT_ID: "$googleClientId"
};
"@ | Set-Content -Path $configPath -Encoding UTF8

  python .\Extensions.py
} finally {
  Pop-Location
}
