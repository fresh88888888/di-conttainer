package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceDispatcherTest {
    private RuntimeDelegate delegate;
    private Runtime runtime;
    private HttpServletRequest request;
    private ResourceContext context;
    private UriInfoBuilder builder;

    @BeforeEach
    public void before() {
        runtime = mock(Runtime.class);
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);
        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
        request = mock(HttpServletRequest.class);
        context = mock(ResourceContext.class);
        when(request.getServletPath()).thenReturn("/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeaders(eq(HttpHeaders.ACCEPT))).thenReturn(new Vector<>(List.of(MediaType.WILDCARD)).elements());
        builder = mock(UriInfoBuilder.class);
        when(runtime.createUriInfoBuilder(same(request))).thenReturn(builder);
    }

    //TODO: 根据与Path匹配结果，降序排序RootResource，选择第一个RootResource
    //TODO: R1、R2, R1 matched, R2 none R1
    //TODO: R1、R2, R1、R2 matched, R1 result < R2 result R1
    @Test
    public void should_use_matched_root_resource() {
        ResourceRouter.RootResource matched = mock(ResourceRouter.RootResource.class);
        UriTemplate matchedUriTemplate = mock(UriTemplate.class);
        UriTemplate.MatchResult result = mock(UriTemplate.MatchResult.class);
        when(matched.getUriTemplate()).thenReturn(matchedUriTemplate);
        when(matchedUriTemplate.match(eq("/users"))).thenReturn(Optional.of(result));
        ResourceRouter.ResourceMethod method = mock(ResourceRouter.ResourceMethod.class);
        when(matched.match(eq("/users"), eq("GET"), eq(new String[]{MediaType.WILDCARD}), eq(builder))).thenReturn(Optional.of(method));
        GenericEntity entity = new GenericEntity("matched", String.class);
        when(method.call(any(), any())).thenReturn(entity);

        ResourceRouter.RootResource unmatched = mock(ResourceRouter.RootResource.class);
        UriTemplate unmatchedUriTemplate = mock(UriTemplate.class);
        when(unmatched.getUriTemplate()).thenReturn(unmatchedUriTemplate);
        when(unmatchedUriTemplate.match(eq("/users"))).thenReturn(Optional.empty());

        DefaultResourceRouter router = new DefaultResourceRouter(runtime, List.of(matched, unmatched));
        OutboundResponse response = router.dispatch(request, context);
        assertSame(response.getGenericEntity(), entity);
        assertEquals(200, response.getStatus());
    }
    //TODO: 如果没有匹配到RootResource，则构造404的Response
    //TODO: 如果返回的RootResource中无法匹配剩余Path，则构造404的Response
    //TODO: 如果ResourceMethod返回null，则构造204的Response
}
