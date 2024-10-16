package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractPostgresTest;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
public class AssetsRepositoryIT extends AbstractPostgresTest {

    @Autowired
    private AssetsRepository repository;

    @Test
    public void saveAndLoad() {
        Asset asset = Asset.builder()
                .issuer("FOO")
                .name("BAR")
                .verified(true)
                .build();

        Asset saved = repository.save(asset);
        assertThat(saved.getId()).isNotNull();

        Asset reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);
    }

    @Test
    public void findAllVerified() {

        List<Asset> assets = repository.findByVerifiedIsTrue();
        List<Tuple3<String, String, Boolean>> assetList = assets.stream().map(a -> Tuples.of(a.getIssuer(), a.getName(), a.isVerified())).toList();
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QX", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "RANDOM", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QUTIL", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QTRY", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QPOOL", true));
        assertThat(assetList).contains(Tuples.of("TFUYVBXYIYBVTEMJHAJGEJOOZHJBQFVQLTBBKMEHPEVIZFXZRPEYFUWGTIWG", "QFT", true));
        assertThat(assetList).contains(Tuples.of("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB", true));
        assertThat(assetList).contains(Tuples.of("QWALLETSGQVAGBHUCVVXWZXMBKQBPQQSHRYKZGEJWFVNUFCEDDPRMKTAUVHA", "QWALLET", true));

    }

    @Test
    public void findByIssuerAndName() {
        assertThat(repository.findByIssuerAndName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QX")).isNotEmpty();
        assertThat(repository.findByIssuerAndName("FOO", "BAR")).isEmpty();

    }

}
