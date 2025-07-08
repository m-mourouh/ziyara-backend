#!/bin/bash

echo "🚀 Starting Ziyara Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL and Redis
echo "📦 Starting PostgreSQL and Redis..."
docker-compose up -d postgres redis

# Wait for PostgreSQL to be ready
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

# Check if PostgreSQL is ready
until docker-compose exec -T postgres pg_isready -U ziyara_user -d ziyara_db > /dev/null 2>&1; do
    echo "⏳ PostgreSQL is still starting up..."
    sleep 2
done

echo "✅ PostgreSQL is ready!"

# Check if Redis is ready
until docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; do
    echo "⏳ Redis is still starting up..."
    sleep 2
done

echo "✅ Redis is ready!"

# Start the Spring Boot application
echo "🌱 Starting Spring Boot application..."
./mvnw spring-boot:run

# To start with admin tools (PgAdmin and Redis Commander):
# docker-compose --profile tools up -d