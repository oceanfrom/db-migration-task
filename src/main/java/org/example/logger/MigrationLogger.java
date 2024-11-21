package org.example.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MigrationLogger {
    private static final Logger logger = LogManager.getLogger(MigrationLogger.class);

    public static void logMigrationStart(String migrationName) {
        logger.info("The beginning of the migration: {}", migrationName);
    }

    public static void logMigrationError(String migrationName, Exception e) {
        logger.error("Migration execution error: {}. Details: {}", migrationName, e.getMessage(), e);
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message, Exception e) {
        logger.error(message, e);
    }
}
