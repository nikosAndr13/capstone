package org.example;

import java.sql.*;
import com.sun.net.httpserver.*;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
public class UsersHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptionsRequest(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;

            try {
                connection = Postgres.getConnection();
                statement = connection.createStatement();
                rs = statement.executeQuery("SELECT * FROM public.\"Users\";");

                List<User> userList = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String passwordHash = rs.getString("password");

                    User user = new User(id, name, email, passwordHash);
                    userList.add(user);
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(userList);

                System.out.println(json);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(json.getBytes());
                outputStream.close();
            } catch (SQLException e) {
                System.err.println("Error executing SQL query: " + e.getMessage());
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
            } finally {
                closeResources(connection, statement, rs);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.set("Access-Control-Allow-Methods", "GET, OPTIONS");
        headers.set("Access-Control-Allow-Headers",
                "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

        sendResponse(exchange, 200, "");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    static class User {
        private final int id;
        private final String username;
        private final String email;
        private final String passwordHash;

        public User(int id, String username, String email, String passwordHash) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.passwordHash = passwordHash;
        }
    }

    private static void closeResources(Connection connection, Statement statement, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
