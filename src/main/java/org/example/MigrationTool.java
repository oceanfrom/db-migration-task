package org.example;

import lombok.RequiredArgsConstructor;
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
        MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
        MigrationManager migrationManager = new MigrationManager(migrationTableManager);
        MigrationService migrationService = new MigrationService(migrationManager, connectionUtils);
        MigrationTool tool = new MigrationTool(migrationService);

        tool.runMigrations();
    }
}
