#!/bin/bash

# Build and Push Script for Breakup Stories API
# Usage: ./build-and-push.sh [version]

set -e  # Exit on any error

# Configuration
DOCKER_USERNAME="madhukinnera"
IMAGE_NAME="breakup-stories-api"
DEFAULT_VERSION="latest"

# Get version from command line or use default
VERSION=${1:-$DEFAULT_VERSION}

echo "🐳 Building and pushing Docker image..."
echo "📦 Image: $DOCKER_USERNAME/$IMAGE_NAME:$VERSION"

# Build the Docker image
echo "🔨 Building Docker image..."
docker build -t $DOCKER_USERNAME/$IMAGE_NAME:$VERSION .

# Tag as latest if not already latest
if [ "$VERSION" != "latest" ]; then
    echo "🏷️  Tagging as latest..."
    docker tag $DOCKER_USERNAME/$IMAGE_NAME:$VERSION $DOCKER_USERNAME/$IMAGE_NAME:latest
fi

# Push to Docker Hub
echo "📤 Pushing to Docker Hub..."
docker push $DOCKER_USERNAME/$IMAGE_NAME:$VERSION

if [ "$VERSION" != "latest" ]; then
    echo "📤 Pushing latest tag..."
    docker push $DOCKER_USERNAME/$IMAGE_NAME:latest
fi

echo "✅ Successfully built and pushed $DOCKER_USERNAME/$IMAGE_NAME:$VERSION"
echo "🎉 Image is now available on Docker Hub!"

# Optional: Show image info
echo ""
echo "📋 Image Information:"
docker images $DOCKER_USERNAME/$IMAGE_NAME:$VERSION

echo ""
echo "🚀 To run the application:"
echo "   docker run -p 8080:8080 $DOCKER_USERNAME/$IMAGE_NAME:$VERSION"
echo ""
echo "📖 Or use docker-compose:"
echo "   docker-compose up -d" 