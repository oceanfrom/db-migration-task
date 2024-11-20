package org.example.manager;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MigrationManager {
    private final MigrationTableManager migrationTableManager;

    public void createTablesIfNotExist() {
        migrationTableManager.createTablesIfNotExist();
    }
}
