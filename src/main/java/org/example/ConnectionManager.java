package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    public Connection getConnection() throws SQLException {
        String url = PropertiesUtils.getDbUrl();
        String user = PropertiesUtils.getDbUser();
        String password = PropertiesUtils.getDbPassword();

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to the database", e);
        }
    }
}
