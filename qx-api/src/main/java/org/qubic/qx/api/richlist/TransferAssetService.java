package org.qubic.qx.api.richlist;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.AssetsDbService;
import org.qubic.qx.api.db.EntitiesDbService;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.richlist.exception.TransferAssetException;
import org.qubic.qx.api.validation.ValidationError;
import org.qubic.qx.api.validation.ValidationUtility;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class TransferAssetService {

    private final AssetsDbService assetsDbService;
    private final EntitiesDbService entitiesDbService;
    private final AssetOwnersRepository assetOwnersRepository;
    private final ValidationUtility validationUtility;

    public TransferAssetService(AssetsDbService assetsRepository, EntitiesDbService entitiesRepository, AssetOwnersRepository assetOwnersRepository, ValidationUtility validationUtility) {
        this.assetsDbService = assetsRepository;
        this.entitiesDbService = entitiesRepository;
        this.assetOwnersRepository = assetOwnersRepository;
        this.validationUtility = validationUtility;
    }

    @Transactional
    public void issueAsset(String issuer, String assetName, long numberOfShares) {
        BigInteger amount = new BigInteger(Long.toUnsignedString(numberOfShares));
        log.info("Crediting [{}] [{}] shares to issuer [{}].", amount, assetName, issuer);
        validate(issuer, issuer, issuer, assetName, amount);
        Asset asset = assetsDbService.getOrCreateAsset(issuer, assetName);
        handleDestinationEntity(issuer, asset, amount);
    }

    @Transactional
    public void transfer(String from, String to, String issuer, String assetName, long numberOfShares) {
        BigInteger amount = new BigInteger(Long.toUnsignedString(numberOfShares));
        log.info("Transfer [{}] [{}] shares from [{}] to [{}]. Issuer [{}].", amount, assetName, from, to, issuer);
        validate(from, to, issuer, assetName, amount);
        Asset asset = assetsDbService.getOrCreateAsset(issuer, assetName);
        handleSourceEntity(from, asset, amount);
        handleDestinationEntity(to, asset, amount);
    }

    private void handleDestinationEntity(String to, Asset asset, BigInteger amount) {
        Entity destinationEntity = entitiesDbService.getOrCreateEntity(to);
        Optional<AssetOwner> destinationOwner = assetOwnersRepository.findByAssetIdAndEntityId(asset.getId(), destinationEntity.getId());
        if (destinationOwner.isPresent()) {
            AssetOwner destination = destinationOwner.get();
            destination.setAmount(destination.getAmount().add(amount));
            assetOwnersRepository.save(destination);
            log.info("Added [{}] shares: {}", amount, destination);
        } else {
            AssetOwner destination = AssetOwner.builder()
                    .assetId(asset.getId())
                    .entityId(destinationEntity.getId())
                    .amount(amount)
                    .build();
            assetOwnersRepository.save(destination);
            log.info("Saved new owner {}", destination);
        }
    }

    private void handleSourceEntity(String from, Asset asset, BigInteger amount) {
        Entity sourceEntity = entitiesDbService.getOrCreateEntity(from);
        Optional<AssetOwner> optionalSource = assetOwnersRepository.findByAssetIdAndEntityId(asset.getId(), sourceEntity.getId());
        if (optionalSource.isPresent()) {
            AssetOwner source = optionalSource.get();
            int compareResult = source.getAmount().compareTo(amount);
            if (compareResult < 0) {
                logInvalidState(String.format("Transferred amount [%d] > owned amount. %s", amount, source));
                assetOwnersRepository.delete(source);
                log.info("Deleted {}", source);
            } else if (compareResult == 0) {
                assetOwnersRepository.delete(source);
                log.info("All assets transferred. Deleted {}", source);
            } else {
                source.setAmount(source.getAmount().subtract(amount));
                assetOwnersRepository.save(source);
                log.info("Removed [{}] shares: {}", amount, source);
            }
        } else {
            logInvalidState(String.format("Source %s owns no asset %s.", sourceEntity, asset));
        }
    }

    private void logInvalidState(String message) {
        log.error("Invalid asset owner state. Fix by importing universe file. Details: {}", message);
    }

    private void validate(String from, String to, String issuer, String assetName, BigInteger amount) {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (from != null) {
            validationUtility.validateIdentity(from).map(validationErrors::add);
        } else {
            log.info("Not validating null 'from' identity.");
        }
        validationUtility.validateIdentity(to).map(validationErrors::add);
        validationUtility.validateIdentity(issuer).map(validationErrors::add);
        validationUtility.validateAssetName(assetName).map(validationErrors::add);
        validationUtility.validateAmount(amount).map(validationErrors::add);
        if (!validationErrors.isEmpty()) {
            String message = String.format("Validation error trying to transfer asset [%s/%s] from [%s] to [%s]: %s",
                    issuer, assetName, from, to,
                    validationErrors.stream()
                            .map(ValidationError::message)
                            .collect(Collectors.joining(",", "[", "]")));
            throw new TransferAssetException(message);
        }
    }

}
