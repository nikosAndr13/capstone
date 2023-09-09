package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class getUserId {
    static int retrieveUserId(Connection connection,String email) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Integer id = null;

        try {

            // Prepare a SQL query to retrieve the hashed password based on the email
            String query = "SELECT id FROM public.\"Users\" WHERE email = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, email);

            // Execute the query
            resultSet = statement.executeQuery();

            // Check if the query returned any result
            if (resultSet.next()) {
                // Retrieve the hashed password from the result set
                id = resultSet.getInt("id");
            }
        } finally {
            // Close the database resources to free up connections and prevent resource
            // leaks
            if (connection != null) {
                connection.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }

        return id;
    }

    static int retrieveProfId(Connection connection,String email) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Integer id;

        try {
            // Prepare a SQL query to retrieve the hashed password based on the email
            String query = "SELECT id FROM public.\"Professionals\" WHERE email = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, email);

            // Execute the query
            resultSet = statement.executeQuery();

            // Check if the query returned any result
            if (resultSet.next()) {
                // Retrieve the hashed password from the result set
                id = resultSet.getInt("id");
            } else {
                id = null;
            }
        } finally {
            // Close the database resources to free up connections and prevent resource
            // leaks
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }

        return id;
    }
}
