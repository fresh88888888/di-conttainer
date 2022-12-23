package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;

public class ResourceServlet extends HttpServlet {
    private Runtime runtime;
    public ResourceServlet(Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        ResourceRouter router = runtime.getResourceRouter();
        OutboundResponse outboundResponse = router.dispatch(req, runtime.createResourceContext(req, resp));
        resp.setStatus(outboundResponse.getStatus());
        MultivaluedMap<String, Object> headers = outboundResponse.getHeaders();
        for (String name: headers.keySet()) {
            for (Object value: headers.get(name)) {
                RuntimeDelegate.HeaderDelegate delegate = RuntimeDelegate.getInstance().createHeaderDelegate(value.getClass());
                resp.addHeader(name, delegate.toString(value));
            }
        }
    }
}
