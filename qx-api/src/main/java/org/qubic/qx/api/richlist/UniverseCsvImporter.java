package org.qubic.qx.api.richlist;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.AssetsDbService;
import org.qubic.qx.api.db.EntitiesDbService;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.richlist.exception.CsvImportException;
import org.qubic.qx.api.validation.ValidationError;
import org.qubic.qx.api.validation.ValidationUtility;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UniverseCsvImporter {

    private final ValidationUtility validationUtility;
    private final EntitiesDbService entitiesDbService;
    private final AssetsDbService assetsDbService;
    private final AssetOwnersRepository assetOwnersRepository;

    public UniverseCsvImporter(ValidationUtility validationUtility, EntitiesDbService entitiesDbService, AssetsDbService assetsDbService, AssetOwnersRepository assetOwnersRepository) {
        this.validationUtility = validationUtility;
        this.entitiesDbService = entitiesDbService;
        this.assetsDbService = assetsDbService;
        this.assetOwnersRepository = assetOwnersRepository;
    }

    public List<AssetOwner> importAssetOwners(Reader input) throws IOException {
        CSVParser parser = readCsvFile(input);
        // at the moment we hold and return all objects in memory. Consider more memory efficient implementation in case
        // file gets too large.
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

        // if we want to make the db completely clean we could delete unused entities here
        // these entities can result from asset ownership changes on import if transaction history
        // is not complete. But it's not necessary to handle this atm.
    }

    private List<AssetOwner> parseCsvFile(CSVParser parser) {
        List<AssetOwner> assetOwners = new ArrayList<>();

        for (CSVRecord csvRecord : parser) {
            if (StringUtils.equalsIgnoreCase("ownership", csvRecord.get("Type"))) {

                List<ValidationError> validationErrors = new ArrayList<>();
                long recordNumber = csvRecord.getRecordNumber();

                String identity = csvRecord.get("ID"); // validate
                validationUtility.validateIdentity(identity).map(validationErrors::add);

                String assetName = csvRecord.get("AssetName");
                validationUtility.validateAssetName(assetName).map(validationErrors::add);

                String assetIssuer = csvRecord.get("AssetIssuer");
                validationUtility.validateIdentity(assetIssuer).map(validationErrors::add);

                BigInteger amount = new BigInteger(csvRecord.get("Amount"));
                validationUtility.validateAmount(amount).map(validationErrors::add);

                if (!validationErrors.isEmpty()) {
                    String message = String.format("Csv import: Invalid record [%d]: %s", recordNumber,
                            validationErrors.stream()
                                    .map(ValidationError::message)
                                    .collect(Collectors.joining(",", "[", "]")));
                    throw new CsvImportException(message);
                }

                Entity entity = entitiesDbService.getOrCreateEntity(identity);
                long assetId = assetsDbService.getOrCreateAsset(assetIssuer, assetName).getId();
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

    private static CSVParser readCsvFile(Reader input) throws IOException {
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setIgnoreSurroundingSpaces(true)
                .build();
        return csvFormat.parse(input);
    }

}
