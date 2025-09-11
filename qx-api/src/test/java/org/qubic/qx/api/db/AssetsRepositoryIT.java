package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetsRepositoryIT extends AbstractPostgresJdbcTest {

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

        List<Asset> assets = repository.findAll();
        List<Tuple3<String, String, Boolean>> assetList = assets.stream().map(a -> Tuples.of(a.getIssuer(), a.getName(), a.isVerified())).toList();
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QX", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "RANDOM", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QUTIL", true));
        assertThat(assetList).contains(Tuples.of("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", true));
        assertThat(assetList).contains(Tuples.of("TFUYVBXYIYBVTEMJHAJGEJOOZHJBQFVQLTBBKMEHPEVIZFXZRPEYFUWGTIWG", "QFT", true));

        assertThat(assetList).doesNotContain(Tuples.of("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "QPOOL", false));
        assertThat(assetList).doesNotContain(Tuples.of("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "QPOOL", true));

    }

    @Test
    public void findByIssuerAndName() {
        assertThat(repository.findByIssuerAndName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QX")).isNotEmpty();
        assertThat(repository.findByIssuerAndName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "QPOOL")).isEmpty(); // removed
        assertThat(repository.findByIssuerAndName("FOO", "BAR")).isEmpty();
    }

}
