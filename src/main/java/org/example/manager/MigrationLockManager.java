package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MigrationLockManager {

    private final ConnectionUtils connectionUtils;

    private static final String LOCK_ID = "migration_lock";

    public boolean acquireLock() {
        try (Connection connection = connectionUtils.getConnection()) {
            String lockQuery = "SELECT pg_advisory_lock(12345)";
            try (PreparedStatement stmt = connection.prepareStatement(lockQuery)) {
                stmt.executeQuery();
            }

            String updateLockQuery = "INSERT INTO migration_lock (lock_id, locked_at) " +
                    "VALUES (?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT (lock_id) DO UPDATE SET locked_at = CURRENT_TIMESTAMP, released_at = NULL";
            try (PreparedStatement stmt = connection.prepareStatement(updateLockQuery)) {
                stmt.setString(1, LOCK_ID);
                stmt.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Error acquiring lock", e);
        }
    }

    public void releaseLock() {
        try (Connection connection = connectionUtils.getConnection()) {
            String unlockQuery = "SELECT pg_advisory_unlock(12345)";
            try (PreparedStatement stmt = connection.prepareStatement(unlockQuery)) {
                stmt.executeQuery();
            }

            String updateLockQuery = "UPDATE migration_lock SET released_at = CURRENT_TIMESTAMP WHERE lock_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateLockQuery)) {
                stmt.setString(1, LOCK_ID);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error releasing lock", e);
        }
    }
}
