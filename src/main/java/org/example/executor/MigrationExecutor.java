package org.example.executor;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;

public class MigrationExecutor {

    public static void executeMigration(File migrationFile, Connection connection) {
        try (Statement statement = connection.createStatement()) {

            String sql = new String(Files.readAllBytes(migrationFile.toPath()));
            statement.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error during migration: " + migrationFile.getName(), e);
        }
    }
}
