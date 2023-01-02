package org.ws.tdd.rest;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class HeadResourceMethodTest {
    @Test
    public void should_call_method_and_ignore_return_value(){
        ResourceContext context = mock(ResourceContext.class);
        UriInfoBuilder builder = mock(UriInfoBuilder.class);
        ResourceRouter.ResourceMethod resourceMethod = mock(ResourceRouter.ResourceMethod.class);
        HeadResourceMethod headResourceMethod = new HeadResourceMethod(resourceMethod);

        assertNull(headResourceMethod.call(context, builder));
        verify(resourceMethod).call(same(context), same(builder));
    }
    @Test
    public void should_delegate_to_method_for_uri_template() {
        ResourceRouter.ResourceMethod resourceMethod = mock(ResourceRouter.ResourceMethod.class);
        UriTemplate template = mock(UriTemplate.class);
        HeadResourceMethod headResourceMethod = new HeadResourceMethod(resourceMethod);
        when(resourceMethod.getUriTemplate()).thenReturn(template);

        assertEquals(template, headResourceMethod.getUriTemplate());
    }

    @Test
    public void should_delegate_to_method_for_http_method() {
        ResourceRouter.ResourceMethod resourceMethod = mock(ResourceRouter.ResourceMethod.class);
        HeadResourceMethod headResourceMethod = new HeadResourceMethod(resourceMethod);
        when(resourceMethod.getHttpMethod()).thenReturn("GET");

        assertEquals(HttpMethod.HEAD, headResourceMethod.getHttpMethod());
    }
}
