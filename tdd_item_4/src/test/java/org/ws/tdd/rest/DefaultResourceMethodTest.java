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
    //TODO: using default convertors for path, matrix, query, form, header, cookie
    //TODO: default converters for int, float, double, byte, char and boolean
    //TODO: default converters for class with converter constructor
    //TODO: default converters for class with converter factory
    //TODO: default converters for List, Set, SortSet
    //TODO: injection - get injectable from resource context
    //TODO: injection - can inject resource context itself
    //TODO: injection - can inject uri info built from uri info builder
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
