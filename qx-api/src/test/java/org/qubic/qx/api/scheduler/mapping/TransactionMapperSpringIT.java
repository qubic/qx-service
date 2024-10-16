package org.qubic.qx.api.scheduler.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractPostgresTest;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TransactionMapperSpringIT extends AbstractPostgresTest { // new identities are saved to the db

    private static final String TEST_TRANSACTION_DATA = """
            {"transactionHash":"kgmteqsciuhikcqrvqzfnlryywpgtnvtvaphbvgbucqgiqdkwfqtgfbezjto","sourcePublicId":"VFWIEWBYSIMPBDHBXYFJVMLGKCCABZKRYFLQJVZTRBUOYSUHOODPVAHHKXPJ","destinationPublicId":"BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID","amount":1000000,"tick":16519142,"inputType":2,"inputSize":80,"extraData":{"@class":".QxTransferAssetData","issuer":"CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL","assetName":"CFB","newOwner":"JEJDVYSXKRBJJEFTHXJBNDYBIKVABKLIYHWGOFXAPGJYBRUVXHCLUMBFTOFB","numberOfUnits":8239152},"moneyFlew":true}""";

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
        assertThat(mapped.getSourceId()).isPositive();
        assertThat(mapped.getDestinationId()).isPositive();
        assertThat(mapped.getAmount()).isEqualTo(dto.amount());
        assertThat(mapped.getInputSize()).isEqualTo(dto.inputSize());
        assertThat(mapped.getExtraData()).isEqualTo(dto.extraData());
        assertThat(mapped.getMoneyFlew()).isEqualTo(dto.moneyFlew());
    }

}