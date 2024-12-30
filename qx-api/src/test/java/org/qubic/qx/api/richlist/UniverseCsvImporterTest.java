package org.qubic.qx.api.richlist;

import at.qubic.api.crypto.IdentityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.EntitiesRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.richlist.exception.CsvImportException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UniverseCsvImporterTest {

    private final IdentityUtil identityUtil = mock();
    private final EntitiesRepository entitiesRepository = mock();
    private final AssetsRepository assetsRepository = mock();
    private final AssetOwnersRepository assetOwnersRepository = mock();
    private final UniverseCsvImporter importer = new UniverseCsvImporter(identityUtil, entitiesRepository, assetsRepository, assetOwnersRepository);

    @BeforeEach
    void initMocks() {
        when(identityUtil.isValidIdentity(anyString())).thenReturn(true);
        when(assetsRepository.findByIssuerAndName(anyString(), anyString())).thenReturn(Optional.of(Asset.builder()
                .id(42L)
                .build()));
        when(entitiesRepository.findByIdentity(anyString())).thenAnswer(args -> Optional.of(Entity.builder()
                .id(123L)
                .identity(args.getArgument(0))
                .build()));
    }

    @Test
    void importAssetOwners() throws IOException {

        Reader input = new StringReader("""
                Index,Type,ID,OwnerIndex,ContractIndex,AssetName,AssetIssuer,Amount
                6,ISSUANCE,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,0,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,0
                2147003,OWNERSHIP,FTENZFKHNGPTOEMKLDYBJDLQRFFDLZUKBCBQTSUNHBSBBIVTMPLDAFCFBGWM,2147003,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,1
                2147004,POSSESSION,FTENZFKHNGPTOEMKLDYBJDLQRFFDLZUKBCBQTSUNHBSBBIVTMPLDAFCFBGWM,2147003,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,1
                2183131,OWNERSHIP,ZNYIZYPNQBAKWDHAPNDHCJLTTKKCQSGJUEJFUSHMLBGCFFQPCNKAQXGCRECN,2183131,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,15
                2183132,POSSESSION,ZNYIZYPNQBAKWDHAPNDHCJLTTKKCQSGJUEJFUSHMLBGCFFQPCNKAQXGCRECN,2183131,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,15
                SOME,NONSENSE
                14273540,OWNERSHIP,ITHUBNHNEBCSKFLXJIXSGITKVRDDRDNSUJAOJUYPWGFPKUBLQKXRVJEDXJGI,14273540,1,QUTIL,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB,184
                """);

        List<AssetOwner> assetOwners = importer.importAssetOwners(input);
        assertThat(assetOwners.size()).isEqualTo(3);
        assertThat(assetOwners).containsExactlyInAnyOrder(
                // same entity id only in test case
                AssetOwner.builder().assetId(42).entityId(123).amount(BigInteger.valueOf(1)).build(),
                AssetOwner.builder().assetId(42).entityId(123).amount(BigInteger.valueOf(15)).build(),
                AssetOwner.builder().assetId(42).entityId(123).amount(BigInteger.valueOf(184)).build()
        );

        verify(entitiesRepository, times(3)).findByIdentity(anyString());
        verify(assetsRepository, times(1)).findByIssuerAndName(anyString(), anyString()); // cached
        verify(assetOwnersRepository).deleteAll();
        verify(assetOwnersRepository).saveAll(anyList());
    }

    @Test
    void importAssetOwners_givenInvalidRecord_thenError() {

        Reader input = new StringReader("""
                Index,Type,ID,OwnerIndex,ContractIndex,AssetName,AssetIssuer,Amount
                2147003,OWNERSHIP,%s,2147003,1,%s,%s,%s
            """.formatted("SOMEID", "NAMETOOLONG", "ISSUER", "-3"));

        when(identityUtil.isValidIdentity("SOMEID")).thenReturn(false);
        when(identityUtil.isValidIdentity("ISSUER")).thenReturn(false);

        assertThatThrownBy(() -> importer.importAssetOwners(input))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContainingAll("invalid amount [-3]",
                        "invalid identity [SOMEID]",
                        "invalid identity [ISSUER]",
                        "invalid asset name [NAMETOOLONG]");

        verifyNoInteractions(entitiesRepository);
        verifyNoInteractions(assetsRepository);
        verifyNoInteractions(assetOwnersRepository);
    }

}