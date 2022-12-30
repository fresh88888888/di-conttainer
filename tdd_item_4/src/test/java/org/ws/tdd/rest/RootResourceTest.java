package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RootResourceTest {

    @Test
    public void should_get_uri_template_from_path_annotation(){
        ResourceRouter.RootResource resource = new RootResourceClass(Messages.class);
        UriTemplate template = resource.getUriTemplate();

        Assertions.assertTrue(template.match("/messages/hello").isPresent());
    }
    //TODO: find resource method, matches the http request and http method
    //TODO: if sub resource locator matches uri, using it to do follow up matching
    //TODO: if no method / sub resource locator matches, return 404
    @Path("/messages")
    static class Messages{

        @GET
        @Path("/hello")
        @Produces(MediaType.TEXT_PLAIN)
        public String hello(){
            return "hello";
        }
    }
}
