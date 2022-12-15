package org.ws.tdd.rest;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.*;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpikeTest {
    Server server;
    @BeforeEach
    public void setup() throws Exception {
        server = new Server(8080);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);
        ServletContextHandler handler = new ServletContextHandler(server, "/");
        TestApplication application = new TestApplication();
        handler.addServlet(new ServletHolder(new ResourceServlet(application, new TestProviders(application))),"/");
        server.setHandler(handler);
        server.start();
    }

    @AfterEach
    public void stop() throws Exception {
        server.stop();
    }

    @Test
    public void should() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8080/")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("test", response.body());
    }

    static class ResourceServlet extends HttpServlet {
        private Application application;
        private Providers providers;

        public ResourceServlet(Application application, Providers providers) {
            this.application = application;
            this.providers = providers;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            Stream<Class<?>> stream = application.getClasses().stream().filter(c -> c.isAnnotationPresent(Path.class));
            Object resource = dispatch(stream);
            System.out.println("in servlet");
            MessageBodyWriter<Object> writer = (MessageBodyWriter<Object>) providers.getMessageBodyWriter(resource.getClass(), null, null, null);
            writer.writeTo(resource, null, null, null, null, null, resp.getOutputStream());
        }
        Object dispatch(Stream<Class<?>> rootResource) {
            Class<?> rootClass = rootResource.findFirst().get();
            try {
                Object resource = rootClass.getConstructor().newInstance();
                Method method =  Arrays.stream(resource.getClass().getMethods()).filter(m-> m.isAnnotationPresent(GET.class)).findFirst().get();

                return method.invoke(resource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class TestProviders implements Providers{
        private Application application;
        private List<MessageBodyWriter> writers;

        public TestProviders(Application application) {
            this.application = application;
            this.writers = (List<MessageBodyWriter>) this.application.getClasses().stream().filter(c-> MessageBodyWriter.class.isAssignableFrom(c))
                    .map(c-> {
                        try {
                            return c.getConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return null;
        }

        @Override
        public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return writers.stream().filter(w -> w.isWriteable(type, genericType, annotations, mediaType)).findFirst().get();
        }

        @Override
        public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
            return null;
        }

        @Override
        public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
            return null;
        }
    }

    @Provider
    static class StringMessageBodyWriter implements MessageBodyWriter<String>{

        public StringMessageBodyWriter() {
        }

        @Override
        public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        @Override
        public void writeTo(String s, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            PrintWriter writer = new PrintWriter(entityStream);
            writer.write(s);
            writer.flush();
        }
    }
    static class TestApplication extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(TestResource.class, StringMessageBodyWriter.class);
        }
    }
    @Path("/test")
    static class TestResource{
        public TestResource() {
        }
        @GET
        public String get(){
            return "test";
        }
    }
}
