package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    }
}
