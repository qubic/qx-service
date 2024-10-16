# QX API Service

Service that provides endpoints for a QX frontend / integrator.

## Installation

### Redis

Redis needs to be running. Defaults to `localhost` at port `6379`. Configure with

```properties
spring.data.redis.port=...
spring.data.redis.host=...
```

### Postgresql

#### Create database user and schema

```postgresql
CREATE USER qx WITH PASSWORD '<enter-your-password-here>';
CREATE DATABASE qx OWNER qx;
```
