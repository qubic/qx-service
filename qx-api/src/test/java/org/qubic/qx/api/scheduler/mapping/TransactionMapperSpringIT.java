package org.qubic.qx.api.scheduler.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperSpringIT extends AbstractSpringIntegrationTest { // new identities are saved to the db

    private static final String TEST_TRANSACTION_DATA = """
            {"transactionHash":"kgmteqsciuhikcqrvqzfnlryywpgtnvtvaphbvgbucqgiqdkwfqtgfbezjto","sourcePublicId":"VFWIEWBYSIMPBDHBXYFJVMLGKCCABZKRYFLQJVZTRBUOYSUHOODPVAHHKXPJ","destinationPublicId":"BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID","amount":1000000,"tick":16519142,"timestamp":1728818707,"inputType":2,"inputSize":80,"extraData":{"@class":".QxTransferAssetData","issuer":"CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL","name":"CFB","newOwner":"JEJDVYSXKRBJJEFTHXJBNDYBIKVABKLIYHWGOFXAPGJYBRUVXHCLUMBFTOFB","numberOfShares":8239152},"moneyFlew":true}""";

    @Autowired
    private TransactionMapper transactionMapper;

    @Transactional // roll back
    @Test
    void map() {
        TransactionRedisDto dto = JsonUtil.fromJson(TEST_TRANSACTION_DATA, TransactionRedisDto.class);
        Transaction mapped = transactionMapper.map(dto);
        assertThat(mapped).isNotNull();
        assertThat(mapped.getId()).isNull();
        assertThat(mapped.getHash()).isEqualTo(dto.transactionHash());
        assertThat(mapped.getTick()).isEqualTo(dto.tick());
        assertThat(mapped.getTickTime()).isEqualTo(Instant.ofEpochSecond(1728818707));
        assertThat(mapped.getSourceId()).isPositive();
        assertThat(mapped.getDestinationId()).isPositive();
        assertThat(mapped.getAmount()).isEqualTo(dto.amount());
        assertThat(mapped.getInputSize()).isEqualTo(dto.inputSize());
        assertThat(mapped.getExtraData()).isEqualTo(dto.extraData());
        assertThat(mapped.getMoneyFlew()).isEqualTo(dto.moneyFlew());
    }

}