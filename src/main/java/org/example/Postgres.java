package org.example;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;
public class Postgres {
    public static Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        final String url = dotenv.get("urls");
        final String DB_USER = dotenv.get("DB_USER");
        final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

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
