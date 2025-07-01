# 🐳 Docker Setup Guide for Activity Management System

Complete Docker configuration for running the Activity Management System with all its dependencies.

## 📋 Overview

This Docker setup includes:
- **Spring Boot Application** - Main backend service
- **MySQL 8.0** - Primary database
- **Redis 7** - Caching and SocketIO session management  
- **phpMyAdmin** - Database management UI
- **Redis Commander** - Redis management UI
- **Nginx** - Reverse proxy and load balancer (production)

## 🚀 Quick Start

### 1. Environment Setup
```bash
# Copy environment template
cp docker/env-template .env

# Edit .env with your configuration
nano .env
```

### 2. Basic Development Setup
```bash
# Start MySQL and Redis only
docker-compose up mysql redis

# Start the full development stack
docker-compose --profile dev up -d
```

### 3. Production Setup
```bash
# Start production stack with Nginx
docker-compose --profile prod up -d
```

## ⚙ Configuration Files

### Environment Variables (`.env`)
Create a `.env` file from the template:
```bash
cp docker/env-template .env
```

Required variables:
- `MAIL_USERNAME` - Your Gmail address
- `MAIL_PASSWORD` - Gmail app password (16 characters)
- `JWT_SECRET` - Strong secret key for JWT tokens

### Directory Structure
```
├── docker/
│   ├── mysql/
│   │   └── init.sql           # MySQL initialization script
│   ├── redis/
│   │   └── redis.conf         # Redis configuration
│   ├── nginx/
│   │   ├── nginx.conf         # Nginx configuration
│   │   └── ssl/               # SSL certificates (optional)
│   └── env-template           # Environment template
├── Dockerfile                 # Application container
└── docker-compose.yml         # Docker Compose configuration
```

## 🔧 Service Details

### Spring Boot Application
- **Ports**: 8080 (HTTP), 9092 (SocketIO)
- **Health Check**: `http://localhost:8080/actuator/health`
- **Profile**: `docker`
- **Features**: 
  - Multi-stage build for optimized image size
  - Non-root user for security
  - JVM memory optimization
  - Health checks

### MySQL Database
- **Port**: 3306
- **Database**: `activity`
- **Root Password**: `rootpassword`
- **Features**:
  - UTF8MB4 character set
  - Performance optimizations
  - Data persistence
  - Health checks

### Redis Cache
- **Port**: 6379
- **Features**:
  - 256MB memory limit
  - LRU eviction policy
  - AOF persistence
  - Optimized for SocketIO
  - TTL-based cleanup

### Management UIs

#### phpMyAdmin
- **URL**: http://localhost:8082
- **Username**: `root`
- **Password**: `rootpassword`

#### Redis Commander  
- **URL**: http://localhost:8081
- **Username**: `admin`
- **Password**: `admin`

## 🏃 Running Different Configurations

### Development Mode
```bash
# Start with management UIs
docker-compose --profile dev up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose --profile dev down
```

### Production Mode
```bash
# Start with Nginx reverse proxy
docker-compose --profile prod up -d

# Scale the application (load balancing)
docker-compose up -d --scale app=3

# Stop production stack
docker-compose --profile prod down
```

### Debug Mode
```bash
# Start with debug profile (includes all UIs)
docker-compose --profile debug up -d

# Access application logs
docker-compose logs -f app

# Access Redis logs
docker-compose logs -f redis

# Access MySQL logs
docker-compose logs -f mysql
```

## 🔍 Monitoring & Troubleshooting

### Health Checks
```bash
# Check all service health
docker-compose ps

# Check application health
curl http://localhost:8080/actuator/health

# Check database connection
docker-compose exec mysql mysqladmin ping -h localhost -u root -prootpassword

# Check Redis
docker-compose exec redis redis-cli ping
```

### Logs
```bash
# View all logs
docker-compose logs

# Follow application logs
docker-compose logs -f app

# View last 100 lines
docker-compose logs --tail=100 app

# View logs for specific service
docker-compose logs mysql
docker-compose logs redis
```

### Database Access
```bash
# Connect to MySQL
docker-compose exec mysql mysql -u root -prootpassword activity

# Connect to Redis
docker-compose exec redis redis-cli

# View Redis keys
docker-compose exec redis redis-cli keys "socket:*"
```

## 📊 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Main App** | http://localhost:8080 | Spring Boot REST API |
| **SocketIO** | http://localhost:9092 | Real-time WebSocket |
| **phpMyAdmin** | http://localhost:8082 | Database management |
| **Redis Commander** | http://localhost:8081 | Redis management |
| **Nginx** | http://localhost:80 | Reverse proxy (prod) |
| **Health Check** | http://localhost:8080/actuator/health | Application health |

## 🔒 Security Considerations

### Development
- Default passwords are used for convenience
- Management UIs are exposed on all interfaces
- No SSL/TLS encryption

### Production
- Change all default passwords
- Use strong JWT secrets
- Configure SSL certificates in Nginx
- Restrict management UI access
- Use Docker secrets for sensitive data

### Production Security Checklist
```bash
# 1. Generate strong JWT secret
openssl rand -base64 32

# 2. Use Docker secrets
echo "strong-db-password" | docker secret create db_password -

# 3. Configure SSL certificates
mkdir -p docker/nginx/ssl
# Add your cert.pem and key.pem files

# 4. Update nginx.conf for HTTPS
# Uncomment the HTTPS server block

# 5. Use environment-specific configurations
SPRING_PROFILES_ACTIVE=production docker-compose --profile prod up -d
```

## 🚀 Deployment Options

### Single Server Deployment
```bash
# Production deployment
docker-compose --profile prod up -d

# With SSL
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Scaling & Load Balancing
```bash
# Scale application instances
docker-compose up -d --scale app=3

# Nginx will automatically load balance between instances
# Redis handles SocketIO session stickiness via ip_hash
```

### Container Registry Deployment
```bash
# Build and tag image
docker build -t your-registry/activity-management:latest .

# Push to registry
docker push your-registry/activity-management:latest

# Update docker-compose.yml to use registry image
# image: your-registry/activity-management:latest
```

## 🛠 Development Workflow

### Making Changes
```bash
# 1. Make code changes
# 2. Rebuild application container
docker-compose build app

# 3. Restart application
docker-compose restart app

# 4. View logs
docker-compose logs -f app
```

### Database Management
```bash
# Backup database
docker-compose exec mysql mysqldump -u root -prootpassword activity > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -prootpassword activity < backup.sql

# Reset database
docker-compose down
docker volume rm smarte-vent-backend_mysql-data
docker-compose up -d
```

### Redis Management
```bash
# Clear Redis cache
docker-compose exec redis redis-cli FLUSHALL

# View SocketIO keys
docker-compose exec redis redis-cli keys "socket:*"

# Monitor Redis operations
docker-compose exec redis redis-cli monitor
```

## 🔧 Troubleshooting

### Common Issues

#### Application Won't Start
```bash
# Check logs
docker-compose logs app

# Check dependencies
docker-compose ps

# Restart services
docker-compose restart app
```

#### Database Connection Issues
```bash
# Verify MySQL is running
docker-compose ps mysql

# Check MySQL logs
docker-compose logs mysql

# Test connection
docker-compose exec app ping mysql
```

#### Redis Connection Issues
```bash
# Check Redis status
docker-compose exec redis redis-cli ping

# Check Redis logs
docker-compose logs redis

# Verify Redis configuration
docker-compose exec redis cat /etc/redis/redis.conf
```

#### SocketIO Issues
```bash
# Check port accessibility
netstat -tlnp | grep :9092

# Check Nginx configuration (if using)
docker-compose exec nginx nginx -t

# Test WebSocket connection
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" http://localhost:9092/socket.io/
```

## 📈 Performance Optimization

### Resource Limits
```yaml
# Add to docker-compose.yml
services:
  app:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
        reservations:
          memory: 512M
          cpus: '0.5'
```

### Cache Optimization
```bash
# Monitor Redis memory usage
docker-compose exec redis redis-cli info memory

# Optimize Redis configuration
# Edit docker/redis/redis.conf
```

### Database Optimization
```bash
# Monitor MySQL performance
docker-compose exec mysql mysql -u root -prootpassword -e "SHOW PROCESSLIST;"

# Check slow queries
docker-compose exec mysql mysql -u root -prootpassword -e "SHOW STATUS LIKE 'Slow_queries';"
```

---

**📋 Need Help?**
- Check the main [README.md](README.md) for application documentation
- Review logs: `docker-compose logs -f app`
- Verify health: `curl http://localhost:8080/actuator/health`
- Access management UIs for debugging 
