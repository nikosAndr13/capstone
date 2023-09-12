package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        String contextPath = "/api";
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(contextPath + "/professionals", new ProfessionalsHandler());
        server.createContext(contextPath + "/users/create", new CreateUserHandler());
        server.createContext(contextPath + "/users/login", new SignInHandler());
        server.createContext(contextPath + "/professional/create", new CreateProfessionalHandler());
        server.createContext(contextPath + "/professional/login", new ProfessionalSignInHandler());
        server.createContext(contextPath + "/specialization", new SpecializationsHandler());
        server.start();
        System.out.println("Server started on port " + port);
    }
}