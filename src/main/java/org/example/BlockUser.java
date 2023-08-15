package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlockUser {
    static int checkFailedAttempts(Connection connection,Integer userid) throws SQLException {
        PreparedStatement statement;
        ResultSet resultSet;
        byte failedAttempts = 0;

        try {
            // Prepare a SQL query to retrieve the hashed password based on the email
            String query = "SELECT SUM(CASE WHEN attempt = false THEN 1 ELSE 0 END) FROM public.\"SignInTrackers\" WHERE userid = ?;";
            statement = connection.prepareStatement(query);
            statement.setInt(1, userid);

            // Execute the query
            resultSet = statement.executeQuery();
            // Check if the query returned any result
            if (resultSet.next()) {
                // Retrieve the hashed password from the result set
                failedAttempts = resultSet.getByte("sum");
            }

        } finally {
          System.out.println("No new connection added");
        }

        return failedAttempts;
    }

    // Helper method to reset the lockout status in the database
    static void resetFailedAttempts(Integer userid, Connection connection) throws SQLException {
        PreparedStatement resetAttemptsStatement = null;

        try {
            // Prepare a SQL query to reset the failed attempts count
            String resetAttemptsQuery = "UPDATE public.\"SignInTrackers\" SET attempt = true WHERE userid = ?";
            resetAttemptsStatement = connection.prepareStatement(resetAttemptsQuery);
            resetAttemptsStatement.setInt(1, userid);

            // Execute the query
            resetAttemptsStatement.executeUpdate();
        } finally {
            // Close the statement to free up connections and prevent resource leaks
            if (resetAttemptsStatement != null) {
                resetAttemptsStatement.close();
            }
        }
    }
}
