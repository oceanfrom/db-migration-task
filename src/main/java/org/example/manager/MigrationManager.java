package org.example.manager;

import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class MigrationManager {
    private final MigrationTableManager migrationTableManager;
    private final AppliedMigrationManager appliedMigrationManager;
    private final MigrationHistoryManager migrationHistoryManager;
    private final MigrationLockManager migrationLockManager;

    public void createTablesIfNotExist() {
        migrationTableManager.createTablesIfNotExist();
    }

    public void logMigrationHistory(String migrationName, String status, String errorMessage) {
        migrationHistoryManager.logMigrationHistory(migrationName, status, errorMessage);
    }

    public void markMigrationAsAppleid(File migrationFile, boolean success, String message) {
        appliedMigrationManager.markMigrationAsApplied(migrationFile, success, message);
    }

    public boolean isMigrationApplied(File migrationFile) {
        return appliedMigrationManager.isMigrationApplied(migrationFile);
    }

    public boolean acquireLock() {
        return migrationLockManager.acquireLock();
    }

    public void releaseLock() {
        migrationLockManager.releaseLock();
    }
}
