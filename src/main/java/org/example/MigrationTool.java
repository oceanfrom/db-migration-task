package org.example;

import lombok.RequiredArgsConstructor;
import org.example.logger.MigrationLogger;
import org.example.manager.*;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.ConnectionUtils;

@RequiredArgsConstructor
public class MigrationTool {
    private final MigrationService migrationService;
    private final MigrationRollbackService migrationRollbackService;

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
        ConnectionUtils connectionUtils = new ConnectionUtils();
        AppliedMigrationManager appliedMigrationManager = new AppliedMigrationManager(connectionUtils);
        MigrationHistoryManager migrationHistoryManager = new MigrationHistoryManager(connectionUtils);
        MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
        MigrationLockManager migrationLockManager = new MigrationLockManager(connectionUtils);
        MigrationManager migrationManager = new MigrationManager(migrationTableManager, appliedMigrationManager, migrationHistoryManager, migrationLockManager);
        MigrationLogger migrationLogger = new MigrationLogger();
        MigrationService migrationService = new MigrationService(migrationManager, connectionUtils);
        MigrationRollbackService migrationRollbackService1 = new MigrationRollbackService(connectionUtils, migrationLogger, appliedMigrationManager, migrationLockManager);
        MigrationTool tool = new MigrationTool(migrationService, migrationRollbackService1);

       // tool.runMigrations();
       tool.rollbackMigrationToVersion("");
       // MigrationStatus.info();
    }
}
