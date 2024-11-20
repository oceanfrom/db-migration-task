package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.executor.MigrationExecutor;
import org.example.manager.MigrationManager;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationFileReader;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MigrationService {
    private final MigrationManager migrationManager;
    private final ConnectionUtils connectionUtils;

    public void runMigrations(){
        migrationManager.createTablesIfNotExist();

        List<File> successfullyAppliedMigrations = new ArrayList<>();
        boolean hasErrors = false;

        try(Connection connection = connectionUtils.getConnection()) {

            List<File> migrations = MigrationFileReader.getMigrationFiles();


            for(File file : migrations) {
                MigrationExecutor.executeMigration(file, connection);
                successfullyAppliedMigrations.add(file);
            }

            if(!hasErrors) {
                connection.commit();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
