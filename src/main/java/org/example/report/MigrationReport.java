package org.example.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logger.MigrationLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MigrationReport {
    private final List<MigrationResult> migrationResults = new ArrayList<>();

    public void addMigrationResult(String migrationName, boolean success, String message) {
        migrationResults.add(new MigrationResult(migrationName, success, message));
        MigrationLogger.logInfo("Added migration result: " + migrationName + " - " + message);
    }

    public void generateJSONReport(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        MigrationLogger.logInfo("Generating JSON report to: " + filePath);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), migrationResults);
    }
}
