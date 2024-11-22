package org.example.utils;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MigrationFileReader {
    private static final String MIGRATION_DIRECTORY = "src/main/resources/db.changelog/versions";
    private final ConnectionUtils connectionUtils;

    public static List<File> getMigrationFiles() throws IOException {
        File migrationDir = new File(MIGRATION_DIRECTORY);

        if (!migrationDir.exists() || !migrationDir.isDirectory()) {
            throw new RuntimeException(MIGRATION_DIRECTORY + " does not exist or is not a directory");
        }

        return Files.list(migrationDir.toPath())
                .filter(path -> path.toString().endsWith(".sql"))
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    public static List<String> getMigrationNames() throws IOException {
        return getMigrationFiles().stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static String readSqlFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new RuntimeException(filePath + " does not exist");
        }
        return Files.readString(path);
    }

    public List<String> getMigrationsBeforeDate(String rollbackDate) {
        String query = "SELECT migration_name FROM applied_migrations WHERE applied_at > ? ORDER BY applied_at DESC";

        try (Connection connection = connectionUtils.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(rollbackDate));
            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> migrations = new ArrayList<>();
            while (resultSet.next()) {
                migrations.add(resultSet.getString("migration_name"));
            }
            return migrations;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
