#!/bin/bash

# Navigate to backend directory
cd "$(dirname "$0")/../BE/compassed-api"

# Set environment variables
export SPRING_PROFILES_ACTIVE=mysql
export MYSQL_URL="jdbc:mysql://localhost:3306/compassed?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MYSQL_USER=root
export MYSQL_PASSWORD=root
export JWT_SECRET="this-is-a-very-strong-jwt-secret-min-32"
export SERVER_PORT=8080

# Make mvnw executable
chmod +x mvnw

# Run Spring Boot
./mvnw spring-boot:run
