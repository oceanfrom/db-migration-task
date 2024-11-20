package org.example;

import lombok.RequiredArgsConstructor;
import org.example.manager.AppliedMigrationManager;
import org.example.manager.MigrationHistoryManager;
import org.example.manager.MigrationManager;
import org.example.manager.MigrationTableManager;
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
        MigrationManager migrationManager = new MigrationManager(migrationTableManager, appliedMigrationManager, migrationHistoryManager);
        MigrationService migrationService = new MigrationService(migrationManager, connectionUtils);
        MigrationTool tool = new MigrationTool(migrationService);

        tool.runMigrations();
    }
}
