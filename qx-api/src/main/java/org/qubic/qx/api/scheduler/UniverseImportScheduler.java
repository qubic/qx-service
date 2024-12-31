package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.richlist.UniverseCsvImporter;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class UniverseImportScheduler {

    private static final BiPredicate<Path, BasicFileAttributes> UNIVERSE_FILE_PATTERN =
            (path, basicFileAttributes) -> StringUtils.startsWith(path.getFileName().toString(), "universe")
                    && StringUtils.endsWith(path.getFileName().toString(), ".csv");
    private static final String IMPORT_DIR_NAME = "import";
    private static final String IMPORTED_DIR_NAME = "imported";


    private final UniverseCsvImporter csvImporter;

    public UniverseImportScheduler(UniverseCsvImporter csvImporter) throws IOException {
        this.csvImporter = csvImporter;
        Path importDir = Files.createDirectories(Path.of(IMPORT_DIR_NAME));
        log.info("Import directory: {}", importDir);
        Path importedDir = Files.createDirectories(Path.of(IMPORTED_DIR_NAME));
        log.info("Imported directory: {}", importedDir);
    }

    @Scheduled(cron = "${scheduler.import.universe.cron}")
    void importUniverseFile() throws IOException {

        Optional<Path> optionalPath;
        try (Stream<Path> paths = Files.find(Path.of(IMPORT_DIR_NAME), 1, UNIVERSE_FILE_PATTERN)) {
            optionalPath = paths.findAny();
        }
        if (optionalPath.isPresent()) {
            Path importFilePath = optionalPath.get();
            log.info("Importing universe file [{}].", importFilePath);
            try (Reader reader = Files.newBufferedReader(importFilePath, StandardCharsets.US_ASCII)) {
                List<AssetOwner> assetOwners = csvImporter.importAssetOwners(reader);
                log.info("Imported [{}] asset owners.", assetOwners.size());
                Path moved = Files.move(importFilePath, Path.of(IMPORTED_DIR_NAME).resolve(String.format("%s.%d", importFilePath.getFileName(), System.currentTimeMillis())), REPLACE_EXISTING);
                log.info("Moved imported file to [{}].", moved);
            }
        } else {
            log.debug("No universe file to import.");
        }

    }

}
