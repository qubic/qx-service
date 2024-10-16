# qx-sync

## Installation

Redis needs to be running. Defaults to `localhost` at port `6379`. Configure with

```properties
spring.data.redis.port=...
spring.data.redis.host=...
```

## Migration

To move all transactions into the transactions queue:
```shell
redis-cli --raw keys 'tx:*' | sed 's/^.*tx:/get tx:/' | redis-cli | sed 's/\"/\\\"/g;s/^/lpush test "/;s/$/"/'
```

1. get keys
2. get values
3. escape " characters with \"
4. add " at beginning and end
5. left push values
