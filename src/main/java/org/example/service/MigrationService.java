package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.manager.MigrationManager;

@RequiredArgsConstructor
public class MigrationService {
    private final MigrationManager migrationManager;

    public void runMigrations(){
        migrationManager.createTablesIfNotExist();
    }
}
