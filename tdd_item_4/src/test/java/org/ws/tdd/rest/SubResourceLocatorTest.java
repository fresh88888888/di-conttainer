package org.ws.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class SubResourceLocatorTest extends InjectableCallerTest {
    private UriTemplate.MatchResult result;
    private Map<String, String> matchedPathParameters = Map.of("param", "param");

    @BeforeEach
    public void setup() {
        super.setup();
        result = mock(UriTemplate.MatchResult.class);
        when(result.getMatchedPathParameters()).thenReturn(matchedPathParameters);
    }
    @Test
    public void should_add_matched_path_parameter_to_builder() throws NoSuchMethodException {
        parameters.put("param", List.of("param"));
        callInjectable("getPathParam", String.class);

        verify(builder).addMatchedPathParameters(matchedPathParameters);
    }
    @Test
    public void should_not_wrap_around_web_application_exception() throws NoSuchMethodException {
        parameters.put("param", List.of("param"));
        try {
            callInjectable("throwWebApplicationException", String.class);
        }catch (WebApplicationException ex){
            assertEquals(300, ex.getResponse().getStatus());
        }catch (Exception ex){
            fail();
        }
    }
    @Override
    protected Object initResource() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{SubResourceMethods.class}, (proxy, method, args) -> {
            lastCal = new LastCal(getMethodName(method.getName(), Arrays.stream(method.getParameters()).map(Parameter::getType).toList()), args != null ? List.of(args) : List.of());
            if(method.getName().equals("throwWebApplicationException")){
                throw new WebApplicationException(300);
            }
            return new Message();
        });
    }
    @Override
    protected void callInjectable(String method, Class<?> type) throws NoSuchMethodException {
        SubResourceLocators.SubResourceLocator locator = new SubResourceLocators.SubResourceLocator(SubResourceMethods.class.getMethod(method, type));
        locator.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, context, builder);
    }

    interface SubResourceMethods {
        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") String path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") int path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") double path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") byte path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") short path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") boolean path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") BigDecimal path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") String path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") int path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") short path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") double path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") boolean path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") byte path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") BigDecimal path);
        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") Convert value);
        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") Convert value);
        @Path("/message")
        @Produces(MediaType.TEXT_PLAIN)
        Message getContext(@Context SomeServiceInContext service);
        @Path("/message")
        @Produces(MediaType.TEXT_PLAIN)
        Message getContext(@Context ResourceContext context);
        @Path("/message")
        @Produces(MediaType.TEXT_PLAIN)
        Message getContext(@Context UriInfo uriInfo);
        @Path("/message/{param}")
        Message throwWebApplicationException(@PathParam("param") String value);
    }
    static class Message {
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String content() {
            return "content";
        }
    }
}
