package org.example.executor;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
@RequiredArgsConstructor
public class MigrationExecutor {

    public static void executeMigration(File migrationFile, Connection connection) {
        try {
            String query = Files.readString(migrationFile.toPath());
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute migration file: " + migrationFile.getName(), e);
        }
    }
}