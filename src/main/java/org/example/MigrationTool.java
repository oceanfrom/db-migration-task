package org.example;

import lombok.RequiredArgsConstructor;
import org.example.manager.MigrationManager;
import org.example.manager.MigrationTableManager;
import org.example.utils.ConnectionUtils;

@RequiredArgsConstructor
public class MigrationTool {
    private final MigrationManager migrationManager;

    public void runMigrations() {
        migrationManager.createTablesIfNotExist();
    }

    public static void main(String[] args) {
        ConnectionUtils connectionUtils = new ConnectionUtils();
        MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
        MigrationManager migrationManager = new MigrationManager(migrationTableManager);
        MigrationTool tool = new MigrationTool(migrationManager);

        tool.runMigrations();
    }
}
