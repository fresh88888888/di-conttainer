package org.ws.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceServletTest extends ServletTest{

    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext context;

    @Override
    protected Servlet getServlet() {
        runtime = mock(Runtime.class);
        router = mock(ResourceRouter.class);
        context = mock(ResourceContext.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createResourceContext(any(), any())).thenReturn(context);

        return new ResourceServlet(runtime);
    }

    @Test
    public void should_use_status_from_response() throws Exception{
        OutboundResponse response = mock(OutboundResponse.class);
        when(response.getStatus()).thenReturn(Response.Status.NOT_MODIFIED.getStatusCode());
        when(router.dispatch(any(), eq(context))).thenReturn(response);
        when(response.getHeaders()).thenReturn(new MultivaluedHashMap<>());

        HttpResponse<String> httpResponse = get("/test");
        assertEquals(httpResponse.statusCode(), Response.Status.NOT_MODIFIED.getStatusCode());

    }
    @Test
    public void should_use_http_headers_from_response() throws Exception{

        RuntimeDelegate delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);

        when(delegate.createHeaderDelegate(NewCookie.class)).thenReturn(new RuntimeDelegate.HeaderDelegate<>() {
            @Override
            public NewCookie fromString(String value) {
                return null;
            }

            @Override
            public String toString(NewCookie value) {
                return value.getName() + "="+ value.getValue();
            }
        });

        NewCookie sessionId = new NewCookie.Builder("SESSION_ID").value("session").build();
        NewCookie userId = new NewCookie.Builder("USER_ID").value("user").build();
        OutboundResponse response = mock(OutboundResponse.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.addAll("Set-Cookie", sessionId, userId);

        when(response.getStatus()).thenReturn(Response.Status.NOT_MODIFIED.getStatusCode());
        when(response.getHeaders()).thenReturn(headers);
        when(router.dispatch(any(), eq(context))).thenReturn(response);

        HttpResponse<String> httpResponse = get("/test");
        assertArrayEquals(httpResponse.headers().allValues("Set-Cookie").toArray(String[]::new), new String[]{"SESSION_ID=session", "USER_ID=user"});

    }
    //TODO: writer body using MessageBodyWriter
    //TODO: 500 if MessageBodyWriter not found
    //TODO: throw WebApplicationException with response, use response
    //TODO: throw WebApplicationException with null response, use ExceptionMapper build response
    //TODO: throw other exception , use ExceptionMapper build response
}
