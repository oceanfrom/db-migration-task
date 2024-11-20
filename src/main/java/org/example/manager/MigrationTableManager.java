package org.example.manager;

import lombok.RequiredArgsConstructor;
import org.example.utils.ConnectionUtils;
import org.example.utils.MigrationFileReader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class MigrationTableManager {
    private final ConnectionUtils connectionUtils;

    public void createTablesIfNotExist() {
        try {
            String createTablesQuery = MigrationFileReader.readSqlFile("src/main/resources/sql/create_tables.sql");

            try(Connection connection = connectionUtils.getConnection()) {
                Statement statement = connection.createStatement();
                connection.setAutoCommit(false);
                statement.execute(createTablesQuery);
                connection.commit();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading SQL file to create tables",e);
        } catch (SQLException e) {
            throw new RuntimeException("Error when executing SQL query to create tables", e);
        }
    }
}
