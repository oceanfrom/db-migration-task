package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MigrationLockManager {
    private final ConnectionUtils connectionUtils;

    public boolean acquireLock() {
        try (Connection connection = connectionUtils.getConnection()) {
            String lockQuery = "SELECT pg_advisory_lock(12345)";
            try (PreparedStatement stmt = connection.prepareStatement(lockQuery)) {
                stmt.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при захвате блокировки", e);
        }
    }

    public void releaseLock() {
        try (Connection connection = connectionUtils.getConnection()) {
            String unlockQuery = "SELECT pg_advisory_unlock(12345)";
            try (PreparedStatement stmt = connection.prepareStatement(unlockQuery)) {
                stmt.executeQuery();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при освобождении блокировки", e);
        }
    }
}
