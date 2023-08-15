package org.example;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class CreateProfessionalHandler implements HttpHandler {
    public void handle(@NotNull HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptionsRequest(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            Connection connection;
            PreparedStatement statement;
            String requestBody = getRequestBody(exchange);
            System.out.println(requestBody);

            CreateProfessionalHandler.JsonUtils.extractValuesFromJson(requestBody);
            String name = CreateProfessionalHandler.JsonUtils.getName();
            String email = CreateProfessionalHandler.JsonUtils.getEmail();
            String password = CreateProfessionalHandler.JsonUtils.getPasswordHash();
            Integer specializationid = CreateProfessionalHandler.JsonUtils.getSpecializationId();
            String addUserQuery = "INSERT INTO public.\"Professionals\" (name, email, password, specializationid) VALUES (?,?,?,?)";
            try {
                connection = Postgres.getConnection();
                statement = connection.prepareStatement(addUserQuery);
                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, passwordHash.hashPassword(password));
                statement.setInt(4, specializationid);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                // Unique constraint violation error
                String response = "User with this email already exists.";
                exchange.sendResponseHeaders(400, response.length());
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes());
                outputStream.close();
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, requestBody.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(requestBody.getBytes());
            outputStream.close();
        } else {
            String response = "CreateUserHandler only accepts POST requests";
            exchange.sendResponseHeaders(405, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private void handleOptionsRequest(@NotNull HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        sendResponse(exchange);
    }

    private void sendResponse(@NotNull HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, "".length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write("".getBytes());
        outputStream.close();
    }

    private @NotNull String getRequestBody(@NotNull HttpExchange exchange) throws IOException {
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
        private static String name;
        private static String email;
        private static String password;
        private static Integer specializationid;
        public static void extractValuesFromJson(String json) {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // Access specific values using their properties/keys
            name = jsonObject.get("name").getAsString();
            email = jsonObject.get("email").getAsString();
            password = jsonObject.get("password").getAsString();
            specializationid = jsonObject.get("specializationid").getAsInt();

            // Print the extracted values
            System.out.println(name);
            System.out.println(email);
            System.out.println(password);
            System.out.println(specializationid);
        }

        public static String getName() {
            return name;
        }

        public static String getEmail() {
            return email;
        }

        public static String getPasswordHash() {
            return password;
        }

        public static Integer getSpecializationId() {
            return specializationid;
        }
    }
}
