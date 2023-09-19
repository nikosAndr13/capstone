package org.example;

import java.sql.*;
import com.sun.net.httpserver.*;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
public class ProfessionalsHandler implements  HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "https://capstone-project-liart-one.vercel.app");
        headers.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptionsRequest(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            String queryString = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParameters(queryString);

            // Retrieve the "limit" and "offset" parameters (add error handling)
            String limitParam = queryParams.get("limit");

            int limit = Integer.parseInt(limitParam);
            Connection connection = null;
            Statement statement = null;
            ResultSet rs = null;
            try {
                connection = Postgres.getConnection();
                statement = connection.createStatement();

                ResultSet totalRowsResult = statement.executeQuery("SELECT COUNT(*) AS total_rows FROM public.\"Professionals\"");
                int totalRows = 0;
                if (totalRowsResult.next()) {
                    totalRows = totalRowsResult.getInt("total_rows");
                }

                rs = statement.executeQuery("SELECT \n" +
                        "P.name,\n" +
                        "P.email,\n" +
                        "S.specialization AS specialization,\n" +
                        "\"Calendly_Link\" as calendly\n" +
                        "FROM public.\"Professionals\" P\n" +
                        "INNER JOIN public.\"specializations\" S ON P.specializationid = S.id" +
                        " LIMIT "+ limit + ";");

                List<ProfessionalsHandler.User> userList = new ArrayList<>();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String specialization = rs.getString("specialization");
                    String calendly = rs.getString("calendly");

                    ProfessionalsHandler.User user = new ProfessionalsHandler.User(name, email, specialization, calendly);
                    userList.add(user);
                }

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("totalRows", totalRows); // Include the total number of rows
                responseMap.put("data", userList);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(responseMap);

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
        headers.set("Access-Control-Allow-Origin", "*");
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

    static class User {
        private final String username;
        private final String email;
        private final String specialization;
        private final String calendly;

        public User(String username, String email, String specialization, String calendly) {
            this.username = username;
            this.email = email;
            this.specialization = specialization;
            this.calendly = calendly;
        }
    }

    private Map<String, String> parseQueryParameters(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
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
