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
import java.util.function.Supplier;

public class ResourceServlet extends HttpServlet {
    private Providers providers;
    private Runtime runtime;

    public ResourceServlet(Runtime runtime) {
        this.runtime = runtime;
        this.providers = runtime.getProviders();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        ResourceRouter router = runtime.getResourceRouter();
        respond(resp, () -> router.dispatch(req, runtime.createResourceContext(req, resp)));
    }
    private void respond(HttpServletResponse resp, Supplier<OutboundResponse> supplier){
        try {
            respond(resp, supplier.get());
        } catch (WebApplicationException ex) {
            respond(resp, () -> (OutboundResponse) ex.getResponse());
        }catch (Throwable throwable){
            respond(resp, () -> (OutboundResponse) ((ExceptionMapper) providers.getExceptionMapper(throwable.getClass())).toResponse(throwable));
        }
    }
    private void respond(HttpServletResponse resp, OutboundResponse outboundResponse) throws IOException {
        resp.setStatus(outboundResponse.getStatus());
        MultivaluedMap<String, Object> headers = outboundResponse.getHeaders();
        for (String name : headers.keySet()) {
            for (Object value : headers.get(name)) {
                RuntimeDelegate.HeaderDelegate delegate = RuntimeDelegate.getInstance().createHeaderDelegate(value.getClass());
                resp.addHeader(name, delegate.toString(value));
            }
        }
        GenericEntity entity = outboundResponse.getGenericEntity();
        if (entity != null) {
            MessageBodyWriter writer = providers.getMessageBodyWriter(entity.getRawType(), entity.getType(), outboundResponse.getAnnotations(), outboundResponse.getMediaType());
            writer.writeTo(entity.getEntity(), entity.getRawType(), entity.getType(), outboundResponse.getAnnotations(), outboundResponse.getMediaType(),
                    outboundResponse.getHeaders(), resp.getOutputStream());
        }
    }
}
