# qx-sync

## Installation

### Redis

Redis is used for storing the sync status and filling queues that pass qx data to consumers.

## Configuration Properties

### Redis

Defaults point to localhost and the default redis port without authorization. You can change that with the following
properties:

```properties
spring.data.redis.port=...
spring.data.redis.host=...
spring.data.redis.password=...
```

### Endpoints

If you want to access secured endpoints (like prometheus metrics) you need to authenticate and configure user information.

```properties
spring.security.user.name=...
spring.security.user.password=...
```

## Migration

Only relevant, if you want to switch to a new version from a very old version (0.1.0) without losing data. 
To move all transactions from old storage format into the transactions queue:

```shell
redis-cli --raw keys 'tx:*' | sed 's/^.*tx:/get tx:/' | redis-cli | sed 's/\"/\\\"/g;s/^/lpush queue:transactions "/;s/$/"/' | redis-cli
```
1. get keys
2. get values
3. escape " characters with \"
4. add " at beginning and end
5. left push values

Move all trades into the trades queue:

```shell
redis-cli --raw zrange trades 0 -1 | sed 's/\"/\\\"/g;s/^/lpush queue:trades "/;s/$/"/' | redis-cli
```