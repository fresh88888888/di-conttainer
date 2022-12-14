package org.ws.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RootResourceTest {
    private ResourceContext context;
    private Messages rootResource;
    @BeforeEach
    public void setup(){
        rootResource = new Messages();
        context = mock(ResourceContext.class);
        when(context.getResource(eq(Messages.class))).thenReturn(rootResource);
    }
    @Test
    public void should_get_uri_template_from_path_annotation() {
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate template = resource.getUriTemplate();

        assertTrue(template.match("/messages/hello").isPresent());
    }

    @ParameterizedTest(name = "{3}")
    @CsvSource(textBlock = """
            GET,        /messages/hello,        Messages.hello,             GET and URI match
            GET,        /messages/ah,           Messages.ah,                GET and URI match
            POST,       /messages/hello,        Messages.postHello,         POST and URI match
            PUT,        /messages/hello,        Messages.putHello,          PUT and URI match
            DELETE,     /messages/hello,        Messages.deleteHello,       DELETE and URI match
            PATCH,      /messages/hello,        Messages.patchHello,        PATCH and URI match
            HEAD,       /messages/hello,        Messages.headHello,         HEAD and URI match
            OPTIONS,    /messages/hello,        Messages.optionsHello,      OPTIONS and URI match
            GET,        /messages/topics/{id},  Messages.topicId,           GET and URI match
            GET,        /messages/topics/1234,  Messages.topic1234,         GET and URI match
            GET,        /messages,              Messages.get,               GET with resource method without Path
            GET,        /messages/1/content,    Message.content,            Map to sub-resource method
            GET,        /messages/1/body,       MessageBody.get,            Map to sub-sub-resource method
            """)
    public void should_resource_match_method_in_root_resource(String httpMethod, String path, String resourceMethod, String content) {
        UriInfoBuilder builder = new StubUriInfoBuilder();
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();
        ResourceRouter.ResourceMethod method = resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, context, builder).get();

        assertEquals(resourceMethod, method.toString());
    }
    @Test
    public void should_resource_match_method_in_sub_resource(){
        ResourceRouter.Resource resource = new ResourceHandler(new Message(), mock(PathTemplate.class));
        UriTemplate.MatchResult result = mock(UriTemplate.MatchResult.class);
        when(result.getRemaining()).thenReturn("/content");
        assertTrue(resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, context, mock(UriInfoBuilder.class)).isPresent());
    }
    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,        /missing-messages/1,        GET and URI match
            POST,       /missing-messages,          http method not matched
            """)
    public void should_return_empty_if_not_matched(String httpMethod, String path, String content) {
        ResourceRouter.Resource resource = new ResourceHandler(MissingMessages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(path).get();
        assertTrue(resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, context, mock(UriInfoBuilder.class)).isEmpty());
    }
    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            GET,        /messages/header,        No matched resource method
            GET,        /messages/1/header,      No matched sub-resource method
            """)
    public void should_return_empty_if_not_matched_in_root_resource(String httpMethod, String uri, String content) {
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();
        uriInfoBuilder.addMatchedResource(new Messages());
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match(uri).get();
        assertTrue(resource.match(result, httpMethod, new String[]{MediaType.TEXT_PLAIN}, context, uriInfoBuilder).isEmpty());
    }
    @Test
    public void should_add_last_match_resource_to_uri_info_builder(){
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match("/messages").get();
        resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, context, uriInfoBuilder);
    }
    @Test
    public void should_add_last_match_path_parameters_uri_info_builder(){
        StubUriInfoBuilder uriInfoBuilder = new StubUriInfoBuilder();
        ResourceRouter.Resource resource = new ResourceHandler(Messages.class);
        UriTemplate.MatchResult result = resource.getUriTemplate().match("/messages/1").get();
        resource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, context, uriInfoBuilder);

        assertTrue(uriInfoBuilder.getLastMatchedResource() instanceof Message);
        //UriInfo uriInfo = uriInfoBuilder.createUriInfo();
        assertEquals(List.of("1"), uriInfoBuilder.getPathParameters().get("id"));
    }
    @Test
    public void should_throw_illegal_argument_exception_if_root_resource_not_have_path_annotation(){
        assertThrows(IllegalArgumentException.class, () -> new ResourceHandler(Message.class));
    }
    //TODO: Head and Options is special case
    @Path("/missing-messages")
    static class MissingMessages{
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
        @Path("/special")
        public String getSpecial() {
            return "special";
        }
        @HEAD
        public void head(){
        }
        @OPTIONS
        public void options(){
        }
        @GET
        @Path("/ah")
        @Produces(MediaType.TEXT_PLAIN)
        public String ah() {
            return "ah";
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
        @Path("/{id:[0-9]+}")
        public Message getById() {
            return new Message();
        }
    }
    static class Message{
        @GET
        @Path("/content")
        @Produces(MediaType.TEXT_PLAIN)
        public String content() {
            return "content";
        }
        @Path("/body")
        public MessageBody body() {
            return new MessageBody();
        }
    }
    static class MessageBody{
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "body";
        }
    }
}
