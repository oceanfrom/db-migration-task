package org.example.dependencies;

import org.example.logger.MigrationLogger;
import org.example.manager.*;
import org.example.report.MigrationReport;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationStatus;

public class DependencyFactory {
    private static final ConnectionUtils connectionUtils = new ConnectionUtils();
    private static final AppliedMigrationManager appliedMigrationManager = new AppliedMigrationManager(connectionUtils);
    private static final MigrationHistoryManager migrationHistoryManager = new MigrationHistoryManager(connectionUtils);
    private static final MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
    private static final MigrationLockManager migrationLockManager = new MigrationLockManager(connectionUtils);
    private static final MigrationManager migrationManager =
            new MigrationManager(migrationTableManager, appliedMigrationManager, migrationHistoryManager, migrationLockManager);
    private static final MigrationLogger migrationLogger = new MigrationLogger();
    private static final MigrationReport migrationReport = new MigrationReport();
    private static final MigrationService migrationService =
            new MigrationService(migrationManager, connectionUtils, migrationReport);
    private static final MigrationRollbackService migrationRollbackService =
            new MigrationRollbackService(connectionUtils, migrationLogger, appliedMigrationManager, migrationLockManager);
    private static final MigrationStatus migrationStatus = new MigrationStatus(migrationLockManager);

    public static MigrationService getMigrationService() {
        return migrationService;
    }

    public static MigrationRollbackService getMigrationRollbackService() {
        return migrationRollbackService;
    }

    public static MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }
}
