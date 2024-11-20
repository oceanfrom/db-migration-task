package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MigrationHistoryManager {
    private final ConnectionUtils connectionUtils;

    public void logMigrationHistory(String migrationName, String status, String errorMessage) {
        String query = "INSERT INTO migration_history (migration_name, status, error_message) " +
                "VALUES (?, ?, ?)";
        try (Connection connection = connectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, migrationName);
            stmt.setString(2, status);
            stmt.setString(3, errorMessage);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while recording migration history", e);
        }
    }
}
