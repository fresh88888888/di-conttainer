package org.ws.tdd.rest;


import jakarta.servlet.Servlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class ServletTest {
    Server server;
    @BeforeEach
    public void setup() throws Exception {
        server = new Server(8080);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);
        ServletContextHandler handler = new ServletContextHandler(server, "/");
        handler.addServlet(new ServletHolder(getServlet()),"/");
        server.setHandler(handler);
        server.start();
    }

    @AfterEach
    public void stop() throws Exception {
        server.stop();
    }

    protected abstract Servlet getServlet();

    protected URI path(String path) throws Exception {
        return new URL(new URL("http://localhost:8080/"), path).toURI();
    }

    protected HttpResponse<String> get(String path) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(path(path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
