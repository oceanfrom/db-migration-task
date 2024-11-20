package org.example;

import lombok.RequiredArgsConstructor;
import org.example.manager.*;
import org.example.service.MigrationService;
import org.example.utils.ConnectionUtils;

@RequiredArgsConstructor
public class MigrationTool {
    private final MigrationService migrationService;

    public void runMigrations() {
        migrationService.runMigrations();
    }

    public static void main(String[] args) {
        ConnectionUtils connectionUtils = new ConnectionUtils();
        AppliedMigrationManager appliedMigrationManager = new AppliedMigrationManager(connectionUtils);
        MigrationHistoryManager migrationHistoryManager = new MigrationHistoryManager(connectionUtils);
        MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
        MigrationLockManager migrationLockManager = new MigrationLockManager(connectionUtils);
        MigrationManager migrationManager = new MigrationManager(migrationTableManager, appliedMigrationManager, migrationHistoryManager, migrationLockManager);
        MigrationService migrationService = new MigrationService(migrationManager, connectionUtils);
        MigrationTool tool = new MigrationTool(migrationService);

        tool.runMigrations();
    }
}
