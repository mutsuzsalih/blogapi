# Security Guidelines

## 🔐 Environment Variables (REQUIRED)

**These environment variables MUST be set before deployment:**

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/dbname
SPRING_DATASOURCE_USERNAME=your-db-user
SPRING_DATASOURCE_PASSWORD=your-secure-db-password

# JWT Configuration (CRITICAL)
JWT_SECRET=your-super-secure-jwt-secret-minimum-256-bits

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Production Settings
SPRING_PROFILES_ACTIVE=prod
SWAGGER_ENABLED=false
SHOW_SQL=false
SECURITY_LOG_LEVEL=WARN
```

## 🚨 Security Checklist

### Before Production Deployment

- [ ] JWT_SECRET environment variable set (minimum 32 characters)
- [ ] Database credentials via environment variables only
- [ ] Swagger disabled (`SWAGGER_ENABLED=false`)
- [ ] SQL logging disabled (`SHOW_SQL=false`)
- [ ] Production CORS origins configured
- [ ] HTTPS enabled for all communications
- [ ] Security headers configured
- [ ] Actuator endpoints restricted

### AWS Security Best Practices

- [ ] RDS database in private subnet
- [ ] Security groups properly configured
- [ ] IAM roles with minimal permissions
- [ ] Secrets Manager for sensitive data
- [ ] CloudFront with HTTPS only
- [ ] S3 buckets not publicly accessible

## 🔍 Security Monitoring

### Health Check Endpoints

- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information

### Logs to Monitor

- Authentication failures
- Authorization violations
- Database connection issues
- JWT token validation errors

## 🚫 Never Commit

- Database passwords
- JWT secrets
- API keys
- Private keys
- Production configuration files

## 📞 Security Issues

If you discover a security vulnerability, please report it privately before public disclosure. 