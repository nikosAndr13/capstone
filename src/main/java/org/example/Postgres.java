package org.example;

import java.sql.*;

public class Postgres {
    public static Connection getConnection() throws SQLException {
        String URLS = System.getenv("URLS");
        String DB_USER = System.getenv("DB_USER");
        String DB_PASSWORD = System.getenv("DB_PASSWORD");
        Connection connection;
        try {
            connection = DriverManager.getConnection(URLS, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            throw e;
        }
        return connection;
    }
}
