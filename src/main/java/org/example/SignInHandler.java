package org.example;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SignInHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptionsRequest(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            Connection connection;
            PreparedStatement statement;
            String requestBody = getRequestBody(exchange);
            System.out.println(requestBody);

            JsonUtils.extractValuesFromJson(requestBody);
            String email = JsonUtils.getEmail();
            String password = JsonUtils.getPasswordHash();

            try {
                connection = Postgres.getConnection();
                String query = "INSERT INTO public.\"SignInTrackers\" (userid, attempt) VALUES (?, ?)";
                statement = connection.prepareStatement(query);
                String storedHashedPassword = getPasswordFromDb.retrieveHashedUserPasswordFromDatabase(connection, email);

                if (storedHashedPassword != null) {
                    int userId = getUserId.retrieveUserId(connection, email);
                    Integer failedAttempts = BlockUser.checkFailedAttempts(connection, userId);
                    System.out.println(failedAttempts);

                    int maxFailedAttempts = 4;

                    // Verify the provided password against the stored hashed password
                    if (passwordHash.verifyPassword(password, storedHashedPassword)) {
                        if (failedAttempts >= maxFailedAttempts) {
                            BlockUser.resetFailedAttempts(userId, connection);
                        }
                        // track signIn TimeStamp
                        statement.setInt(1, userId);
                        statement.setBoolean(2, true);
                        statement.executeUpdate();
                        // Passwords match, authentication successful
                        String response = "Authentication successful.";
                        exchange.sendResponseHeaders(200, response.length());
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());
                        outputStream.close();

                    } else if (failedAttempts < maxFailedAttempts) {
                        // track signIn TimeStamp
                        statement.setInt(1, userId);
                        statement.setBoolean(2, false);
                        statement.executeUpdate();
                        // Passwords don't match, authentication failed
                        String response = "Authentication failed. Invalid password.";
                        exchange.sendResponseHeaders(401, response.length());
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());
                        outputStream.close();
                    }
                    if (failedAttempts >= maxFailedAttempts) {
                        // Account is locked, deny access and inform the user
                        String response = "Account is temporarily locked due to too many failed attempts. Please try again later.";
                        exchange.sendResponseHeaders(401, response.length());
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());
                        outputStream.close();
                    }
                } else {
                    // User not found in the database, handle accordingly
                    String response = "Account not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(response.getBytes());
                    outputStream.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();

                String response = "Error occurred while processing the request";
                exchange.sendResponseHeaders(500, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
            }
        }
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "https://capstone-project-liart-one.vercel.app/");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        sendResponse(exchange);
    }

    private void sendResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, "".length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write("".getBytes());
        outputStream.close();
    }

    private String getRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }
        br.close();
        return requestBody.toString();
    }

    static class JsonUtils {
        private static final Gson gson = new Gson();
        private static String email;
        private static String password;

        public static void extractValuesFromJson(String json) {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // Access specific values using their properties/keys
            email = jsonObject.get("email").getAsString();
            password = jsonObject.get("password").getAsString();

            // Print the extracted values
            System.out.println(email);
            System.out.println(password);
        }

        public static String getEmail() {
            return email;
        }

        public static String getPasswordHash() {
            return password;
        }

    }
}