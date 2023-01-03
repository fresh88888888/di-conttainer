package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import org.tdd.item.Context;

public interface Runtime {

    Providers getProviders();
    ResourceContext createResourceContext(HttpServletRequest req, HttpServletResponse resp);
    Context getApplicationContext();
    ResourceRouter getResourceRouter();
    UriInfoBuilder createUriInfoBuilder(HttpServletRequest request);
}
