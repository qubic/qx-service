package org.qubic.qx.api.redis.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.qubic.qx.api.redis.repository.TradesRedisRepository.*;

class TradesRedisRepositorySpringIT extends AbstractSpringIntegrationTest {

    private static final String TEST_TRADE_DATA = """
            {"tick":16570914,"timestamp":1728818707,"transactionHash":"wdbbfxqzqnovtbnktggdpgxdzijbyerhzbrnxmufudyqbeccfhgudpafmijo","bid":false,"taker":"ZYDKPPIJREOAPDQEVLQMLKRNMIXCOJKKXQBRVRBWECKTSBEJEFQDCXYDGHKJ","maker":"KUVGXNCUCJFYKAEHQRXAONPNRPZBFQGOOSFMHSCJVEEHTFZUEUDRNSGAQKLM","issuer":"CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL","assetName":"CFB","price":3,"numberOfShares":10000000}""";

    @Autowired
    private TradesRedisRepository repository;

    @Autowired
    private StringRedisTemplate redisStringTemplate;

    @Test
    void readFromQueue() {
        pushTestTradeIntoReceiveQueue();

        TradeRedisDto dto = repository.readFromQueue();
        assertThat(dto).isEqualTo(new TradeRedisDto(
                16570914,
                1728818707,
                "wdbbfxqzqnovtbnktggdpgxdzijbyerhzbrnxmufudyqbeccfhgudpafmijo",
                false,
                "ZYDKPPIJREOAPDQEVLQMLKRNMIXCOJKKXQBRVRBWECKTSBEJEFQDCXYDGHKJ",
                "KUVGXNCUCJFYKAEHQRXAONPNRPZBFQGOOSFMHSCJVEEHTFZUEUDRNSGAQKLM",
                "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL",
                "CFB",
                3,
                10000000
        ));

        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_RECEIVE)).isZero();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_PROCESS)).isOne();
        assertThat(redisStringTemplate.opsForList().remove(KEY_QUEUE_PROCESS, 1, TEST_TRADE_DATA)).isOne();
    }

    @Test
    void readFromQueue_givenEmpty_thenReturnEmpty() {
        TradeRedisDto dto = repository.readFromQueue();
        assertThat(dto).isNull();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_PROCESS)).isZero();
    }

    @Test
    void removeFromProcessingQueue() {
        pushTestTradeIntoReceiveQueue();
        TradeRedisDto dto = repository.readFromQueue();
        assertThat(repository.removeFromProcessingQueue(dto)).isOne();
        assertThat(redisStringTemplate.opsForList().size(TransactionsRedisRepository.KEY_QUEUE_RECEIVE)).isZero();
        assertThat(redisStringTemplate.opsForList().size(TransactionsRedisRepository.KEY_QUEUE_PROCESS)).isZero();
    }

    @Test
    void moveIntoErrorsQueue() {
        TradeRedisDto dto = JsonUtil.fromJson(TEST_TRADE_DATA, TradeRedisDto.class);
        assertThat(repository.pushIntoErrorsQueue(dto)).isOne();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_ERRORS)).isOne();
        assertThat(redisStringTemplate.opsForList().rightPop(KEY_QUEUE_ERRORS)).isEqualTo(TEST_TRADE_DATA);
    }

    private void pushTestTradeIntoReceiveQueue() {
        Long count = redisStringTemplate.opsForList().leftPush(KEY_QUEUE_RECEIVE, TEST_TRADE_DATA);
        assertThat(count).isOne();
    }

}