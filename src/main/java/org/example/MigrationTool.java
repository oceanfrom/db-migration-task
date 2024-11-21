package org.example;

import lombok.RequiredArgsConstructor;
import org.example.dependencies.DependencyFactory;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.MigrationStatus;


@RequiredArgsConstructor
public class MigrationTool {
    private final MigrationService migrationService;
    private final MigrationRollbackService migrationRollbackService;
    private final MigrationStatus migrationStatus;

    public void runMigrations() {
        migrationService.runMigrations();
    }

    public void rollbackMigrationToVersion(String migrationName) {
        migrationRollbackService.rollbackMigrationToVersion(migrationName);
    }

    public void rollbackMigrationCount(int i) {
        migrationRollbackService.rollbackMigrationCount(i);
    }

    public void rollbackMigrationByDate(String migrationTime) {
        migrationRollbackService.rollbackMigrationByDate(migrationTime);
    }

    public static void main(String[] args) {
        MigrationTool tool = new MigrationTool(
                DependencyFactory.getMigrationService(),
                DependencyFactory.getMigrationRollbackService(),
                DependencyFactory.getMigrationStatus()
        );

        tool.runMigrations();
        tool.rollbackMigrationToVersion("0002-insert.sql");
        tool.migrationStatus.info();

    }
}
