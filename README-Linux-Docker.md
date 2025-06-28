# Linux Docker Deployment Guide

This guide provides comprehensive instructions for deploying the Breakup Stories API on Linux systems using Docker.

## 🐳 Overview

The Linux deployment includes:
- **Port 9100**: Backend API service
- **Multi-stage Docker build**: Optimized for Linux environments
- **MongoDB**: Database with authentication
- **Mongo Express**: Optional database management UI
- **Nginx**: Optional reverse proxy with SSL support
- **Health checks**: Automated service monitoring
- **Production-ready**: Security and performance optimizations

## 📋 Prerequisites

### System Requirements
- **OS**: Linux (Ubuntu 20.04+, CentOS 8+, or similar)
- **Docker**: Version 20.10+
- **Docker Compose**: Version 2.0+
- **Memory**: Minimum 2GB RAM (4GB recommended)
- **Storage**: Minimum 10GB free space
- **CPU**: 2 cores minimum (4 cores recommended)

### Install Docker (if not installed)
```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# CentOS/RHEL
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Logout and login again for group changes to take effect
```

### Install Docker Compose
```bash
# Install Docker Compose v2
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## 🚀 Quick Start

### 1. Clone and Setup
```bash
# Clone the repository
git clone <your-repo-url>
cd breakup_be

# Make scripts executable
chmod +x build-and-push-linux.sh
chmod +x deploy-linux.sh
```

### 2. Build and Push (First Time)
```bash
# Build and push the Linux Docker image
./build-and-push-linux.sh

# Or with custom version
./build-and-push-linux.sh v1.0.0
```

### 3. Deploy
```bash
# Deploy development environment
./deploy-linux.sh dev

# Deploy production environment
./deploy-linux.sh prod

# Deploy with specific version
./deploy-linux.sh prod v1.0.0
```

## 📁 File Structure

```
breakup_be/
├── Dockerfile.linux              # Linux-specific Dockerfile
├── docker-compose.linux.yml      # Development compose file
├── docker-compose.linux.prod.yml # Production compose file
├── nginx.linux.conf              # Nginx configuration
├── build-and-push-linux.sh       # Build and push script
├── deploy-linux.sh               # Deployment script
├── mongo-init.js                 # MongoDB initialization
└── README-Linux-Docker.md        # This file
```

## 🔧 Configuration

### Environment Variables (.env file)
Create a `.env` file in the project root:

```bash
# MongoDB Configuration
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your-secure-password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-in-production

# Email Configuration (Gmail)
GMAIL_USERNAME=your-email@gmail.com
GMAIL_APP_PASSWORD=your-app-password

# Upload Service Configuration
UPLOAD_SERVICE_URL=http://localhost:9090
UPLOAD_SERVICE_ENDPOINT=/api/v1/upload

# Audio Streaming Configuration
AUDIO_BUFFER_SIZE=8192
AUDIO_CACHE_DURATION=3600
AUDIO_MAX_RANGE_SIZE=1048576
AUDIO_ENABLE_CACHING=true

# Logging
LOG_LEVEL=INFO

# JWT Expiration
JWT_EXPIRATION=2592000000
```

### Port Configuration
- **9100**: Backend API (main service)
- **27017**: MongoDB (database)
- **8081**: Mongo Express (database management)
- **80/443**: Nginx (optional reverse proxy)

## 🏗️ Manual Deployment

### Development Environment
```bash
# Start development environment
docker-compose -f docker-compose.linux.yml up -d

# View logs
docker-compose -f docker-compose.linux.yml logs -f

# Stop services
docker-compose -f docker-compose.linux.yml down
```

### Production Environment
```bash
# Start production environment
docker-compose -f docker-compose.linux.prod.yml up -d

# Start with Nginx reverse proxy
docker-compose -f docker-compose.linux.prod.yml --profile nginx up -d

# View logs
docker-compose -f docker-compose.linux.prod.yml logs -f

# Stop services
docker-compose -f docker-compose.linux.prod.yml down
```

## 🔍 Monitoring and Health Checks

### Health Check Endpoints
- **API Health**: `http://localhost:9100/actuator/health`
- **MongoDB**: Automatic health checks in Docker Compose
- **Container Status**: `docker-compose -f docker-compose.linux.yml ps`

### Logs and Debugging
```bash
# View all logs
docker-compose -f docker-compose.linux.yml logs

# View specific service logs
docker-compose -f docker-compose.linux.yml logs breakup-stories-api

# Follow logs in real-time
docker-compose -f docker-compose.linux.yml logs -f

# View container logs directly
docker logs breakup-stories-api-linux
```

### Performance Monitoring
```bash
# Check resource usage
docker stats

# Check disk usage
docker system df

# Clean up unused resources
docker system prune -a
```

## 🔒 Security Considerations

### Production Security
1. **Change default passwords** in `.env` file
2. **Use strong JWT secrets**
3. **Enable SSL/TLS** with Nginx
4. **Restrict network access** to MongoDB
5. **Regular security updates**

### SSL/TLS Setup
```bash
# Create SSL directory
mkdir -p ssl

# Generate self-signed certificate (for testing)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/key.pem -out ssl/cert.pem

# Start with Nginx
docker-compose -f docker-compose.linux.prod.yml --profile nginx up -d
```

## 📊 Scaling and Performance

### Resource Limits
The production compose file includes resource limits:
- **API**: 2GB memory, 1 CPU core
- **MongoDB**: 1GB memory
- **Nginx**: Minimal resources

### Load Balancing
To scale horizontally:
1. Add more API instances in `nginx.linux.conf`
2. Use external load balancer
3. Consider using Docker Swarm or Kubernetes

### Performance Optimization
- **JVM tuning**: Optimized for Linux containers
- **MongoDB indexing**: Automatic on startup
- **Nginx caching**: Configured for static content
- **Audio streaming**: Optimized for range requests

## 🛠️ Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Check what's using port 9100
sudo netstat -tulpn | grep :9100

# Kill process using the port
sudo kill -9 <PID>
```

#### MongoDB Connection Issues
```bash
# Check MongoDB logs
docker-compose -f docker-compose.linux.yml logs mongodb

# Test MongoDB connection
docker exec breakup-stories-mongodb-linux mongosh --eval "db.adminCommand('ping')"
```

#### API Not Starting
```bash
# Check API logs
docker-compose -f docker-compose.linux.yml logs breakup-stories-api

# Check environment variables
docker exec breakup-stories-api-linux env | grep SPRING
```

#### Memory Issues
```bash
# Check available memory
free -h

# Increase swap if needed
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### Debug Mode
```bash
# Run in debug mode
docker-compose -f docker-compose.linux.yml up

# Access container shell
docker exec -it breakup-stories-api-linux /bin/bash
```

## 🔄 Updates and Maintenance

### Updating the Application
```bash
# Pull latest image
docker pull madhukinnera/breakup-stories-api:linux-latest

# Restart services
docker-compose -f docker-compose.linux.yml restart breakup-stories-api
```

### Database Backup
```bash
# Backup MongoDB
docker exec breakup-stories-mongodb-linux mongodump --out /backup

# Copy backup from container
docker cp breakup-stories-mongodb-linux:/backup ./backup
```

### Database Restore
```bash
# Copy backup to container
docker cp ./backup breakup-stories-mongodb-linux:/backup

# Restore MongoDB
docker exec breakup-stories-mongodb-linux mongorestore /backup
```

## 📞 Support

### Useful Commands
```bash
# Check service status
docker-compose -f docker-compose.linux.yml ps

# View resource usage
docker stats

# Check network connectivity
docker network ls
docker network inspect breakup_be_breakup-network

# Clean up everything
docker-compose -f docker-compose.linux.yml down -v
docker system prune -a
```

### Log Locations
- **Application logs**: Docker container logs
- **Nginx logs**: `/var/log/nginx/` (if using Nginx)
- **MongoDB logs**: Docker container logs

## 🎯 Next Steps

1. **Set up monitoring**: Consider Prometheus + Grafana
2. **Implement CI/CD**: Automated deployment pipeline
3. **Add backup strategy**: Automated database backups
4. **Security hardening**: Regular security audits
5. **Performance tuning**: Monitor and optimize based on usage

---

**Note**: This deployment is optimized for Linux environments. For other operating systems, use the appropriate Docker files and configurations. 