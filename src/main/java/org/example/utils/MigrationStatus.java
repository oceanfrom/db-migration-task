package org.example.utils;

import lombok.RequiredArgsConstructor;
import org.example.manager.MigrationLockManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MigrationStatus {


    private static final String SELECT_MIGRATIONS_QUERY = "SELECT migration_name, rollbacked_on FROM applied_migrations ORDER BY applied_at ASC";
    private final MigrationLockManager migrationLockManager;

    public void info() {
        String lastMigration = null;

        if(!migrationLockManager.acquireLock())
            throw new RuntimeException("Failed to acquire lock for migration rollback");

        try (Connection connection = new ConnectionUtils().getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_MIGRATIONS_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String migrationName = resultSet.getString("migration_name");
                String rollbackedOn = resultSet.getString("rollbacked_on");

                if (rollbackedOn != null) {
                    System.out.println("Migration " + migrationName + " was rolled back on " + rollbackedOn);
                } else {
                    System.out.println("Migration " + migrationName + " was applied successfully (no rollback).");
                    lastMigration = migrationName;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            migrationLockManager.releaseLock();
        }

        if (lastMigration != null) {
            System.out.println("\nLast active migration: " + lastMigration);
        } else {
            System.out.println("\nNo active migrations found.");
        }
    }

}
