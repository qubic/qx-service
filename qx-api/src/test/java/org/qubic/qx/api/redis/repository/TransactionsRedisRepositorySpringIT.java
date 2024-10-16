package org.qubic.qx.api.redis.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.qubic.qx.api.redis.repository.TransactionsRedisRepository.*;

class TransactionsRedisRepositorySpringIT extends AbstractSpringIntegrationTest {

    private static final String TEST_TRANSACTION_DATA = """
            {"transactionHash":"kgmteqsciuhikcqrvqzfnlryywpgtnvtvaphbvgbucqgiqdkwfqtgfbezjto","sourcePublicId":"VFWIEWBYSIMPBDHBXYFJVMLGKCCABZKRYFLQJVZTRBUOYSUHOODPVAHHKXPJ","destinationPublicId":"BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID","amount":1000000,"tick":16519142,"inputType":2,"inputSize":80,"extraData":{"@class":".QxTransferAssetData","issuer":"CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL","assetName":"CFB","newOwner":"JEJDVYSXKRBJJEFTHXJBNDYBIKVABKLIYHWGOFXAPGJYBRUVXHCLUMBFTOFB","numberOfUnits":8239152},"moneyFlew":true}""";
    @Autowired
    private TransactionsRedisRepository repository;

    @Autowired
    private StringRedisTemplate redisStringTemplate;

    @Test
    void readFromQueue() {
        pushTransactionIntoReceiveQueue();

        TransactionRedisDto dto = repository.readFromQueue();
        assertThat(dto).isEqualTo(new TransactionRedisDto(
                "kgmteqsciuhikcqrvqzfnlryywpgtnvtvaphbvgbucqgiqdkwfqtgfbezjto",
                "VFWIEWBYSIMPBDHBXYFJVMLGKCCABZKRYFLQJVZTRBUOYSUHOODPVAHHKXPJ",
                "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID",
                1_000_000,
                16519142,
                2,
                80,
                new QxTransferAssetData("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL",
                        "CFB",
                        "JEJDVYSXKRBJJEFTHXJBNDYBIKVABKLIYHWGOFXAPGJYBRUVXHCLUMBFTOFB",
                        8239152),
                true
        ));

        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_RECEIVE)).isZero();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_PROCESS)).isOne();
        assertThat(redisStringTemplate.opsForList().remove(KEY_QUEUE_PROCESS, 1, TEST_TRANSACTION_DATA)).isOne();
    }

    @Test
    void readFromQueue_givenEmpty_thenReturnEmpty() {
        TransactionRedisDto dto = repository.readFromQueue();
        assertThat(dto).isNull();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_PROCESS)).isZero();
    }

    @Test
    void removeFromProcessingQueue() {
        pushTransactionIntoReceiveQueue();
        TransactionRedisDto dto = repository.readFromQueue();
        assertThat(repository.removeFromProcessingQueue(dto)).isOne();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_RECEIVE)).isZero();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_PROCESS)).isZero();
    }

    @Test
    void moveIntoErrorsQueue() {
        TransactionRedisDto dto = JsonUtil.fromJson(TEST_TRANSACTION_DATA, TransactionRedisDto.class);
        assertThat(repository.pushIntoErrorsQueue(dto)).isOne();
        assertThat(redisStringTemplate.opsForList().size(KEY_QUEUE_ERRORS)).isOne();
        assertThat(redisStringTemplate.opsForList().rightPop(KEY_QUEUE_ERRORS)).isEqualTo(TEST_TRANSACTION_DATA);
    }

    private void pushTransactionIntoReceiveQueue() {
        Long count = redisStringTemplate.opsForList().leftPush(KEY_QUEUE_RECEIVE, TEST_TRANSACTION_DATA);
        assertThat(count).isOne();
    }

}