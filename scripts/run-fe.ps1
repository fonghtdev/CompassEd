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
  python .\Extensions.py
} finally {
  Pop-Location
}
