#!/bin/bash

# Linux Deployment Script for Breakup Stories API
# Usage: ./deploy-linux.sh [environment] [version]

set -e  # Exit on any error

# Configuration
DOCKER_USERNAME="madhukinnera"
IMAGE_NAME="breakup-stories-api"
DEFAULT_ENVIRONMENT="dev"
DEFAULT_VERSION="linux-latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get parameters
ENVIRONMENT=${1:-$DEFAULT_ENVIRONMENT}
VERSION=${2:-$DEFAULT_VERSION}

echo -e "${BLUE}🐳 Linux Deployment Script for Breakup Stories API${NC}"
echo -e "${BLUE}📦 Environment: $ENVIRONMENT${NC}"
echo -e "${BLUE}🏷️  Version: $VERSION${NC}"
echo ""

# Function to check if Docker is running
check_docker() {
    echo -e "${YELLOW}🔍 Checking Docker...${NC}"
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker is running${NC}"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    echo -e "${YELLOW}🔍 Checking Docker Compose...${NC}"
    if ! docker-compose --version > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker Compose is not available. Please install Docker Compose.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker Compose is available${NC}"
}

# Function to create .env file if it doesn't exist
create_env_file() {
    if [ ! -f .env ]; then
        echo -e "${YELLOW}📝 Creating .env file...${NC}"
        cat > .env << EOF
# MongoDB Configuration
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=password123

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-in-production

# Email Configuration (Gmail)
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# Upload Service Configuration
UPLOAD_SERVICE_URL=http://localhost:9090
UPLOAD_SERVICE_ENDPOINT=/api/v1/upload



# Logging
LOG_LEVEL=INFO

# JWT Expiration
JWT_EXPIRATION=86400000
EOF
        echo -e "${GREEN}✅ .env file created${NC}"
        echo -e "${YELLOW}⚠️  Please update the .env file with your actual values${NC}"
    else
        echo -e "${GREEN}✅ .env file already exists${NC}"
    fi
}

# Function to pull the latest image
pull_image() {
    echo -e "${YELLOW}📥 Pulling latest image...${NC}"
    docker pull $DOCKER_USERNAME/$IMAGE_NAME:$VERSION
    echo -e "${GREEN}✅ Image pulled successfully${NC}"
}

# Function to stop existing containers
stop_containers() {
    echo -e "${YELLOW}🛑 Stopping existing containers...${NC}"
    docker-compose -f docker-compose.linux.yml down 2>/dev/null || true
    docker-compose -f docker-compose.linux.prod.yml down 2>/dev/null || true
    echo -e "${GREEN}✅ Existing containers stopped${NC}"
}

# Function to start containers
start_containers() {
    echo -e "${YELLOW}🚀 Starting containers...${NC}"
    
    if [ "$ENVIRONMENT" = "prod" ]; then
        echo -e "${BLUE}🏭 Starting production environment...${NC}"
        docker-compose -f docker-compose.linux.prod.yml up -d
    else
        echo -e "${BLUE}🔧 Starting development environment...${NC}"
        docker-compose -f docker-compose.linux.yml up -d
    fi
    
    echo -e "${GREEN}✅ Containers started successfully${NC}"
}

# Function to wait for services to be ready
wait_for_services() {
    echo -e "${YELLOW}⏳ Waiting for services to be ready...${NC}"
    
    # Wait for MongoDB
    echo -e "${BLUE}📊 Waiting for MongoDB...${NC}"
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker exec breakup-stories-mongodb-linux mongosh --eval "db.adminCommand('ping')" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ MongoDB is ready${NC}"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        echo -e "${RED}❌ MongoDB failed to start within 60 seconds${NC}"
        exit 1
    fi
    
    # Wait for API
    echo -e "${BLUE}🌐 Waiting for API...${NC}"
    timeout=120
    while [ $timeout -gt 0 ]; do
        if curl -f http://localhost:9100/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ API is ready${NC}"
            break
        fi
        sleep 3
        timeout=$((timeout - 3))
    done
    
    if [ $timeout -le 0 ]; then
        echo -e "${RED}❌ API failed to start within 120 seconds${NC}"
        exit 1
    fi
}

# Function to show deployment status
show_status() {
    echo ""
    echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}📋 Service Status:${NC}"
    docker-compose -f docker-compose.linux.yml ps 2>/dev/null || docker-compose -f docker-compose.linux.prod.yml ps
    echo ""
    echo -e "${BLUE}🌐 Access URLs:${NC}"
    echo -e "${GREEN}   API:${NC} http://localhost:9100"
    echo -e "${GREEN}   Health Check:${NC} http://localhost:9100/actuator/health"
    echo -e "${GREEN}   Swagger UI:${NC} http://localhost:9100/swagger-ui.html"
    echo -e "${GREEN}   API Docs:${NC} http://localhost:9100/api-docs"
    echo -e "${GREEN}   Mongo Express:${NC} http://localhost:8081"
    echo ""
    echo -e "${BLUE}📊 Useful Commands:${NC}"
    echo -e "${YELLOW}   View logs:${NC} docker-compose -f docker-compose.linux.yml logs -f"
    echo -e "${YELLOW}   Stop services:${NC} docker-compose -f docker-compose.linux.yml down"
    echo -e "${YELLOW}   Restart services:${NC} docker-compose -f docker-compose.linux.yml restart"
    echo ""
}

# Function to show logs
show_logs() {
    echo -e "${YELLOW}📋 Recent logs:${NC}"
    docker-compose -f docker-compose.linux.yml logs --tail=20 2>/dev/null || docker-compose -f docker-compose.linux.prod.yml logs --tail=20
}

# Main deployment process
main() {
    echo -e "${BLUE}🚀 Starting Linux deployment...${NC}"
    echo ""
    
    # Pre-deployment checks
    check_docker
    check_docker_compose
    create_env_file
    
    # Deployment process
    pull_image
    stop_containers
    start_containers
    wait_for_services
    
    # Post-deployment
    show_status
    show_logs
    
    echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
}

# Run main function
main "$@" 