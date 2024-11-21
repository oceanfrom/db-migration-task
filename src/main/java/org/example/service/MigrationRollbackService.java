package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.logger.MigrationLogger;
import org.example.manager.AppliedMigrationManager;
import org.example.manager.MigrationLockManager;
import org.example.utils.ConnectionUtils;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@RequiredArgsConstructor
public class MigrationRollbackService {

    private final ConnectionUtils connectionManager;
    private final MigrationLogger migrationLogger;
    private final AppliedMigrationManager appliedMigrationsManager;
    private final MigrationLockManager migrationLockManager;

    public void rollbackMigrationToVersion(String migrationName) {
        MigrationLogger.logInfo("Migration rollback starts");

        if (!migrationLockManager.acquireLock()) {
            throw new RuntimeException("Failed to acquire lock for migration rollback");
        }

        List<String> successfullyRolledBackMigrations = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {

                List<String> allMigrations = getAllMigrations();
                int endIdx = allMigrations.indexOf(migrationName);
                if (endIdx == -1) {
                    throw new RuntimeException("Migration " + migrationName + " not found");
                }

                MigrationLogger.logInfo("Migration found for rollback");

                for (int i = allMigrations.size() - 1; i >= endIdx + 1; i--) {
                    String currentMigration = allMigrations.get(i);
                    String rollbackFileName = currentMigration.replace(".sql", "-rollback.sql");
                    File rollbackFile = new File("src/main/resources/db.changelog/rollback/" + rollbackFileName);

                    if (rollbackFile.exists()) {
                        if(appliedMigrationsManager.isMigrationRolledBack(currentMigration)) {
                            MigrationLogger.logInfo("Migration " + currentMigration + " has already been rolled back, skipping...");
                        } else {
                            MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);
                            MigrationLogger.logInfo("Rollback starts for: " + currentMigration);
                            MigrationExecutor.executeMigration(rollbackFile, connection);
                            appliedMigrationsManager.markMigrationAsRolledBack(currentMigration);
                            successfullyRolledBackMigrations.add(currentMigration);
                            MigrationLogger.logInfo("Migration rolled back successfully");
                        }
                    } else {
                        MigrationLogger.logInfo("Rollback file does not exist for: " + currentMigration);
                    }
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                for (String migration : successfullyRolledBackMigrations) {
                    appliedMigrationsManager.unmarkMigrationAsRolledBack(migration);
                    MigrationLogger.logInfo("Rollback of migration " + migration + " is undone due to failure");
                }
                MigrationLogger.logError("Migration rollback failed", e);
            } finally {
                connection.setAutoCommit(true);
                migrationLockManager.releaseLock();
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Error during rollback transaction", e);
        }
    }

    public void rollbackMigrationCount(int count) {
        MigrationLogger.logInfo("Migration rollback starts");
        if (!migrationLockManager.acquireLock()) {
            throw new RuntimeException("Failed to acquire lock for migration rollback");
        }
        List<String> successfullyRolledBackMigrations = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<String> allMigrations = getAllMigrations();

                int lastRolledBackIdx = -1;
                for (int i = 0; i < allMigrations.size(); i++) {
                    String currentMigration = allMigrations.get(i);
                    if (appliedMigrationsManager.isMigrationRolledBack(currentMigration)) {
                        lastRolledBackIdx = i;
                        break;
                    }
                }

                if (lastRolledBackIdx == -1) {
                    lastRolledBackIdx = allMigrations.size() - 1;
                }
                MigrationLogger.logInfo("Last rolled back migration index: " + lastRolledBackIdx);

                int startIdx = lastRolledBackIdx;
                if (startIdx < 0) startIdx = 0;

                MigrationLogger.logInfo("Rollback will start from index: " + startIdx);

                int rollbackCount = 0;

                for (int i = startIdx; i >= 0 && rollbackCount < count; i--) {
                    String currentMigration = allMigrations.get(i);

                    if (appliedMigrationsManager.isMigrationRolledBack(currentMigration)) {
                        MigrationLogger.logInfo("Migration " + currentMigration + " has already been rolled back, skipping...");
                        continue;
                    }
                    String rollbackFileName = currentMigration.replace(".sql", "-rollback.sql");
                    File rollbackFile = new File("src/main/resources/db.changelog/rollback/" + rollbackFileName);

                    if (rollbackFile.exists()) {
                        MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);

                        MigrationLogger.logInfo("Rollback starts for: " + currentMigration);
                        MigrationExecutor.executeMigration(rollbackFile, connection);
                        appliedMigrationsManager.markMigrationAsRolledBack(currentMigration);
                        successfullyRolledBackMigrations.add(currentMigration);
                        MigrationLogger.logInfo("Migration rolled back successfully");

                        rollbackCount++;
                    } else {
                        MigrationLogger.logInfo("Rollback file does not exist for: " + currentMigration);
                    }
                }

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                for (String migration : successfullyRolledBackMigrations) {
                    appliedMigrationsManager.unmarkMigrationAsRolledBack(migration);
                    MigrationLogger.logInfo("Rollback of migration " + migration + " is undone due to failure");
                }
                MigrationLogger.logError("Migration rollback failed", e);
            } finally {
                connection.setAutoCommit(true);
                migrationLockManager.releaseLock();
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Error during rollback transaction", e);
        }
    }

    public void rollbackMigrationByDate(String rollbackDate) {
        MigrationLogger.logInfo("Migration rollback by date starts");

        if (!migrationLockManager.acquireLock()) {
            throw new RuntimeException("Failed to acquire lock for migration rollback");
        }

        List<String> successfullyRolledBackMigrations = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                List<String> migrationsToRollback = getMigrationsBeforeDate(rollbackDate);

                for (String migration : migrationsToRollback) {
                    String rollbackFileName = migration.replace(".sql", "-rollback.sql");
                    File rollbackFile = new File("src/main/resources/db.changelog/rollback/" + rollbackFileName);

                    if (rollbackFile.exists()) {
                        MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);

                        if(appliedMigrationsManager.isMigrationRolledBack(migration)) {
                            MigrationLogger.logInfo("Migration " + migration + " has already been rolled back");
                        } else {
                            MigrationLogger.logInfo("Rollback starts for: " + migration);
                            MigrationExecutor.executeMigration(rollbackFile, connection);
                            appliedMigrationsManager.markMigrationAsRolledBack(migration);
                            successfullyRolledBackMigrations.add(migration);
                            MigrationLogger.logInfo("Migration " + migration + " rolled back successfully");
                        }
                    } else {
                        MigrationLogger.logInfo("Rollback file does not exist for: " + migration);
                    }
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                for (String migration : successfullyRolledBackMigrations) {
                    appliedMigrationsManager.unmarkMigrationAsRolledBack(migration);
                    MigrationLogger.logInfo("Rollback of migration " + migration + " is undone due to failure");
                }
                MigrationLogger.logError("Migration rollback by date failed", e);
            } finally {
                connection.setAutoCommit(true);
                migrationLockManager.releaseLock();
            }
        } catch (SQLException e) {
            MigrationLogger.logError("Error during rollback transaction", e);
        }
    }

    private List<String> getMigrationsBeforeDate(String rollbackDate) throws SQLException {
        List<String> migrations = new ArrayList<>();

        String query = "SELECT migration_name FROM applied_migrations WHERE applied_at > ? ORDER BY applied_at DESC";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.valueOf(rollbackDate));
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                migrations.add(resultSet.getString("migration_name"));
            }
        }

        return migrations;
    }

    private List<String> getAllMigrations() {
        File migrationsDir = new File("src/main/resources/db.changelog/versions");
        File[] migrationFiles = migrationsDir.listFiles((dir, name) -> name.endsWith(".sql"));

        if (migrationFiles == null) {
            return Collections.emptyList();
        }

        List<String> migrations = new ArrayList<>();
        for (File migrationFile : migrationFiles) {
            migrations.add(migrationFile.getName());
        }

        return migrations;
    }
}
