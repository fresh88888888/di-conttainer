package org.ws.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class DefaultResourceMethodTest {
    private ResourceContext context;
    private UriInfoBuilder builder;
    private CallableResourceMethods resource;
    private UriInfo uriInfo;
    private MultivaluedHashMap<String, String> parameters;
    //private MultivaluedHashMap<String, String> queryParameters;

    @BeforeEach
    public void setup() {
        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        resource = mock(CallableResourceMethods.class);
        uriInfo = mock(UriInfo.class);
        parameters = new MultivaluedHashMap<>();
        //queryParameters = new MultivaluedHashMap<>();
        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(builder.createUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
    }

    @Test
    public void should_call_resource_method() throws NoSuchMethodException {
        when(resource.get()).thenReturn("resource called");
        DefaultResourceMethod resourceMethod = getResourceMethod("get");

        assertEquals(new GenericEntity<>("resource called", String.class), resourceMethod.call(context, builder));
    }

    @Test
    public void should_use_resource_method_generic_return_type() throws NoSuchMethodException {
        when(resource.getList()).thenReturn(List.of());
        DefaultResourceMethod resourceMethod = getResourceMethod("getList");

        assertEquals(new GenericEntity<>(List.of(), CallableResourceMethods.class.getMethod("getList").getGenericReturnType()), resourceMethod.call(context, builder));
    }
    @Test
    public void should_inject_String_to_path_param() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getPathParam", String.class);
        parameters.put("path", List.of("path"));
        resourceMethod.call(context, builder);

        verify(resource).getPathParam(eq("path"));
    }
    @Test
    public void should_call_resource_method_with_void_return_type() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("post");

        assertNull(resourceMethod.call(context, builder));
    }
    @Test
    public void should_inject_int_to_path_param() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getPathParam", int.class);
        parameters.put("path", List.of("1"));
        resourceMethod.call(context, builder);

        verify(resource).getPathParam(eq(1));
    }
    @Test
    public void should_inject_string_to_query_param() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getQueryParam", String.class);
        parameters.put("query", List.of("query"));
        resourceMethod.call(context, builder);

        verify(resource).getQueryParam(eq("query"));
    }
    @Test
    public void should_inject_int_to_query_param() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getQueryParam", int.class);
        parameters.put("query", List.of("1"));
        resourceMethod.call(context, builder);

        verify(resource).getQueryParam(eq(1));
    }
    //TODO: using default convertors for path, matrix, query, form, header, cookie
    //TODO: default convertors for int, float, double, byte, char, String and boolean
    //TODO: default convertors for class with converter constructor
    //TODO: default convertors for class with converter factory
    //TODO: default convertors for List, Set, SortSet
    //TODO: injection - get injectable from resource context
    //TODO: injection - can inject resource context itself
    //TODO: injection - can inject uri info built from uri info builder
    private DefaultResourceMethod getResourceMethod(String method, Class... types) throws NoSuchMethodException {
        return new DefaultResourceMethod(CallableResourceMethods.class.getMethod(method, types));
    }

    interface CallableResourceMethods {
        @GET
        String get();
        @POST
        void post();
        @GET
        List<String> getList();
        @GET
        String getPathParam(@PathParam("path") String value);
        @GET
        String getPathParam(@PathParam("path") int value);
        @GET
        String getQueryParam(@QueryParam("query") String value);
        @GET
        String getQueryParam(@QueryParam("query") int value);
    }
}
