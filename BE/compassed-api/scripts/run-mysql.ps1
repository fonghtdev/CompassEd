param(
  [string]$MySqlHost = "localhost",
  [int]$MySqlPort = 3306,
  [string]$MySqlUser = "root",
  [string]$MySqlPassword = "1234",
  [string]$Database = "compassed",
  [string]$JwtSecret = "this-is-a-very-strong-jwt-secret-min-32",
  [string]$AdminEmail = "",
  [string]$AdminPassword = "",
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
$env:SERVER_PORT = "$ServerPort"

Write-Host "Starting backend on http://localhost:$ServerPort ..."
.\mvnw.cmd spring-boot:run
