package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.logger.MigrationLogger;
import org.example.manager.MigrationManager;
import org.example.report.MigrationReport;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationFileReader;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MigrationService {
    private final MigrationManager migrationManager;
    private final ConnectionUtils connectionUtils;
    private final MigrationReport migrationReport;

    public void runMigrations(){
        migrationManager.createTablesIfNotExist();

        if (!migrationManager.acquireLock()) {
            MigrationLogger.logInfo("Migrations are already being performed in another process");
            return;
        }

        List<File> successfullyAppliedMigrations = new ArrayList<>();

        try(Connection connection = connectionUtils.getConnection()) {
            connection.setAutoCommit(false);

            List<File> migrations = MigrationFileReader.getMigrationFiles();
            boolean hasErrors = false;
            for(File file : migrations) {
                String migrationName = file.getName();
                try{
                    if(migrationManager.isMigrationApplied(file)){
                        MigrationLogger.logInfo("Migration has been already applied" + migrationName);
                        migrationReport.addMigrationResult(migrationName, true, "Already applied");
                    } else {
                        MigrationLogger.logMigrationStart(migrationName);
                        MigrationExecutor.executeMigration(file, connection);
                        successfullyAppliedMigrations.add(file);
                        migrationReport.addMigrationResult(migrationName, true, "Already applied");
                        MigrationLogger.logInfo("Migration applied" + migrationName);
                    }
                } catch (Exception e) {
                    hasErrors = true;
                    MigrationLogger.logMigrationError(migrationName, e);
                    migrationReport.addMigrationResult(migrationName, false, "Error: " + e.getMessage());
                    MigrationLogger.logError("Error during migration" + migrationName + ": " + e.getMessage(), e);
                    connection.rollback();
                    successfullyAppliedMigrations.clear();
                    MigrationLogger.logError("All changes have been rolled back due to an error", e);
                    break;
                }
            }

            if(!hasErrors) {
                connection.commit();
                for(File file : successfullyAppliedMigrations) {
                    migrationManager.markMigrationAsAppleid(file, true, "Applied successfully");
                }
                MigrationLogger.logInfo("All migrations has been applied successfully");
            }
        } catch (Exception e) {
            MigrationLogger.logError("Error when performing migrations", e);
        } finally {
            migrationManager.releaseLock();
            try {
                migrationReport.generateJSONReport("migration_report.json");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
