package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssetOwnersRepositoryIT extends AbstractPostgresJdbcTest {

    @Autowired
    private AssetsRepository assetsRepository;

    @Autowired
    private EntitiesRepository entitiesRepository;

    @Autowired
    private AssetOwnersRepository repository;

    @Test
    void saveAndLoad() {
        Entity entity = Entity.builder()
                .identity("FOO")
                .build();

        entitiesRepository.save(entity);

        Asset asset = Asset.builder()
                .name("NAME")
                .issuer("ISSUER")
                .build();

        assetsRepository.save(asset);

        AssetOwner owner = AssetOwner.builder()
                .assetId(asset.getId())
                .entityId(entity.getId())
                .amount(BigInteger.valueOf(123))
                .build();

        AssetOwner saved = repository.save(owner);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved).isEqualTo(owner);

        AssetOwner reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(owner);
    }

    @Sql(scripts = "/testdata/db/setup-asset_owners-repository-test.sql")
    @Test
    void findByAsset() {

        List<AmountPerEntityDto> asset1Owners = repository.findOwnersByAsset("ISSUER1", "ASSET1", 100);
        assertThat(asset1Owners).containsExactly(
                new AmountPerEntityDto("ID1", BigInteger.valueOf(12345)),
                new AmountPerEntityDto("ID2", BigInteger.valueOf(123))
        );

        List<AmountPerEntityDto> asset1OwnersLimited = repository.findOwnersByAsset("ISSUER1", "ASSET1", 1);
        assertThat(asset1OwnersLimited).containsExactly(
                new AmountPerEntityDto("ID1", BigInteger.valueOf(12345))
        );

        List<AmountPerEntityDto> asset2Owners = repository.findOwnersByAsset("ISSUER2", "ASSET2", 100);
        assertThat(asset2Owners).containsExactly(
                new AmountPerEntityDto("ID1", BigInteger.valueOf(54321))
        );

        assertThat(repository.findOwnersByAsset("FOO", "BAR", 1)).isEmpty();

    }

}