package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultResourceMethodTest {
    private ResourceContext context;
    private UriInfoBuilder builder;
    private CallableResourceMethods resource;
    @BeforeEach
    public void setup(){
        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        resource = mock(CallableResourceMethods.class);
        when(builder.getLastMatchedResource()).thenReturn(resource);
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
    //TODO: injection - context
    //TODO: injection - uri info: path, query, matrix...
    private DefaultResourceMethod getResourceMethod(String method) throws NoSuchMethodException {
        return new DefaultResourceMethod(CallableResourceMethods.class.getMethod(method));
    }
    interface CallableResourceMethods{
        @GET
        String get();
        @GET
        List<String> getList();
    }
}
