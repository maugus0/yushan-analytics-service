# Yushan Analytics Service

> 📊 **Analytics Service for Yushan Webnovel Platform.** - Tracks user behavior, generates insights, and provides real-time analytics for the gamified web novel reading experience.

# Yushan Platform - Analytics Service Setup Guide

## Architecture Overview

```
┌─────────────────────────────┐
│   Eureka Service Registry   │
│       localhost:8761        │
└──────────────┬──────────────┘
               │
┌──────────────┴──────────────┐
│   Service Registration &     │
│      Discovery Layer         │
└──────────────┬──────────────┘
               │
    ┌──────────┴──────────┬───────────────┬──────────┬──────────┐
    │                     │               │          │          │
    ▼                     ▼               ▼          ▼          ▼
┌────────┐          ┌─────────┐  ┌────────────┐ ┌──────────┐ ┌──────────┐
│  User  │          │ Content │  │ Engagement │ │Gamifica- │ │Analytics │
│Service │          │ Service │  │  Service   │ │  tion    │ │ Service  │
│ :8081  │◄────────►│  :8082  │  │   :8084    │ │ Service  │ │  :8083   │◄──┐
└────────┘          └─────────┘  └────────────┘ │  :8085   │ └──────────┘   │
    │                     │              │       └──────────┘      │          │
    └─────────────────────┴──────────────┴───────────────────────┘          │
                    Inter-service Communication                               │
                      (via Feign Clients)                                     │
                                                                              │
                    ┌─────────────────────────────────────────────────────────┘
                    │   Analytics Data Collection & Processing
                    ▼
            ┌───────────────┐
            │  Time-Series  │
            │   Database    │
            │  (InfluxDB/   │
            │  PostgreSQL)  │
            └───────────────┘
```

---
## Prerequisites

Before setting up the Analytics Service, ensure you have:
1. **Java 21** installed
2. **Maven 3.8+** or use the included Maven wrapper
3. **Eureka Service Registry** running
4. **PostgreSQL 15+** (for analytics data storage)
5. **Redis** (optional, for caching and real-time metrics)

---
## Step 1: Start Eureka Service Registry

**IMPORTANT**: The Eureka Service Registry must be running before starting any microservice.

```bash
# Clone the service registry repository
git clone https://github.com/maugus0/yushan-platform-service-registry
cd yushan-platform-service-registry

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally
./mvnw spring-boot:run
```

### Verify Eureka is Running

- Open: http://localhost:8761
- You should see the Eureka dashboard

---

## Step 2: Clone the Analytics Service Repository

```bash
git clone https://github.com/maugus0/yushan-analytics-service.git
cd yushan-analytics-service

# Option 1: Run with Docker (Recommended)
docker-compose up -d

# Option 2: Run locally (requires PostgreSQL 15 to be running beforehand)
./mvnw spring-boot:run
```

---

## Expected Output

### Console Logs (Success)

```
2024-10-16 10:30:15 - Starting AnalyticsServiceApplication
2024-10-16 10:30:18 - Tomcat started on port(s): 8083 (http)
2024-10-16 10:30:20 - DiscoveryClient_ANALYTICS-SERVICE/analytics-service:8083 - registration status: 204
2024-10-16 10:30:20 - Started AnalyticsServiceApplication in 9.2 seconds
```

### Eureka Dashboard

```
Instances currently registered with Eureka:
✅ ANALYTICS-SERVICE - 1 instance(s)
   Instance ID: analytics-service:8083
   Status: UP (1)
```

---

## API Endpoints

### Health Check
- **GET** `/api/v1/health` - Service health status

### Event Tracking
- **POST** `/api/v1/analytics/events` - Track user events (reads, clicks, sessions)
- **POST** `/api/v1/analytics/events/batch` - Batch event tracking

### User Analytics
- **GET** `/api/v1/analytics/users/{userId}/summary` - Get user analytics summary
- **GET** `/api/v1/analytics/users/{userId}/reading-stats` - Get reading statistics
- **GET** `/api/v1/analytics/users/{userId}/engagement` - Get engagement metrics

### Content Analytics
- **GET** `/api/v1/analytics/novels/{novelId}/stats` - Get novel statistics
- **GET** `/api/v1/analytics/novels/{novelId}/readers` - Get reader demographics
- **GET** `/api/v1/analytics/novels/{novelId}/trends` - Get trending metrics

### Platform Analytics
- **GET** `/api/v1/analytics/platform/dashboard` - Platform-wide dashboard metrics
- **GET** `/api/v1/analytics/platform/active-users` - Active users count
- **GET** `/api/v1/analytics/platform/popular-content` - Most popular content

### Reports
- **GET** `/api/v1/analytics/reports/daily` - Generate daily analytics report
- **GET** `/api/v1/analytics/reports/weekly` - Generate weekly analytics report
- **GET** `/api/v1/analytics/reports/monthly` - Generate monthly analytics report

---

## Key Features

### 📈 Real-Time Analytics
- Track user reading sessions
- Monitor engagement metrics
- Real-time active user counts
- Live trending content

### 📊 Aggregated Metrics
- Daily/Weekly/Monthly summaries
- User retention analytics
- Content popularity rankings
- Reading time analytics

### 🎯 User Behavior Tracking
- Page views and chapter reads
- Time spent on content
- Navigation patterns
- Feature usage statistics

### 📉 Performance Metrics
- API response times
- Service health monitoring
- Error rate tracking
- System resource usage

---

## Database Schema

The Analytics Service uses the following key entities:

- **UserEvent** - Individual user interaction events
- **ReadingSession** - User reading session data
- **ContentMetrics** - Aggregated content statistics
- **UserMetrics** - Aggregated user statistics
- **PlatformMetrics** - Platform-wide metrics

---

## Next Steps

Once this basic setup is working:
1. ✅ Create database entities (UserEvent, ReadingSession, ContentMetrics, etc.)
2. ✅ Set up Flyway migrations for time-series tables
3. ✅ Create repositories and services for data aggregation
4. ✅ Implement API endpoints for analytics queries
5. ✅ Add Feign clients for fetching data from other services
6. ✅ Set up Redis for caching aggregated metrics
7. ✅ Implement scheduled jobs for data aggregation
8. ✅ Add data retention policies
9. ✅ Set up dashboard visualization endpoints

---
## Troubleshooting

**Problem: Service won't register with Eureka**
- Ensure Eureka is running: `docker ps`
- Check logs: Look for "DiscoveryClient" messages
- Verify defaultZone URL is correct

**Problem: Port 8083 already in use**
- Find process: `lsof -i :8083` (Mac/Linux) or `netstat -ano | findstr :8083` (Windows)
- Kill process or change port in application.yml

**Problem: Database connection fails**
- Verify PostgreSQL is running: `docker ps | grep yushan-postgres`
- Check database credentials in application.yml
- Test connection: `psql -h localhost -U yushan_analytics -d yushan_analytics`

**Problem: Build fails**
- Ensure Java 21 is installed: `java -version`
- Check Maven: `./mvnw -version`
- Clean and rebuild: `./mvnw clean install -U`

**Problem: High memory usage**
- Consider implementing data aggregation strategies
- Set up data archiving for old events
- Implement proper indexing on time-series queries
- Configure connection pool sizes appropriately

**Problem: Slow query performance**
- Add database indexes on frequently queried columns
- Implement caching for aggregated metrics
- Consider using database partitioning for large tables
- Use batch processing for event ingestion

---

## Performance Tips
1. **Event Batching**: Use batch endpoints for high-volume event tracking
2. **Caching**: Enable Redis caching for frequently accessed metrics
3. **Data Retention**: Implement policies to archive old data
4. **Indexing**: Ensure proper database indexes on timestamp and user_id columns
5. **Async Processing**: Use asynchronous processing for non-critical analytics

---

## Monitoring
The Analytics Service exposes metrics through:
- Spring Boot Actuator endpoints (`/actuator/metrics`)
- Custom analytics dashboards
- Prometheus-compatible metrics (if configured)

---

## Inter-Service Communication
The Analytics Service communicates with:
- **User Service**: Fetch user profile data
- **Content Service**: Fetch novel and chapter metadata
- **Engagement Service**: Correlate engagement events
- **Gamification Service**: Track achievement-related analytics

---

## Security Considerations
- API endpoints should be secured with authentication
- Implement rate limiting for event tracking endpoints
- Validate all incoming event data
- Sanitize user IDs and content IDs in queries
- Use read-only database replicas for reporting queries

---

## License
This project is part of the Yushan Platform ecosystem.
