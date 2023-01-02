package org.ws.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceMethodsTest {
    @ParameterizedTest(name = "{3}")
    @CsvSource(textBlock = """
            GET,        /messages/hello,        Messages.hello,             GET and URI match
            POST,       /messages/hello,        Messages.postHello,         POST and URI match
            PUT,        /messages/hello,        Messages.putHello,          PUT and URI match
            DELETE,     /messages/hello,        Messages.deleteHello,       DELETE and URI match
            PATCH,      /messages/hello,        Messages.patchHello,        PATCH and URI match
            HEAD,       /messages/hello,        Messages.headHello,         HEAD and URI match
            OPTIONS,    /messages/hello,        Messages.optionsHello,      OPTIONS and URI match
            GET,        /messages/topics/{id},  Messages.topicId,           GET and URI match
            GET,        /messages/topics/1234,  Messages.topic1234,         GET and URI match
            GET,        /messages,              Messages.get,               GET with resource method without Path
            HEAD,       /messages/head,         Messages.getHead,           HEAD with GET resource method
            """)
    public void should_resource_match_method_in_root_resource(String httpMethod, String path, String resourceMethod, String context) {
        ResourceMethods methods = new ResourceMethods(Messages.class.getMethods());
        UriTemplate.MatchResult result = new PathTemplate("/messages").match(path).get();
        String remaining = result.getRemaining() == null ? "" : result.getRemaining();
        ResourceRouter.ResourceMethod method = methods.findResourceMethods(remaining, httpMethod).get();

        assertEquals(resourceMethod, method.toString());
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,        /missing-messages/1,        GET and URI match
            POST,       /missing-messages,          http method not matched
            """)
    public void should_return_empty_if_not_matched(String httpMethod, String path, String context) {
        ResourceMethods methods = new ResourceMethods(MissingMessages.class.getMethods());
        UriTemplate.MatchResult result = new PathTemplate("/missing-messages").match(path).get();
        String remaining = result.getRemaining() == null ? "" : result.getRemaining();

        assertTrue(methods.findResourceMethods(remaining, httpMethod).isEmpty());
    }
    @Test
    public void should_convert_get_resource_method_to_head_resource_method(){
        ResourceMethods resourceMethods = new ResourceMethods(Messages.class.getMethods());
        UriTemplate.MatchResult result = new PathTemplate("/messages").match("/messages/head").get();
        ResourceRouter.ResourceMethod method = resourceMethods.findResourceMethods(result.getRemaining(), "HEAD").get();

        assertInstanceOf(HeadResourceMethod.class, method);
    }
    //TODO: OPTIONS
    @Test
    public void should_get_options_for_given_uri(){
        RuntimeDelegate delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
        ResourceContext context = mock(ResourceContext.class);
        UriInfoBuilder builder = mock(UriInfoBuilder.class);

        ResourceMethods resourceMethods = new ResourceMethods(Messages.class.getMethods());
        UriTemplate.MatchResult result = new PathTemplate("/messages").match("/messages/head").get();

        ResourceRouter.ResourceMethod method = resourceMethods.findResourceMethods(result.getRemaining(), "OPTIONS").get();
        GenericEntity<?> entity = method.call(context, builder);
        Response response = (Response) entity.getEntity();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertEquals(Set.of(HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.HEAD), response.getAllowedMethods());
    }
    @Test
    public void should_not_include_head_in_options_if_given_uri_not_head_get_method(){
        RuntimeDelegate delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
        ResourceContext context = mock(ResourceContext.class);
        UriInfoBuilder builder = mock(UriInfoBuilder.class);

        ResourceMethods resourceMethods = new ResourceMethods(Messages.class.getMethods());
        UriTemplate.MatchResult result = new PathTemplate("/messages").match("/messages/no-head").get();

        ResourceRouter.ResourceMethod method = resourceMethods.findResourceMethods(result.getRemaining(), "OPTIONS").get();
        GenericEntity<?> entity = method.call(context, builder);
        Response response = (Response) entity.getEntity();

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertEquals(Set.of(HttpMethod.POST, HttpMethod.OPTIONS), response.getAllowedMethods());
    }
    @Path("/missing-messages")
    static class MissingMessages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "messages";
        }
    }
    @Path("/messages")
    static class Messages {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "messages";
        }
        @GET
        @Path("/head")
        @Produces(MediaType.TEXT_PLAIN)
        public String getHead() {
            return "head";
        }
        @POST
        @Path("/no-head")
        @Produces(MediaType.TEXT_PLAIN)
        public void postNoHead() {
        }
        @GET
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String hello() {
            return "hello";
        }

        @POST
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String postHello() {
            return "hello";
        }

        @PUT
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String putHello() {
            return "hello";
        }

        @DELETE
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String deleteHello() {
            return "hello";
        }

        @PATCH
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String patchHello() {
            return "hello";
        }

        @HEAD
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String headHello() {
            return "hello";
        }

        @OPTIONS
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String optionsHello() {
            return "hello";
        }

        @GET
        @Path("/topics/{id}")
        @Produces(MediaType.TEXT_PLAIN)
        public String topicId() {
            return "topicId";
        }

        @GET
        @Path("/topics/1234")
        @Produces(MediaType.TEXT_PLAIN)
        public String topic1234() {
            return "topicId";
        }
    }
}
