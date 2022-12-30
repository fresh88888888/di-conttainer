package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubResourceLocatorsTest {

    @Test
    public void should_match_path_with_uri() {
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        ResourceRouter.SubResourceLocator locator = locators.finSubResource("/hello").get();

        assertEquals("Messages.hello", locator.toString());
    }

    @Test
    public void should_return_empty_if_not_match_uri(){
        SubResourceLocators locators = new SubResourceLocators(Messages.class.getMethods());
        assertTrue(locators.finSubResource("/missing").isEmpty());
    }
    @Path("/messages")
    static class Messages {
        @Path("/hello")
        public Message hello() {
            return new Message();
        }
    }
    static class Message {
    }
}
