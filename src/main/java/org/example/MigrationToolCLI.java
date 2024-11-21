package org.example;

import org.example.dependencies.DependencyFactory;
import org.example.logger.MigrationLogger;
import org.example.service.MigrationRollbackService;
import org.example.service.MigrationService;
import org.example.utils.MigrationStatus;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "migration-tool",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Tool for managing database migrations."
)
public class MigrationToolCLI implements Runnable {

    @Option(names = "--command", required = true, description = "Command to execute: migrate, rollback, status")
    private String command;

    @Option(names = "--migration", description = "Migration name (required for rollback)")
    private String migrationName;

    @Option(names = "--count", description = "Number of migrations to rollback (required for count-based rollback)")
    private Integer count;

    @Option(names = "--date", description = "Date to rollback migrations by (format: yyyy-MM-dd HH:mm:ss.SSSSSS) (required for date-based rollback)")
    private String rollbackDate;

    private final MigrationService migrationService;
    private final MigrationRollbackService migrationRollbackService;
    private final MigrationStatus migrationStatus;

    public MigrationToolCLI(MigrationService migrationService, MigrationRollbackService migrationRollbackService, MigrationStatus migrationStatus) {
        this.migrationService = migrationService;
        this.migrationRollbackService = migrationRollbackService;
        this.migrationStatus = migrationStatus;
    }

    @Override
    public void run() {
        switch (command) {
            case "migrate":
                migrationService.runMigrations();
                MigrationLogger.logInfo("All migrations applied successfully.");
                break;

            case "rollback":
                if (migrationName != null) {
                    migrationRollbackService.rollbackMigrationToVersion(migrationName);
                    MigrationLogger.logInfo("Rollback to version " + migrationName + " completed.");
                } else if (count != null) {
                    migrationRollbackService.rollbackMigrationCount(count);
                    MigrationLogger.logInfo("Rollback of " + count + " migrations completed.");
                } else if (rollbackDate != null) {
                    migrationRollbackService.rollbackMigrationByDate(rollbackDate);
                    MigrationLogger.logInfo("Rollback completed by date " + rollbackDate);
                } else {
                    MigrationLogger.logInfo("Error: You must provide either --migration, --count, or --date for rollback.");
                }
                break;

            case "status":
                MigrationLogger.logInfo("Current migration status:");
                migrationStatus.info();
                break;

            default:
                MigrationLogger.logInfo("Unknown command: " + command);
                break;
        }
    }

    public static void main(String[] args) {
        MigrationService migrationService = DependencyFactory.getMigrationService();
        MigrationRollbackService migrationRollbackService = DependencyFactory.getMigrationRollbackService();
        MigrationStatus migrationStatus = DependencyFactory.getMigrationStatus();


        int exitCode = new CommandLine(new MigrationToolCLI(migrationService, migrationRollbackService, migrationStatus)).execute(args);
        System.exit(exitCode);
    }
}
