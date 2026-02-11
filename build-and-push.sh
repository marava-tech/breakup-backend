#!/bin/bash

# Build and Push Script for Breakup Stories API
# Builds for linux/amd64 platform
# Usage: ./build-and-push.sh [version]

set -e  # Exit on any error

# Configuration
DOCKER_USERNAME="maravatechnologies"
IMAGE_NAME="breakup-stories-api"
TAG="latest"

echo "🐳 Building and pushing Docker image for linux/amd64..."
echo "📦 Image: $DOCKER_USERNAME/$IMAGE_NAME"
echo "🏷️  Tag: $TAG"

# Check Docker Hub authentication
echo "🔐 Checking Docker Hub authentication..."

if docker info 2>/dev/null | grep -q "Username"; then
    CURRENT_USER=$(docker info 2>/dev/null | grep "Username" | awk '{print $2}')
    echo "✅ Logged in as: $CURRENT_USER"
else
    echo "❌ Not logged in to Docker Hub. Please login first:"
    echo "   docker login"
    exit 1
fi

# Check if JAR file exists
if [ ! -f "target/breakup-be-1.0.0.jar" ]; then
    echo "❌ Error: JAR file not found at target/breakup-be-1.0.0.jar"
    echo "💡 Please build the JAR first: mvn clean package -DskipTests"
    exit 1
fi

# Build Docker image for linux/amd64 platform
echo "🔨 Building Docker image for linux/amd64..."
FULL_IMAGE_NAME="$DOCKER_USERNAME/$IMAGE_NAME:$TAG"

if docker build --platform linux/amd64 -t "$FULL_IMAGE_NAME" .; then
    echo "✅ Successfully built image: $FULL_IMAGE_NAME"
else
    echo "❌ Failed to build image"
    exit 1
fi

# Push the image
echo "📤 Pushing to Docker Hub..."
if docker push "$FULL_IMAGE_NAME"; then
    echo "✅ Successfully pushed image: $FULL_IMAGE_NAME"
else
    echo "❌ Failed to push image"
    exit 1
fi

echo ""
echo "🎉 Done! Image pushed to Docker Hub: $FULL_IMAGE_NAME"
echo ""
echo "🚀 To run the application:"
echo "   docker run -p 8080:8080 $FULL_IMAGE_NAME"
echo ""
echo "📖 Or use docker-compose:"
echo "   docker-compose up -d"
