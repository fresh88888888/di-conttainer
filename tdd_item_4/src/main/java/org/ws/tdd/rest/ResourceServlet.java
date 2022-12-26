package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.io.IOException;

public class ResourceServlet extends HttpServlet {
    private Runtime runtime;
    public ResourceServlet(Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResourceRouter router = runtime.getResourceRouter();
        Providers providers = runtime.getProviders();
        OutboundResponse outboundResponse;
        try {
            outboundResponse = router.dispatch(req, runtime.createResourceContext(req, resp));
        }catch (WebApplicationException ex){
            outboundResponse = (OutboundResponse) ex.getResponse();
        }catch (Throwable throwable){
            outboundResponse = (OutboundResponse) ((ExceptionMapper) providers.getExceptionMapper(throwable.getClass())).toResponse(throwable);
        }

        resp.setStatus(outboundResponse.getStatus());
        MultivaluedMap<String, Object> headers = outboundResponse.getHeaders();
        for (String name: headers.keySet()) {
            for (Object value: headers.get(name)) {
                RuntimeDelegate.HeaderDelegate delegate = RuntimeDelegate.getInstance().createHeaderDelegate(value.getClass());
                resp.addHeader(name, delegate.toString(value));
            }
        }
        GenericEntity entity = outboundResponse.getGenericEntity();
        MessageBodyWriter writer = providers.getMessageBodyWriter(entity.getRawType(), entity.getType(), outboundResponse.getAnnotations(), outboundResponse.getMediaType());
        writer.writeTo(entity.getEntity(), entity.getRawType(), entity.getType(), outboundResponse.getAnnotations(), outboundResponse.getMediaType(),
                outboundResponse.getHeaders(), resp.getOutputStream());

    }
}
