package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.utils.ConnectionUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class AppliedMigrationManager {
    private final ConnectionUtils connectionUtils;

    public void markMigrationAsApplied(File migrationFile, boolean success, String message) {
        String query =
                "INSERT INTO applied_migrations (migration_name, status, applied_at) " +
                        "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (migration_name) DO UPDATE SET " +
                        "status = EXCLUDED.status, applied_at = CASE WHEN EXCLUDED.status = 'APPLIED' THEN CURRENT_TIMESTAMP ELSE NULL END;";
        try (Connection connection = connectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, migrationFile.getName());
            stmt.setString(2, success ? "APPLIED" : "FAILED");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking migration as applied", e);
        }
    }

    public boolean isMigrationApplied(File migrationFile) {
        String checkQuery =
                "SELECT COUNT(*) FROM applied_migrations " +
                        "WHERE migration_name = ? AND status = 'APPLIED'";
        try (Connection connection = connectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, migrationFile.getName());
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking migration status", e);
        }
    }

    public void markMigrationAsRolledBack(String migrationName) {
        String updateQuery = "UPDATE applied_migrations SET rollbacked_on = CURRENT_TIMESTAMP WHERE migration_name = ?";
        try (Connection connection = connectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, migrationName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error when marking migration as rollback", e);
        }
    }

    public boolean isMigrationRolledBack(String migrationName) {
        String checkQuery =
                "SELECT COUNT(*) FROM applied_migrations " +
                        "WHERE migration_name = ? AND rollbacked_on IS NOT NULL";
        try (Connection connection = connectionUtils.getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, migrationName);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if migration is rolled back", e);
        }
    }
}