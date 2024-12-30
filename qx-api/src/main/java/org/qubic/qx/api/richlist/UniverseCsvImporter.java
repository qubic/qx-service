package org.qubic.qx.api.richlist;

import at.qubic.api.crypto.IdentityUtil;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.databind.util.LookupCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.EntitiesRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.richlist.exception.CsvImportException;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class UniverseCsvImporter {

    private final IdentityUtil identityUtil;
    private final EntitiesRepository entitiesRepository;
    private final AssetsRepository assetsRepository;
    private final AssetOwnersRepository assetOwnersRepository;
    private final LookupCache<String, Long> assetsCache = new LRUMap<>(100, 100);

    public UniverseCsvImporter(IdentityUtil identityUtil, EntitiesRepository entitiesRepository, AssetsRepository assetsRepository, AssetOwnersRepository assetOwnersRepository) {
        this.identityUtil = identityUtil;
        this.entitiesRepository = entitiesRepository;
        this.assetsRepository = assetsRepository;
        this.assetOwnersRepository = assetOwnersRepository;
    }

    public List<AssetOwner> importAssetOwners(Reader input) throws IOException {
        CSVParser parser = readCsvFile(input);
        List<AssetOwner> assetOwners = parseCsvFile(parser);
        log.info("Parsed [{}] asset owners.", assetOwners.size());

        if (!assetOwners.isEmpty()) {
            updateDatabase(assetOwners);
        } else {
            log.warn("No asset owners in import file. Not updating database.");
        }

        return assetOwners;
    }

    private void updateDatabase(List<AssetOwner> assetOwners) {
        assetOwnersRepository.deleteAll();
        log.info("Deleted old asset owner records.");

        assetOwnersRepository.saveAll(assetOwners);
        log.info("Saved new asset owner records.");
    }

    private List<AssetOwner> parseCsvFile(CSVParser parser) {
        List<AssetOwner> assetOwners = new ArrayList<>();

        for (CSVRecord csvRecord : parser) {
            if (StringUtils.equalsIgnoreCase("ownership", csvRecord.get("Type"))) {

                List<ValidationError> validationErrors = new ArrayList<>();
                long recordNumber = csvRecord.getRecordNumber();

                String identity = csvRecord.get("ID"); // validate
                validateIdentity(identity, recordNumber).map(validationErrors::add);

                String assetName = csvRecord.get("AssetName");
                validateAssetName(assetName, recordNumber).map(validationErrors::add);

                String assetIssuer = csvRecord.get("AssetIssuer");
                validateIdentity(assetIssuer, recordNumber).map(validationErrors::add);

                BigInteger amount = new BigInteger(csvRecord.get("Amount"));
                validateAmount(amount, recordNumber).map(validationErrors::add);

                if (!validationErrors.isEmpty()) {
                    throw new CsvImportException("Validation failed: " +  validationErrors.stream().map(err -> err.message)
                            .collect(Collectors.joining(",", "[", "]")));
                }

                Entity entity = getOrCreateEntity(identity);
                long assetId = getOrCreateAssetId(assetIssuer, assetName);
                AssetOwner assetOwner = AssetOwner.builder()
                        .assetId(assetId)
                        .entityId(entity.getId())
                        .amount(amount)
                        .build();

                assetOwners.add(assetOwner);
                log.debug("Saved asset owner: {}", assetOwner);

            } else {

                log.debug("Skipping csv record: {}", csvRecord);

            }
        }
        return assetOwners;
    }

    private long getOrCreateAssetId(String assetIssuer, String assetName) {
        String assetCacheKeyFormat = "%s/%s";
        Long assetId = assetsCache.get(String.format(assetCacheKeyFormat, assetIssuer, assetName));
        if (assetId == null) {
            Long id = assetsRepository.findByIssuerAndName(assetIssuer, assetName)
                    .orElseGet(() -> {
                        log.info("Creating asset with issuer [{}] and name [{}].", assetIssuer, assetName);
                        return assetsRepository.save(Asset.builder().issuer(assetIssuer).name(assetName).build());
                    }).getId();
            assetsCache.put(String.format(assetCacheKeyFormat, assetIssuer, assetName), id);
            return id;
        } else {
            return assetId;
        }
    }

    private Entity getOrCreateEntity(String identity) {
        return entitiesRepository.findByIdentity(identity)
                .orElseGet(() -> {
                    log.info("Creating entity with identity [{}].", identity);
                    return entitiesRepository.save(Entity.builder().identity(identity).build());
                });
    }

    private Optional<ValidationError> validateAmount(BigInteger amount, long recordNumber) {
        ValidationError error = null;
        if (amount.signum() <= 0) {
            String message = String.format("Csv record [%s]: invalid amount [%d].", recordNumber, amount);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }

    private Optional<ValidationError> validateIdentity(String identity, long recordNumber) {
        ValidationError error = null;
        if (!identityUtil.isValidIdentity(identity)) {
            String message = String.format("Csv record [%s]: invalid identity [%s]", recordNumber, identity);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }

    private Optional<ValidationError> validateAssetName(String name, long recordNumber) {
        ValidationError error = null;

        if (StringUtils.isBlank(name) || StringUtils.length(name) > 7) {
            String message = String.format("Csv record [%s]: invalid asset name [%s]", recordNumber, name);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }

    private static CSVParser readCsvFile(Reader input) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setIgnoreSurroundingSpaces(true)
                .build();
        return csvFormat.parse(input);
    }

    private record ValidationError(String message) { }

}
