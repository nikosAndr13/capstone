package org.example;

import java.sql.*;
public class getPasswordFromDb {

        static String retrieveHashedUserPasswordFromDatabase(Connection connection,String email) throws SQLException {
            PreparedStatement statement;
            ResultSet resultSet;
            String hashedPassword;

            try {
                // Prepare a SQL query to retrieve the hashed password based on the email
                String query = "SELECT password FROM public.\"Users\" WHERE email = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, email);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any result
                if (resultSet.next()) {
                    // Retrieve the hashed password from the result set
                    hashedPassword = resultSet.getString("password");
                } else {
                    hashedPassword = null;
                }
            } finally {
               System.out.println("Successfully retrieved");
            }

            return hashedPassword;
        }

        static String retrieveHashedProfPasswordFromDatabase(Connection connection,String email) throws SQLException {
            PreparedStatement statement = null;
            ResultSet resultSet;
            String hashedPassword;

            try {
                // Prepare a SQL query to retrieve the hashed password based on the email
                String query = "SELECT password FROM public.\"Professionals\" WHERE email = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, email);

                // Execute the query
                resultSet = statement.executeQuery();

                // Check if the query returned any result
                if (resultSet.next()) {
                    // Retrieve the hashed password from the result set
                    hashedPassword = resultSet.getString("password");
                } else {
                    hashedPassword = null;
                }
            } finally {
                // Close the database resources to free up connections and prevent resource
                // leaks
                if (statement != null) {
                    statement.close();
                }
            }

            return hashedPassword;
        }
    }

