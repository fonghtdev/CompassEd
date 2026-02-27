$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run-be.ps1") -WorkingDirectory $root
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $PSScriptRoot "run-fe.ps1") -WorkingDirectory $root

Write-Host "Started backend and frontend in two new terminals."
