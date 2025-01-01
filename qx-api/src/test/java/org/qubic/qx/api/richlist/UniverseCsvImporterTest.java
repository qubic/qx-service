package org.qubic.qx.api.richlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.AssetsDbService;
import org.qubic.qx.api.db.EntitiesDbService;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.richlist.exception.CsvImportException;
import org.qubic.qx.api.validation.ValidationError;
import org.qubic.qx.api.validation.ValidationUtility;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UniverseCsvImporterTest {

    private final ValidationUtility validationUtility = mock();
    private final EntitiesDbService entitiesDbService = mock();
    private final AssetsDbService assetsDbService = mock();
    private final AssetOwnersRepository assetOwnersRepository = mock();
    private final UniverseCsvImporter importer = new UniverseCsvImporter(validationUtility, entitiesDbService, assetsDbService, assetOwnersRepository);

    @BeforeEach
    void initMocks() {
        when(validationUtility.validateIdentity(anyString())).thenReturn(Optional.empty());
        when(validationUtility.validateAmount(any(BigInteger.class))).thenReturn(Optional.empty());
        when(validationUtility.validateAssetName(anyString())).thenReturn(Optional.empty());
        when(assetsDbService.getOrCreateAsset(anyString(), anyString())).thenAnswer(args -> Asset.builder()
                .issuer(args.getArgument(0))
                .name(args.getArgument(1))
                .id(42L)
                .build());
        when(entitiesDbService.getOrCreateEntity(anyString())).thenAnswer(args -> Entity.builder()
                .id(123L)
                .identity(args.getArgument(0))
                .build());
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

        verify(entitiesDbService, times(3)).getOrCreateEntity(anyString());
        verify(assetsDbService, times(3)).getOrCreateAsset(anyString(), anyString()); // cached
        verify(assetOwnersRepository).deleteAll();
        verify(assetOwnersRepository).saveAll(anyList());
    }

    @Test
    void importAssetOwners_givenInvalidRecord_thenError() {

        Reader input = new StringReader("""
                Index,Type,ID,OwnerIndex,ContractIndex,AssetName,AssetIssuer,Amount
                2147003,OWNERSHIP,%s,2147003,1,%s,%s,%s
            """.formatted("SOMEID", "NAMETOOLONG", "ISSUER", "-3"));

        when(validationUtility.validateIdentity(anyString())).thenReturn(Optional.of(new ValidationError("invalid identity")));
        when(validationUtility.validateAmount(any(BigInteger.class))).thenReturn(Optional.of(new ValidationError("invalid amount")));
        when(validationUtility.validateAssetName(anyString())).thenReturn(Optional.of(new ValidationError("invalid asset name")));

        assertThatThrownBy(() -> importer.importAssetOwners(input))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContainingAll("invalid amount",
                        "invalid identity",
                        "invalid identity",
                        "invalid asset name");

        verifyNoInteractions(entitiesDbService);
        verifyNoInteractions(assetsDbService);
        verifyNoInteractions(assetOwnersRepository);
    }

}