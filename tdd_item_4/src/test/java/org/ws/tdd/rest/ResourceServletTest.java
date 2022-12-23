package org.ws.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

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

    //TODO: use status code as http status
    @Test
    public void should_use_status_from_response() throws Exception{
        OutboundResponse response = mock(OutboundResponse.class);
        when(response.getStatus()).thenReturn(Response.Status.NOT_MODIFIED.getStatusCode());
        when(router.dispatch(any(), eq(context))).thenReturn(response);

        HttpResponse<String> httpResponse = get("/test");
        Assertions.assertEquals(httpResponse.statusCode(), Response.Status.NOT_MODIFIED.getStatusCode());

    }
    //TODO: use headers as http headers
    //TODO: writer body using MessageBodyWriter
    //TODO: 500 if MessageBodyWriter not found
    //TODO: throw WebApplicationException with response, use response
    //TODO: throw WebApplicationException with null response, use ExceptionMapper build response
    //TODO: throw other exception , use ExceptionMapper build response
}
