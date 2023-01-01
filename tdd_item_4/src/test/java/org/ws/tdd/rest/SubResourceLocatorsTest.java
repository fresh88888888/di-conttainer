package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubResourceLocatorsTest {
    @ParameterizedTest(name = "{2}")
    @CsvSource(textBlock = """
            /hello,              hello,              fully matched with uri
            /topics/1234,        1234,               multiple matched choices
            /topics/1,           id,                 matched with variable
            """)
    public void should_match_path_with_uri(String path, String message, String context) {
        StubUriInfoBuilder infoBuilder = new StubUriInfoBuilder();
        infoBuilder.addMatchedResource(new Messages());
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());

        assertTrue(locators.findSubResourceMethods(path, "GET", new String[]{MediaType.TEXT_PLAIN}, mock(ResourceContext.class), infoBuilder).isPresent());
        assertEquals(message, ((Message)infoBuilder.getLastMatchedResource()).message);
    }
    @ParameterizedTest(name = "{1}")
    @CsvSource(textBlock = """
            /missing,              unmatched in resource
            /hello/content,        unmatched sub-resource method
            """)
    public void should_return_empty_if_not_match_uri(String path, String context) {
        StubUriInfoBuilder infoBuilder = new StubUriInfoBuilder();
        infoBuilder.addMatchedResource(new Messages());
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());

        assertFalse(locators.findSubResourceMethods(path, "GET", new String[]{MediaType.TEXT_PLAIN}, mock(ResourceContext.class), infoBuilder).isPresent());
    }
    @Path("/messages")
    static class Messages {
        @Path("/hello")
        public Message hello() {
            return new Message("hello");
        }
        @Path("/topics/{id}")
        @Produces(MediaType.TEXT_PLAIN)
        public Message id() {
            return new Message("id");
        }
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
