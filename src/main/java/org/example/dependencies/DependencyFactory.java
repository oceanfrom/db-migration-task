package org.example.dependencies;

import lombok.Getter;
import org.example.manager.*;
import org.example.report.MigrationReport;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationFileReader;
import org.example.utils.MigrationStatus;

public class DependencyFactory {
    private static final ConnectionUtils connectionUtils = new ConnectionUtils();
    private static final MigrationTrackerManager appliedMigrationManager = new MigrationTrackerManager(connectionUtils);
    private static final MigrationTableManager migrationTableManager = new MigrationTableManager(connectionUtils);
    private static final MigrationLockManager migrationLockManager = new MigrationLockManager(connectionUtils);
    private static final MigrationManager migrationManager =
            new MigrationManager(migrationTableManager, appliedMigrationManager, migrationLockManager);
    private static final MigrationReport migrationReport = new MigrationReport();
    @Getter
    private static final MigrationService migrationService =
            new MigrationService(migrationManager, connectionUtils, migrationReport);
    @Getter
    private static final MigrationRollbackService migrationRollbackService =
            new MigrationRollbackService(connectionUtils, appliedMigrationManager, migrationLockManager, new MigrationFileReader(connectionUtils));
    @Getter
    private static final MigrationStatus migrationStatus = new MigrationStatus(migrationLockManager);

}
