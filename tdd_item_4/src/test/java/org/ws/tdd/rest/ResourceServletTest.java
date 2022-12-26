package org.ws.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceServletTest extends ServletTest{

    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext context;
    private Providers providers;
    private OutboundResponseBuilder response;

    @Override
    protected Servlet getServlet() {
        runtime = mock(Runtime.class);
        router = mock(ResourceRouter.class);
        context = mock(ResourceContext.class);
        providers = mock(Providers.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createResourceContext(any(), any())).thenReturn(context);
        when(runtime.getProviders()).thenReturn(providers);

        return new ResourceServlet(runtime);
    }

    @BeforeEach
    public void before(){
        response = new OutboundResponseBuilder();
        RuntimeDelegate delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);

        when(delegate.createHeaderDelegate(NewCookie.class)).thenReturn(new RuntimeDelegate.HeaderDelegate<>() {
            @Override
            public NewCookie fromString(String value) {
                return null;
            }

            @Override
            public String toString(NewCookie value) {
                return value.getName() + "="+ value.getValue();
            }
        });
    }

    @Test
    public void should_use_status_from_response() throws Exception{
        response.status(Response.Status.NOT_MODIFIED).returnFrom(router);
        HttpResponse<String> httpResponse = get("/test");
        assertEquals(httpResponse.statusCode(), Response.Status.NOT_MODIFIED.getStatusCode());
    }
    @Test
    public void should_use_http_headers_from_response() throws Exception{
        response.headers("Set-Cookie", new NewCookie.Builder("SESSION_ID").value("session").build(), new NewCookie.Builder("USER_ID").value("user").build()).returnFrom(router);
        HttpResponse<String> httpResponse = get("/test");
        assertArrayEquals(httpResponse.headers().allValues("Set-Cookie").toArray(String[]::new), new String[]{"SESSION_ID=session", "USER_ID=user"});

    }
    @Test
    public void should_write_entity_to_http_response_using_message_body_writer() throws Exception{
        response.entity(new GenericEntity<>("entity", String.class), new Annotation[0]).returnFrom(router);
        HttpResponse<String> httpResponse = get("/test");
        assertEquals(httpResponse.body(), "entity");
    }
    @Test
    public void should_use_response_from_web_application_exception() throws Exception {
        response.status(Response.Status.FORBIDDEN)
                .headers(HttpHeaders.SET_COOKIE, new NewCookie.Builder("SESSION_ID").value("session").build())
                .entity(new GenericEntity<>("error", String.class), new Annotation[0])
                .throwFrom(router);
        HttpResponse<String> httpResponse = get("/test");
        assertEquals(httpResponse.statusCode(), Response.Status.FORBIDDEN.getStatusCode());
        assertArrayEquals(new String[]{"SESSION_ID=session"}, httpResponse.headers().allValues(HttpHeaders.SET_COOKIE).toArray(String[]::new));
        assertEquals("error", httpResponse.body());
    }
    @Test
    public void should_build_response_by_exception_mapper_if_null_response_from_web_application_exception() throws Exception {
        when(router.dispatch(any(), eq(context))).thenThrow(RuntimeException.class);
        when(providers.getExceptionMapper(eq(RuntimeException.class))).thenReturn(exception -> response.status(Response.Status.FORBIDDEN).build());
        HttpResponse<String> httpResponse = get("/test");

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), httpResponse.statusCode());
    }

    //TODO: 500 if MessageBodyWriter not found
    //TODO: entity is null, ignore MessageBodyWriter
    class OutboundResponseBuilder{
        private Response.Status status = Response.Status.OK;
        private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        private GenericEntity<String> entity = new GenericEntity<>("entity", String.class);
        private Annotation[] annotations = new Annotation[0];
        private MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

        public OutboundResponseBuilder status(Response.Status status){
            this.status = status;
            return this;
        }

        public OutboundResponseBuilder headers(String name, Object... values){
            this.headers.addAll(name, values);
            return this;
        }

        public OutboundResponseBuilder entity(GenericEntity<String> entity, Annotation[] annotations){
            this.entity = entity;
            this.annotations = annotations;
            return this;
        }

        void returnFrom(ResourceRouter router){
            build(outboundResponse -> when(router.dispatch(any(), eq(context))).thenReturn(outboundResponse));
        }

        void throwFrom(ResourceRouter router){
            build(outboundResponse -> {
                WebApplicationException exception = new WebApplicationException(outboundResponse);
                when(router.dispatch(any(), eq(context))).thenThrow(exception);
            });
        }
        void build(Consumer<OutboundResponse> consumer){
            OutboundResponse response = build();
            consumer.accept(response);
            stubMessageBodyWriter();
        }
        private OutboundResponse build() {
            OutboundResponse response = mock(OutboundResponse.class);
            when(response.getStatus()).thenReturn(status.getStatusCode());
            when(response.getStatusInfo()).thenReturn(status);
            when(response.getHeaders()).thenReturn(headers);
            when(response.getGenericEntity()).thenReturn(entity);
            when(response.getAnnotations()).thenReturn(annotations);
            when(response.getMediaType()).thenReturn(mediaType);
            stubMessageBodyWriter();

            return response;
        }
        private void stubMessageBodyWriter() {
            when(providers.getMessageBodyWriter(eq(String.class), eq(String.class), same(annotations), eq(mediaType))).thenReturn(new MessageBodyWriter<>() {
                @Override
                public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                    return false;
                }
                @Override
                public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
                    PrintWriter writer = new PrintWriter(entityStream);
                    writer.write(s);
                    writer.flush();
                }
            });
        }
    }
}
