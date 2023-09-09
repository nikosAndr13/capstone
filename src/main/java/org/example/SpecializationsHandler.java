package org.example;

import java.sql.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.sun.net.httpserver.Headers;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
public class SpecializationsHandler implements  HttpHandler{
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptionsRequest(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;

            try {
                connection = Postgres.getConnection();
                statement = connection.createStatement();
                rs = statement.executeQuery("SELECT * FROM public.\"specializations\";");

                List<specialization> userList = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("specialization");

                    specialization specializations = new specialization(id, name);
                    userList.add(specializations);
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(userList);


                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.getBytes().length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(json.getBytes());
                outputStream.close();
            } catch (SQLException e) {
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

        sendResponse(exchange);
    }

    private void sendResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, "".length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write("".getBytes());
        outputStream.close();
    }

    static class specialization {
        private final int id;
        private final String specialization;

        public specialization(int id, String specialization) {
            this.id = id;
            this.specialization = specialization;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
