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

        try {
            if (appliedMigrationsManager.isMigrationRolledBack(migrationName)) {
                throw new RuntimeException("Migration " + migrationName + " has already been rolled back");
            }

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
                    MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);

                    MigrationLogger.logInfo("Rollback starts for: " + currentMigration);
                    MigrationExecutor.executeMigration(rollbackFile, connectionManager.getConnection());
                    appliedMigrationsManager.markMigrationAsRolledBack(currentMigration);
                    MigrationLogger.logInfo("Migration rolled back successfully");
                } else {
                    MigrationLogger.logInfo("Rollback file does not exist for: " + currentMigration);
                }
            }

        } catch (Exception e) {
            MigrationLogger.logError("Migration rollback failed", e);
        } finally {
            migrationLockManager.releaseLock();
        }
    }

    public void rollbackMigrationCount(int count) {
        MigrationLogger.logInfo("Migration rollback starts");

        if (!migrationLockManager.acquireLock()) {
            throw new RuntimeException("Failed to acquire lock for migration rollback");
        }

        try {
            List<String> allMigrations = getAllMigrations();

            for (int i = allMigrations.size() - 1; i >= allMigrations.size() - count; i--) {
                String currentMigration = allMigrations.get(i);

                String rollbackFileName = currentMigration.replace(".sql", "-rollback.sql");
                File rollbackFile = new File("src/main/resources/db.changelog/rollback/" + rollbackFileName);

                if (rollbackFile.exists()) {
                    MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);

                    MigrationLogger.logInfo("Rollback starts for: " + currentMigration);
                    MigrationExecutor.executeMigration(rollbackFile, connectionManager.getConnection());
                    appliedMigrationsManager.markMigrationAsRolledBack(currentMigration);
                    MigrationLogger.logInfo("Migration rolled back successfully");
                } else {
                    MigrationLogger.logInfo("Rollback file does not exist for: " + currentMigration);
                }
            }

        } catch (Exception e) {
            MigrationLogger.logError("Migration rollback failed", e);
        } finally {
            migrationLockManager.releaseLock();
        }
    }

    public void rollbackMigrationByDate(String rollbackDate) {
        MigrationLogger.logInfo("Migration rollback by date starts");

        if (!migrationLockManager.acquireLock()) {
            throw new RuntimeException("Failed to acquire lock for migration rollback");
        }

        try {
            List<String> migrationsToRollback = getMigrationsBeforeDate(rollbackDate);

            for (String migration : migrationsToRollback) {
                String rollbackFileName = migration.replace(".sql", "-rollback.sql");
                File rollbackFile = new File("src/main/resources/db.changelog/rollback/" + rollbackFileName);

                if (rollbackFile.exists()) {
                    MigrationLogger.logInfo("Rollback file found: " + rollbackFileName);

                    MigrationLogger.logInfo("Rollback starts for: " + migration);
                    MigrationExecutor.executeMigration(rollbackFile, connectionManager.getConnection());
                    appliedMigrationsManager.markMigrationAsRolledBack(migration);
                    MigrationLogger.logInfo("Migration " + migration + " rolled back successfully");
                } else {
                    MigrationLogger.logInfo("Rollback file does not exist for: " + migration);
                }
            }

        } catch (Exception e) {
            MigrationLogger.logError("Migration rollback by date failed", e);
        } finally {
            migrationLockManager.releaseLock();
        }
    }

    private List<String> getMigrationsBeforeDate(String rollbackDate) throws SQLException {
        List<String> migrations = new ArrayList<>();

        String query = "SELECT migration_name FROM applied_migrations WHERE applied_at > ? ORDER BY applied_at DESC";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setTimestamp(1, Timestamp.valueOf(rollbackDate));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                migrations.add(rs.getString("migration_name"));
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
