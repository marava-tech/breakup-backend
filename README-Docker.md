# 🐳 Docker Setup for Breakup Stories API

This guide will help you build, run, and deploy the Breakup Stories API using Docker.

## 📋 Prerequisites

- Docker installed on your system
- Docker Compose installed
- Docker Hub account (for pushing images)

## 🏗️ Project Structure

```
breakup_be/
├── Dockerfile                 # Multi-stage Docker build
├── .dockerignore             # Files to exclude from build
├── docker-compose.yml        # Development environment
├── docker-compose.prod.yml   # Production environment
├── mongo-init.js             # MongoDB initialization script
├── build-and-push.sh         # Build and push script
└── README-Docker.md          # This file
```

## 🚀 Quick Start

### 1. Development Environment

```bash
# Start all services (MongoDB + API + Mongo Express)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 2. Production Environment

```bash
# Create .env file with production variables
cp .env.example .env

# Start production services
docker-compose -f docker-compose.prod.yml up -d

# Stop production services
docker-compose -f docker-compose.prod.yml down
```

## 🔨 Building the Docker Image

### Manual Build

```bash
# Build the image
docker build -t madhukinnera/breakup-stories-api:latest .

# Run the container
docker run -p 8080:8080 madhukinnera/breakup-stories-api:latest
```

### Using Build Script

```bash
# Build and push to Docker Hub
./build-and-push.sh

# Build specific version
./build-and-push.sh v1.0.0
```

## 📦 Docker Images

### Image Details

- **Base Image**: `eclipse-temurin:17-jre-alpine`
- **Size**: ~200MB (optimized)
- **Security**: Non-root user
- **Health Check**: Built-in health monitoring

### Available Tags

- `madhukinnera/breakup-stories-api:latest`
- `madhukinnera/breakup-stories-api:v1.0.0`
- `madhukinnera/breakup-stories-api:dev`

## 🌐 Services

### Development Services

| Service | Port | Description |
|---------|------|-------------|
| **API** | 8080 | Spring Boot Application |
| **MongoDB** | 27017 | Database |
| **Mongo Express** | 8081 | Database Management UI |

### Production Services

| Service | Port | Description |
|---------|------|-------------|
| **API** | 8080 | Spring Boot Application |
| **MongoDB** | 27017 | Database (localhost only) |
| **Nginx** | 80/443 | Reverse Proxy (optional) |

## ⚙️ Configuration

### Environment Variables

#### Required for Production

```bash
# MongoDB
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your-secure-password

# JWT
JWT_SECRET=your-super-secret-jwt-key

# Email (Gmail)
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# Upload Service
UPLOAD_SERVICE_URL=https://your-upload-service.com
```

#### Optional

```bash
# Audio Streaming
AUDIO_BUFFER_SIZE=8192
AUDIO_CACHE_DURATION=3600
AUDIO_MAX_RANGE_SIZE=1048576
AUDIO_ENABLE_CACHING=true

# Logging
LOG_LEVEL=INFO

# JWT Expiration
JWT_EXPIRATION=86400000
```

### MongoDB Configuration

The MongoDB container is automatically initialized with:

- Database: `breakup_stories`
- User: `breakup_user` / `breakup_password`
- Collections: All required collections with indexes
- Permissions: Read/Write access

## 🔍 Monitoring & Health Checks

### Health Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics (development only)
curl http://localhost:8080/actuator/metrics
```

### Docker Health Checks

```bash
# Check container health
docker ps

# View health check logs
docker inspect breakup-stories-api
```

## 🚀 Deployment Options

### 1. Single Server Deployment

```bash
# Production deployment
docker-compose -f docker-compose.prod.yml up -d

# With Nginx reverse proxy
docker-compose -f docker-compose.prod.yml --profile nginx up -d
```

### 2. Docker Swarm Deployment

```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.prod.yml breakup-stories
```

### 3. Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Port Already in Use

```bash
# Check what's using the port
lsof -i :8080

# Stop conflicting service
sudo systemctl stop conflicting-service
```

#### 2. MongoDB Connection Issues

```bash
# Check MongoDB logs
docker-compose logs mongodb

# Restart MongoDB
docker-compose restart mongodb
```

#### 3. Memory Issues

```bash
# Check container memory usage
docker stats

# Increase memory limits in docker-compose.yml
```

### Logs and Debugging

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f breakup-stories-api

# Access container shell
docker exec -it breakup-stories-api sh
```

## 🔒 Security Considerations

### Production Security

1. **Change Default Passwords**: Update MongoDB and JWT secrets
2. **Use Environment Variables**: Never hardcode secrets
3. **Network Security**: Use internal networks
4. **Regular Updates**: Keep base images updated
5. **Resource Limits**: Set memory and CPU limits

### Security Best Practices

```bash
# Use secrets management
docker secret create jwt_secret ./jwt_secret.txt

# Enable Docker content trust
export DOCKER_CONTENT_TRUST=1

# Scan images for vulnerabilities
docker scan madhukinnera/breakup-stories-api:latest
```

## 📊 Performance Optimization

### Resource Limits

```yaml
# In docker-compose.prod.yml
deploy:
  resources:
    limits:
      memory: 2G
      cpus: '1.0'
    reservations:
      memory: 1G
      cpus: '0.5'
```

### JVM Optimization

```bash
# Optimized JVM options
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"
```

## 🧪 Testing

### Run Tests in Container

```bash
# Build with tests
docker build --target builder -t breakup-stories-test .

# Run tests
docker run breakup-stories-test ./mvnw test
```

### Integration Testing

```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
./run-integration-tests.sh

# Cleanup
docker-compose -f docker-compose.test.yml down
```

## 📈 Monitoring

### Prometheus Metrics

```bash
# Enable Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Log Aggregation

```bash
# Use log drivers
docker run --log-driver=json-file --log-opt max-size=10m your-app
```

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Deploy
on:
  push:
    branches: [main]

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build and push
        run: |
          docker build -t madhukinnera/breakup-stories-api:${{ github.sha }} .
          docker push madhukinnera/breakup-stories-api:${{ github.sha }}
```

## 📞 Support

For issues and questions:

1. Check the logs: `docker-compose logs`
2. Verify configuration: Check environment variables
3. Test connectivity: Use health check endpoints
4. Review documentation: Check this README

## 🎉 Success!

Your Breakup Stories API is now containerized and ready for deployment! 🚀 