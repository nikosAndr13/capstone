package org.example;

import java.sql.*;
public class Postgres {
    private static final String url = "jdbc:postgresql://trumpet.db.elephantsql.com:5432/tmwqmrtr";
    private static final String DB_USER = "tmwqmrtr";
    private static final String DB_PASSWORD = "ubL-UpR6OnoBkBKPm4oG7eqhkiix09Db";
    public static Connection getConnection() throws SQLException {
        Connection connection;
        try {
            connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            System.out.println("Connected to the database successfully!");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            throw e;
        }
        return connection;
    }
}
