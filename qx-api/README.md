# QX API Service

Service that provides endpoints for a QX frontend / integrator.

## Installation

You need Postgresql and Redis installed.

## Configuration Properties

You can use all spring boot configuration properties. For additional configuration properties check out the
see the [application.properties](src/main/resources/application.properties) file in the source code.

### General

Set server port.

```properties
server.port=8081
```

### Redis

Defaults point to localhost and the default redis port without authorization. You can change that with the following
properties:

```properties
spring.data.redis.port=...
spring.data.redis.host=...
spring.data.redis.password=...
```

### Postgresql

You need to configure the database connection. Make sure that user and schema are created before startup. The flyway
migrations will update the database.

#### Create database user and schema

Example with user `qx` and database `qx`:

```postgresql
CREATE USER qx WITH PASSWORD '<enter-your-password-here>';
CREATE DATABASE qx OWNER qx;
```

#### Configura database connections

```properties
# database (application)
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...

# flyway database migrations
spring.flyway.url=...
spring.flyway.user=...
spring.flyway.password=...
```

### Schedulers

Schedulers can be configured with cron syntax or disabled with `-`. Schedulers are disable by default.

```properties
scheduler.sync.cron=*/15 * * * * * # sync every 15 seconds
scheduler.migrate.cron=- # disabled
```

The migration scheduler is only necessary if you have old transactions without timestamp stored in your database. Leave
it disabled for new installations.

### Endpoints

If you want to access secured endpoints (like prometheus metrics) you need to authenticate and configure user information.

```properties
spring.security.user.name=...
spring.security.user.password=...
```

## API

See [OpenAPI descriptions](api-docs.yaml).
