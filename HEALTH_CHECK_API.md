# Health Check API Documentation

This document describes the health check endpoints available in the Breakup Stories Backend API.

## Endpoints

### 1. Ping Endpoint

**URL:** `GET /api/health/ping`

**Description:** Simple ping endpoint to check if the service is accessible and running.

**Authentication:** None required - Public endpoint

**Response:**
```json
{
  "status": "OK",
  "message": "Service is running",
  "timestamp": "2024-01-15T10:30:45.123",
  "service": "Breakup Stories Backend"
}
```

**HTTP Status Codes:**
- `200 OK`: Service is running normally
- `500 Internal Server Error`: Service is not accessible

### 2. Health Status Endpoint

**URL:** `GET /api/health/status`

**Description:** Detailed health check endpoint that provides comprehensive information about the service status.

**Authentication:** None required - Public endpoint

**Response:**
```json
{
  "status": "HEALTHY",
  "message": "All systems operational",
  "timestamp": "2024-01-15T10:30:45.123",
  "service": "Breakup Stories Backend",
  "version": "1.0.0",
  "components": {
    "database": "UP",
    "api": "UP",
    "fileStorage": "UP"
  }
}
```

**HTTP Status Codes:**
- `200 OK`: All systems are healthy
- `503 Service Unavailable`: One or more components are down

## Usage Examples

### Using curl

```bash
# Simple ping
curl -X GET http://localhost:8080/api/health/ping

# Detailed health check
curl -X GET http://localhost:8080/api/health/status
```

### Using JavaScript/Fetch

```javascript
// Simple ping
fetch('/api/health/ping')
  .then(response => response.json())
  .then(data => console.log('Service status:', data.status));

// Detailed health check
fetch('/api/health/status')
  .then(response => response.json())
  .then(data => {
    console.log('Overall status:', data.status);
    console.log('Components:', data.components);
  });
```

## Monitoring and Alerts

These endpoints can be used for:

1. **Load Balancer Health Checks**: Configure your load balancer to use `/api/health/ping` for health checks
2. **Monitoring Systems**: Use `/api/health/status` for detailed monitoring and alerting
3. **Application Monitoring**: Integrate with tools like Prometheus, Grafana, or New Relic
4. **Automated Testing**: Include health checks in your CI/CD pipeline

## Implementation Notes

- Both endpoints are lightweight and should respond quickly
- The ping endpoint is designed for simple availability checks
- The status endpoint provides more detailed information for monitoring purposes
- All responses include timestamps for tracking when the check was performed
- **The endpoints are publicly accessible and do not require authentication**
- This makes them perfect for external monitoring systems and load balancers 