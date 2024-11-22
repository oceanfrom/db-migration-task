package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.logger.MigrationLogger;
import org.example.manager.MigrationTrackerManager;
import org.example.manager.MigrationLockManager;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationFileReader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MigrationRollbackService {

    private final ConnectionUtils connectionUtils;
    private final MigrationTrackerManager appliedMigrationsManager;
    private final MigrationLockManager migrationLockManager;
    private final MigrationFileReader migrationFileReader;
    private static final String ROLLBACK_DIR = "src/main/resources/db.changelog/rollback/";


    private interface MigrationSelector {
        List<String> selectMigrations() throws SQLException, IOException;
    }

    private void executeRollback(MigrationSelector migrationSelector) {
        MigrationLogger.logInfo("Migration rollback process starts");

        if (!migrationLockManager.acquireLock()) {
            throw new IllegalStateException("Failed to acquire lock for migration rollback");
        }

        List<String> successfullyRolledBackMigrations = new ArrayList<>();
        try (Connection connection = connectionUtils.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<String> migrationsToRollback = migrationSelector.selectMigrations();
                for (String migration : migrationsToRollback) {
                    processRollback(connection, migration, successfullyRolledBackMigrations);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                undoRolledBackMigrations(successfullyRolledBackMigrations);
                MigrationLogger.logError("Rollback failed", e);
                throw new RuntimeException(e);
            } finally {
                migrationLockManager.releaseLock();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Database error during rollback", e);
        }
    }

    public void rollbackMigrationToVersion(String migrationName) {
        executeRollback(() -> {
            List<String> allMigrations = MigrationFileReader.getMigrationNames();
            int endIdx = allMigrations.indexOf(migrationName);
            if (endIdx == -1) {
                throw new IllegalArgumentException("Migration " + migrationName + " not found");
            }
            MigrationLogger.logInfo("Starting rollback to version: " + migrationName);
            return allMigrations.subList(endIdx + 1, allMigrations.size());
        });
    }

    public void rollbackMigrationCount(int count) {
        executeRollback(() -> {
            List<String> allMigrations = MigrationFileReader.getMigrationNames();

            int lastRolledBackIdx = -1;
            for (int i = 0; i < allMigrations.size(); i++) {
                if (appliedMigrationsManager.isMigrationRolledBack(allMigrations.get(i))) {
                    lastRolledBackIdx = i-1;
                    break;
                }
            }
            if (lastRolledBackIdx == -1)
                lastRolledBackIdx = allMigrations.size() - 1;

            int startIdx = lastRolledBackIdx;
            if (startIdx < 0)
                startIdx = 0;

            List<String> migrationsToRollback = new ArrayList<>();
            int rollbackCount = 0;
            for (int i = startIdx; i >= 0 && rollbackCount < count; i--) {
                migrationsToRollback.add(allMigrations.get(i));
                rollbackCount++;
            }
            return migrationsToRollback;
        });
    }

    public void rollbackMigrationByDate(String rollbackDate) {
        executeRollback(() -> {
            MigrationLogger.logInfo("Starting rollback by date: " + rollbackDate);
            return migrationFileReader.getMigrationsBeforeDate(rollbackDate);
        });
    }


    private void processRollback(Connection connection, String migration, List<String> successfullyRolledBackMigrations) throws SQLException {
        if (appliedMigrationsManager.isMigrationRolledBack(migration)) {
            MigrationLogger.logInfo("Migration " + migration + " has already been rolled back, skipping...");
            return;
        }

        String rollbackFileName = migration.replace(".sql", "-rollback.sql");
        File rollbackFile = new File(ROLLBACK_DIR + rollbackFileName);

        if (!rollbackFile.exists()) {
            MigrationLogger.logInfo("Rollback file does not exist for: " + migration);
            return;
        }

        MigrationLogger.logInfo("Rolling back migration: " + migration);
        MigrationExecutor.executeMigration(rollbackFile, connection);
        appliedMigrationsManager.markMigrationAsRolledBack(migration);
        successfullyRolledBackMigrations.add(migration);
        MigrationLogger.logInfo("Migration " + migration + " rolled back successfully");
    }

    private void undoRolledBackMigrations(List<String> rolledBackMigrations) {
        for (String migration : rolledBackMigrations) {
            appliedMigrationsManager.unmarkMigrationAsRolledBack(migration);
            MigrationLogger.logInfo("Undo rollback for migration: " + migration);
        }
    }

}
