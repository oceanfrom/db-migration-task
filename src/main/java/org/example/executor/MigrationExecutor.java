package org.example.executor;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
public class MigrationExecutor {

    public static void executeMigration(File migrationFile, Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String query = Files.readString(migrationFile.toPath());
            statement.executeUpdate(query);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute migration file: " + migrationFile.getName(), e);
        }
    }
}