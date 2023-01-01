package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubResourceLocatorsTest {
    @Test
    public void should_match_path_with_uri() {
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        ResourceRouter.SubResourceLocator locator = locators.finSubResource("/hello").get();

        assertEquals("Messages.hello", locator.toString());
    }

    @Test
    public void should_return_empty_if_not_match_uri() {
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        assertTrue(locators.finSubResource("/missing").isEmpty());
    }
    @Test
    public void should_call_locator_method_to_generate_sub_resource(){
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        ResourceRouter.SubResourceLocator subResourceLocator = locators.finSubResource("/hello").get();
        StubUriInfoBuilder infoBuilder = new StubUriInfoBuilder();
        infoBuilder.addMatchedResource(new Messages());
        ResourceRouter.Resource subResource = subResourceLocator.getSubResource(mock(ResourceContext.class), infoBuilder);

        UriTemplate.MatchResult result = mock(UriTemplate.MatchResult.class);
        when(result.getRemaining()).thenReturn(null);
        ResourceRouter.ResourceMethod resourceMethod = subResource.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, infoBuilder).get();

        assertEquals("Message.content", resourceMethod.toString());
        assertEquals("hello", ((Message)infoBuilder.getLastMatchedResource()).message);
    }
    @Path("/messages")
    static class Messages {
        @Path("/hello")
        public Message hello() {
            return new Message("hello");
        }
        @GET
        @Path("/topics/{id}")
        @Produces(MediaType.TEXT_PLAIN)
        public Message id() {
            return new Message("id");
        }
        @GET
        @Path("/topics/1234")
        @Produces(MediaType.TEXT_PLAIN)
        public Message message1234() {
            return new Message("1234");
        }
    }

    static class Message {
        private String message;
        public Message(String message) {
            this.message = message;
        }

        @GET
        public String content(){
            return "content";
        }
    }
}
