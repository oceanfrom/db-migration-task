package org.example.manager;

import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class MigrationManager {
    private final MigrationTableManager migrationTableManager;
    private final MigrationTrackerManager appliedMigrationManager;
    private final MigrationLockManager migrationLockManager;

    public void createTablesIfNotExist() {
        migrationTableManager.createTablesIfNotExist();
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
